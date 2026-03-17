package com.github.ondosh.chatbot.bot;

import javax.net.ssl.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.UUID;

public class GigaChatBot implements IBot {

    private static final String AUTHORIZATION_KEY = "MDE5Y2Y1M2MtNTE4ZS03ZWM5LTk1YWYtMDc0OGE2YTA5ZWNhOjUwYWVhMjg0LTYwNWYtNDRjYi1iNzY3LWYyMzRmYjI1MTQ4Zg==";
    private static final String CLIENT_ID = "019cf53c-518e-7ec9-95af-0748a6a09eca";

    private static final String AUTH_URL = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth";
    private static final String API_URL = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions";

    private final HttpClient client;
    private String accessToken = null;
    private long tokenExpiryTime = 0;

    public GigaChatBot() {
        // Отключаем проверку SSL для тестирования
        this.client = createTrustAllHttpClient();
    }

    private HttpClient createTrustAllHttpClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return HttpClient.newHttpClient();
        }
    }

    @Override
    public String getResponse(String input) {
        try {
            ensureValidToken();

            // Добавляем инструкцию в системное сообщение
            String requestBody = String.format("""
                {
                    "model": "GigaChat",
                    "messages": [
                        {
                            "role": "system",
                            "content": "Отвечай только обычным текстом. НЕ используй Markdown, HTML или любое другое форматирование. Не используй символы *, #, `, [], (), >. Отвечай простыми предложениями без специального форматирования."
                        },
                        {
                            "role": "user",
                            "content": "%s"
                        }
                    ],
                    "temperature": 0.7,
                    "max_tokens": 1000
                }
                """, escapeJson(input));


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

            System.out.println("API Response Status: " + response.statusCode());
            System.out.println("API Response Body: " + response.body());

            if (response.statusCode() == 200) {
                return parseResponse(response.body());
            } else {
                return "Ошибка API: " + response.body();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка: " + e.getMessage();
        }
    }

    private void ensureValidToken() throws Exception {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpiryTime) {
            refreshAccessToken();
        }
    }

    private void refreshAccessToken() throws Exception {
        System.out.println("Получение нового токена...");
        System.out.println("Client ID: " + CLIENT_ID);
        System.out.println("Auth Key: " + AUTHORIZATION_KEY.substring(0, Math.min(10, AUTHORIZATION_KEY.length())) + "...");

        // Для GigaChat нужно использовать Authorization Key напрямую, без кодирования
        // Authorization Key уже является готовым токеном для Basic авторизации
        String authHeader = "Basic " + AUTHORIZATION_KEY;

        System.out.println("Auth Header: " + authHeader.substring(0, Math.min(30, authHeader.length())) + "...");

        String requestBody = "scope=GIGACHAT_API_PERS";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AUTH_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("Authorization", authHeader)
                .header("RqUID", UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        System.out.println("Sending auth request to: " + AUTH_URL);

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("Auth Response Status: " + response.statusCode());
        System.out.println("Auth Response Body: " + response.body());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ошибка авторизации: " + response.statusCode() + " - " + response.body());
        }

        parseAuthResponse(response.body());
        System.out.println("Токен успешно получен");
    }

    private void parseAuthResponse(String json) {
        try {
            // Парсим access_token
            String tokenKey = "\"access_token\":\"";
            int tokenStart = json.indexOf(tokenKey);
            if (tokenStart != -1) {
                tokenStart += tokenKey.length();
                int tokenEnd = json.indexOf("\"", tokenStart);
                if (tokenEnd != -1) {
                    accessToken = json.substring(tokenStart, tokenEnd);
                }
            }

            // Парсим expires_at
            String expiresKey = "\"expires_at\":\"";
            int expiresStart = json.indexOf(expiresKey);
            if (expiresStart != -1) {
                expiresStart += expiresKey.length();
                int expiresEnd = json.indexOf("\"", expiresStart);
                if (expiresEnd != -1) {
                    // Устанавливаем время жизни токена (29 минут для запаса)
                    tokenExpiryTime = System.currentTimeMillis() + 29 * 60 * 1000;
                }
            } else {
                // Если не нашли expires_at, устанавливаем 29 минут
                tokenExpiryTime = System.currentTimeMillis() + 29 * 60 * 1000;
            }

            if (accessToken == null) {
                throw new RuntimeException("Не удалось найти access_token в ответе");
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка парсинга токена: " + e.getMessage());
        }
    }

    private String parseResponse(String json) {
        try {
            System.out.println("Parsing response: " + json);

            // Парсим ответ от GigaChat
            String contentKey = "\"content\":\"";
            int contentStart = json.indexOf(contentKey);
            if (contentStart != -1) {
                contentStart += contentKey.length();
                int contentEnd = json.indexOf("\"", contentStart);
                if (contentEnd != -1) {
                    String content = json.substring(contentStart, contentEnd);
                    return decodeJsonString(content);
                }
            }

            // Пробуем другой формат
            String messageKey = "\"message\":{\"content\":\"";
            int messageStart = json.indexOf(messageKey);
            if (messageStart != -1) {
                messageStart += messageKey.length();
                int messageEnd = json.indexOf("\"", messageStart);
                if (messageEnd != -1) {
                    String content = json.substring(messageStart, messageEnd);
                    return decodeJsonString(content);
                }
            }

            return "Не удалось получить ответ. Ответ: " + json;

        } catch (Exception e) {
            return "Ошибка парсинга: " + e.getMessage();
        }
    }

    private String decodeJsonString(String text) {
        return text.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    public boolean isCommand(String input) {
        return false; // SimpleChatBot не обрабатывает команды
    }

    @Override
    public String executeCommand(String input) {
        return ""; // SimpleChatBot не выполняет команды
    }
}