package com.github.ondosh.chatbot.bot;

import com.github.ondosh.chatbot.model.UserProfile;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.UUID;

/**
 * Реализация бота на основе GigaChat API от Сбера.
 * Получает токен доступа через OAuth 2.0 и отправляет запросы к LLM.
 * Токен автоматически обновляется по истечении срока жизни.
 */
public class GigaChatBot implements IBot {

    private static final String AUTHORIZATION_KEY = readKeyFromFile();

    /**
     * Считывает ключ для использования нейросети
     */
    private static String readKeyFromFile() {
        try {
            // Ищем файл рядом с jar или в папке проекта
            Path path = Paths.get(System.getProperty("user.dir"), "Auth_key.txt");
            return new String(Files.readAllBytes(path)).trim();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл: " + "Auth_key.txt", e);
        }
    }

    /**
     * URL для получения OAuth-токена через Сбербанк API.
     */
    private static final String AUTH_URL = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth";

    /**
     * URL основного API GigaChat для отправки сообщений модели.
     */
    private static final String API_URL = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions";

    /**
     * Значения параметров модели по умолчанию
     */
    private double temperature = 0.7;
    private int max_tokens = 1000;

    /**
     * HTTP-клиент с отключённой проверкой SSL-сертификата.
     */
    private final HttpClient client;

    /**
     * Текущий токен доступа, полученный после авторизации.
     */
    private String accessToken = null;

    /**
     * Время (в миллисекундах), после которого токен считается устаревшим.
     */
    private long tokenExpiryTime = 0;

    /**
     * Конструктор. Создаёт HTTP-клиент с отключённой SSL-валидацией —
     * необходимо для работы с самоподписанными сертификатами Сбербанка в тестовой среде.
     */
    public GigaChatBot() {
        this.client = createTrustAllHttpClient();
    }

    private UserProfile userProfile = null;

    public void setUserProfile(UserProfile profile) {
        this.userProfile = profile;
    }

    @Override
    public void setStats(int stat, int stat1, int stat2) {

    }

    @Override
    public int getTotalMessages() {
        return 0;
    }

    @Override
    public int getUserMessages() {
        return 0;
    }

    @Override
    public int getBotMessages() {
        return 0;
    }

    /**
     * Создаёт {@link HttpClient}, который доверяет любым SSL-сертификатам.
     *
     * @return настроенный HTTP-клиент
     */
    private HttpClient createTrustAllHttpClient() {
        try {
            // Создаём TrustManager, который принимает любой сертификат без проверки
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        // Проверки клиентского и серверного сертификатов намеренно пропущены
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // Инициализируем SSL-контекст с нашим доверяющим-всему менеджером
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();

        } catch (Exception e) {
            // Если не удалось создать кастомный клиент — падаем на стандартный
            e.printStackTrace();
            return HttpClient.newHttpClient();
        }
    }

    /**
     * Запасной метод получения ответа, который берёт значения по умолчанию
     * и вызывает основной (с 3 аргументами).
     */
    @Override
    public String getResponse(String input) {
        // Используем значения по умолчанию из полей класса
        return getResponse(input, this.temperature, this.max_tokens);
    }
    /**
     * Основной метод получения ответа от GigaChat.
     * Перед запросом проверяет актуальность токена и при необходимости обновляет его.
     *
     * @param input текст сообщения от пользователя
     * @return текстовый ответ модели или сообщение об ошибке
     */
    public String getResponse(String input, double temperature, int max_tokens) {
        try {
            // Убеждаемся, что токен существует и не устарел
            ensureValidToken();
            double temp = temperature;
            int m_tokens = max_tokens;

            // Формируем тело запроса в формате JSON.
            // Системное сообщение запрещает модели использовать Markdown-разметку,
            // чтобы ответ отображался как чистый текст в чате.
            String requestBody = String.format(java.util.Locale.US, """
                    {
                        "model": "GigaChat",
                        "messages": [
                            {
                                "role": "system",
                                "content": "%s"
                            },
                            {
                                "role": "user",
                                "content": "%s"
                            }
                        ],
                        "temperature": %f,
                        "max_tokens": %d
                    }
                    """, escapeJson(buildSystemPrompt()),
                    escapeJson(input),
                    temp, m_tokens); //сделать возможность изменения

            // Строим HTTP-запрос с токеном в заголовке Bearer
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Отправляем HTTP-запрос и получаем ответ от сервера.
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString() // превращает тело ответа в строку
            );

            if (response.statusCode() == 200) {
                // ответ получен
                return parseResponse(response.body());
            } else {
                return "Ошибка API: " + response.body();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка: " + e.getMessage();
        }
    }

