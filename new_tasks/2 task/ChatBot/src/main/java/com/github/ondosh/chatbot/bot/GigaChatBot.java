package com.github.ondosh.chatbot.bot;

import com.github.ondosh.chatbot.model.UserProfile;

import javax.net.ssl.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.UUID;

/**
 * Реализация бота на основе GigaChat API от Сбера.
 * Получает токен доступа через OAuth 2.0 и отправляет запросы к LLM.
 * Токен автоматически обновляется по истечении срока жизни.
 */
public class GigaChatBot implements IBot {

    private static final String AUTHORIZATION_KEY = readKeyFromFile("Auth_key.txt");
    private static final String CLIENT_ID = readKeyFromFile("client_id.txt");

    private static String readKeyFromFile(String fileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(fileName))).trim();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл: " + fileName, e);
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

    /** HTTP-клиент с отключённой проверкой SSL-сертификата. */
    private final HttpClient client;

    /** Текущий токен доступа, полученный после авторизации. */
    private String accessToken = null;

    /** Время (в миллисекундах), после которого токен считается устаревшим. */
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

    /**
     * Создаёт {@link HttpClient}, который доверяет любым SSL-сертификатам.
     * <b>Внимание:</b> не использовать в продакшене — небезопасно.
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
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
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
     * Основной метод получения ответа от GigaChat.
     * Перед запросом проверяет актуальность токена и при необходимости обновляет его.
     *
     * @param input текст сообщения от пользователя
     * @return текстовый ответ модели или сообщение об ошибке
     */
    @Override
    public String getResponse(String input) {
        try {
            // Убеждаемся, что токен существует и не устарел
            ensureValidToken();

            // Формируем тело запроса в формате JSON.
            // Системное сообщение запрещает модели использовать Markdown-разметку,
            // чтобы ответ отображался как чистый текст в чате.
            String requestBody = String.format("""
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
                    "temperature": 0.7,
                    "max_tokens": 1000
                }
                """, escapeJson(buildSystemPrompt()), escapeJson(input));

            // Строим HTTP-запрос с токеном в заголовке Bearer
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                // Успешный ответ — парсим и возвращаем текст
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
        return text.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
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
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String buildSystemPrompt() {
        String base = "Отвечай только обычным текстом. НЕ используй Markdown, HTML или любое другое форматирование. " +
                "Не используй символы *, #, `, [], (), >. Отвечай простыми предложениями без специального форматирования.";

        if (userProfile == null) return base;

        return base + " Собеседника зовут " + userProfile.getName() +
                ", ему " + userProfile.getAge() + " лет, он из города " + userProfile.getCity() + ".";
    }

    /**
     * GigaChatBot не обрабатывает команды — эта логика делегирована {@link CommandParser}.
     *
     * @param input входное сообщение
     * @return всегда {@code false}
     */
    @Override
    public boolean isCommand(String input) {
        return false;
    }

    /**
     * GigaChatBot не выполняет команды — эта логика делегирована {@link CommandParser}.
     *
     * @param input входное сообщение
     * @return всегда пустая строка
     */
    @Override
    public String executeCommand(String input) {
        return "";
    }
}