    /**
     * Проверяет, действителен ли текущий токен.
     * Если токен отсутствует или истёк — запрашивает новый.
     *
     * @throws Exception если авторизация завершилась ошибкой
     */
    private void ensureValidToken() throws Exception {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpiryTime) {
            refreshAccessToken();
        }
    }

    /**
     * Выполняет OAuth-авторизацию и сохраняет новый токен.
     * Использует AUTHORIZATION_KEY как готовый Base64-encoded заголовок Basic Auth.
     *
     * @throws Exception если HTTP-запрос завершился с ошибкой или токен не удалось распарсить
     */
    private void refreshAccessToken() throws Exception {
        String authHeader = "Basic " + AUTHORIZATION_KEY;
        String requestBody = "scope=GIGACHAT_API_PERS";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AUTH_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("Authorization", authHeader)
                .header("RqUID", UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ошибка авторизации: " + response.statusCode() + " - " + response.body());
        }

        parseAuthResponse(response.body());
    }

    /**
     * Извлекает {@code access_token} и время жизни токена из JSON-ответа авторизации.
     * Используется ручной парсинг, чтобы не вводить зависимость от сторонних библиотек.
     *
     * @param json тело ответа от OAuth-эндпоинта
     * @throws RuntimeException если токен не найден в ответе
     */
    private void parseAuthResponse(String json) {
        try {
            // Ищем поле access_token в JSON-строке
            String tokenKey = "\"access_token\":\"";
            int tokenStart = json.indexOf(tokenKey);
            if (tokenStart != -1) {
                tokenStart += tokenKey.length();
                int tokenEnd = json.indexOf("\"", tokenStart);
                if (tokenEnd != -1) {
                    accessToken = json.substring(tokenStart, tokenEnd);
                }
            }

            // Ищем поле expires_at — Unix timestamp в миллисекундах, без кавычек.
            // Токен обновляем за 1 минуту до истечения; если поле отсутствует — берём 29 минут.
            String expiresKey = "\"expires_at\":";
            int expiresStart = json.indexOf(expiresKey);
            if (expiresStart != -1) {
                expiresStart += expiresKey.length();
                int expiresEnd = json.indexOf(",", expiresStart);

                // Если запятой нет — expires_at последнее поле, берём до закрывающей скобки
                if (expiresEnd == -1) {
                    expiresEnd = json.indexOf("}", expiresStart);
                }

                long expiresAt = Long.parseLong(json.substring(expiresStart, expiresEnd).trim());
                tokenExpiryTime = expiresAt - 60 * 1000;
            } else {
                tokenExpiryTime = System.currentTimeMillis() + 29 * 60 * 1000;
            }

            if (accessToken == null) {
                throw new RuntimeException("Не удалось найти access_token в ответе");
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка парсинга токена: " + e.getMessage());
        }
    }

    /**
     * Извлекает текст ответа модели из JSON-тела ответа GigaChat API.
     *
     * @param json тело ответа от API
     * @return декодированный текст ответа или сообщение об ошибке
     */
    private String parseResponse(String json) {
        try {
            // Ищем поле content — оно содержит текст ответа модели
            String contentKey = "\"content\":\"";
            int contentStart = json.indexOf(contentKey);
            if (contentStart != -1) {
                contentStart += contentKey.length();
                int contentEnd = json.indexOf("\"", contentStart);
                if (contentEnd != -1) {
                    return decodeJsonString(json.substring(contentStart, contentEnd));
                }
            }

            return "Не удалось получить ответ. Ответ: " + json;

        } catch (Exception e) {
            return "Ошибка парсинга: " + e.getMessage();
        }
    }

    /**
     * Декодирует escape-последовательности JSON в реальные символы.
     * Например, {@code \n} → перенос строки, {@code \"} → кавычка.
     *
     * @param text строка с JSON escape-последовательностями
     * @return строка с реальными символами
     */
    private String decodeJsonString(String text) {
        return text.replace("\\\\", "\\")    // Обратный слеш (должен быть первым!)
                .replace("\\\"", "\"")    // Двойная кавычка
                .replace("\\n", "\n")     // Перевод строки
                .replace("\\r", "\r")     // Возврат каретки
                .replace("\\t", "\t")     // Табуляция
                .replace("\\b", "\b")     // Backspace
                .replace("\\f", "\f");    // Form feed
    }
    /**
     * Экранирует специальные символы в строке для безопасной вставки в JSON.
     * Предотвращает инъекции и синтаксические ошибки при формировании тела запроса.
     *
     * @param text исходная строка (может быть null)
     * @return строка, безопасная для вставки в JSON-значение
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")   // Обратный слеш
                .replace("\"", "\\\"")   // Двойная кавычка
                .replace("\n", "\\n")    // Перенос строки
                .replace("\r", "\\r")    // Возврат каретки
                .replace("\t", "\\t")    // Табуляция
                .replace("\b", "\\b")    // Backspace
                .replace("\f", "\\f");   // Form feed
    }

    /**
     * Делаем промпт для нейронки.
     * Говорим ей в каком именно формате отвечать
     * (нам важно чтобы нейронка не отвечала в формате md)
     */
    private String buildSystemPrompt() {
        String base = "Отвечай только обычным текстом. НЕ используй Markdown, HTML или любое другое форматирование. " +
                "Не используй символы *, #, `, [], (), >. Отвечай простыми предложениями без специального форматирования.";

        if (userProfile == null) return base;

        return base + " Собеседника зовут " + userProfile.getName() +
                ", ему " + userProfile.getAge() + " лет, он из города " + userProfile.getCity() + ".";
    }

    /**
     * Получаем имя бота.
     * @return "GigaChat"
     */
    @Override
    public String getBotName() {
        return "GigaChat";
    }

    /**
     * Проверка на то, доступен ли бот (проверяет актуальность токена)
     * @return boolean
     */
    @Override
    public boolean isAvailable() {
        try {
            ensureValidToken();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public double getTemperature() {
        return this.temperature;
    }

    public void setTemperature(double temp) {
        if ((0.0 <= temp) && (temp <= 2.0)) {
            this.temperature = temp;
        } else {
            throw new IllegalArgumentException("Температура должна быть в диапазоне от 0.0 до 2.0");
        }
    }

    public void setMax_tokens(int m_tokens) {
        if ((10 <= m_tokens) && (m_tokens <= 6000)) {
            this.max_tokens = m_tokens;
        } else {
            throw new IllegalArgumentException("Максимальное количество токенов должно быть в диапазоне от 10 до 6000");
        }

    }

    public int getMax_tokens() {
        return this.max_tokens;
    }

}