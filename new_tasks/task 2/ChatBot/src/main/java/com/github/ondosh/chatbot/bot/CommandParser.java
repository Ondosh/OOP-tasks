package com.github.ondosh.chatbot.bot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CommandParser {

    // Статистика
    private int totalMessages = 0;
    private int userMessages  = 0;
    private int botMessages   = 1;

    public void countUserMessage() { userMessages++; totalMessages++; }
    public void countBotMessage()  { botMessages++;  totalMessages++; }

    // ---------------------------------------------------------------
    // Фразовые паттерны (бывший SimpleChatBot)
    // Ключ — Pattern, значение — ответ
    // ---------------------------------------------------------------
    private static final Map<Pattern, String> PHRASE_PATTERNS = new LinkedHashMap<>();

    static {
        // Приветствия
        PHRASE_PATTERNS.put(re("привет|здравствуй|хай|добрый\\s+день|добрый\\s+вечер|доброе\\s+утро"),
                "Привет! Если хочешь узнать команды, то напиши \"помоги\". ");

        // Прощания
        PHRASE_PATTERNS.put(re("пока|до\\s+свидания|увидимся|до\\s+встречи"),
                "Пока! Было приятно пообщаться.");

        // Вопросы о боте
        PHRASE_PATTERNS.put(re("как\\s+тебя\\s+зовут|твоё\\s+имя|твое\\s+имя"),
                "Меня зовут ChatBot. А тебя?");
        PHRASE_PATTERNS.put(re("кто\\s+ты|что\\s+ты\\s+такое"),
                "Я чат-бот, написанный на Java.");

        // Состояние
        PHRASE_PATTERNS.put(re("как\\s+(ты|дела|жизнь|поживаешь)"),
                "Всё отлично, спасибо что спросил!");

        // Благодарности
        PHRASE_PATTERNS.put(re("спасибо|благодарю|благодарен"),
                "Пожалуйста! Обращайся.");

        // Справка — объединяет «что умеешь», «помоги», «команды»
        PHRASE_PATTERNS.put(re("что\\s+ты\\s+умеешь|что\\s+умеешь|список\\s+команд|команды|помощь|помоги|помогите"),
                "Вот что я умею:\n" +
                        "• «который час» / «время» — текущее время\n" +
                        "• «какая дата» / «какой день» — сегодняшняя дата\n" +
                        "• «умножь X на Y» — умножение\n" +
                        "• «раздели X на Y» — деление\n" +
                        "• «сложи X и Y» — сложение\n" +
                        "• «вычти X из Y» — вычитание\n" +
                        "• «статистика» — сколько сообщений в чате\n" +
                        "• Простые фразы: привет, пока, как дела и т.д.\n" +
                        "• Отвечаю на любые вопросы, обращаясь к нейросети.");
    }

    private static Pattern re(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    // ---------------------------------------------------------------
    // Командные паттерны
    // ---------------------------------------------------------------
    private static final Pattern P_TIME  = Pattern.compile(
            "который час|сколько времени|какое время|время", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_DATE  = Pattern.compile(
            "какая дата|какой день|какое сегодня", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_STAT  = Pattern.compile(
            "статистика|сколько сообщений", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_MUL   = Pattern.compile(
            "умножь\\s+(-?[\\d.,]+)\\s+на\\s+(-?[\\d.,]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_DIV   = Pattern.compile(
            "раздели\\s+(-?[\\d.,]+)\\s+на\\s+(-?[\\d.,]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_ADD   = Pattern.compile(
            "сложи\\s+(-?[\\d.,]+)\\s+и\\s+(-?[\\d.,]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_SUB   = Pattern.compile(
            "вычти\\s+(-?[\\d.,]+)\\s+из\\s+(-?[\\d.,]+)", Pattern.CASE_INSENSITIVE);

    // ---------------------------------------------------------------
    // Публичный API
    // ---------------------------------------------------------------

    /**
     * Фраза считается «простой» только если вся значимая часть сообщения
     * покрывается паттерном — то есть остаток после совпадения не длиннее
     * порога. Это защищает от «привет, объясни мне квантовую физику».
     */
    public boolean isPhrase(String input) {
        String lower = input.toLowerCase().trim();
        for (Pattern p : PHRASE_PATTERNS.keySet()) {
            var m = p.matcher(lower);
            if (m.find()) {
                // Считаем «хвост»: всё, что не вошло в совпадение
                String leftover = (lower.substring(0, m.start()) + lower.substring(m.end()))
                        .replaceAll("[\\s,!?.]+", "");
                if (leftover.length() <= 8) { // порог — подбирается под проект
                    return true;
                }
            }
        }
        return false;
    }

    public String executePhrase(String input) {
        String lower = input.toLowerCase().trim();
        for (Map.Entry<Pattern, String> entry : PHRASE_PATTERNS.entrySet()) {
            if (entry.getKey().matcher(lower).find()) {
                return entry.getValue();
            }
        }
        return getFallback(lower);
    }

    public boolean isCommand(String input) {
        String lower = input.toLowerCase().trim();
        return P_TIME.matcher(lower).find()
                || P_DATE.matcher(lower).find()
                || P_STAT.matcher(lower).find()
                || P_MUL.matcher(lower).find()
                || P_DIV.matcher(lower).find()
                || P_ADD.matcher(lower).find()
                || P_SUB.matcher(lower).find();
    }

    public String executeCommand(String input) {
        String lower = input.toLowerCase().trim();
        var m = P_MUL.matcher(lower);

        if (P_TIME.matcher(lower).find())
            return "Сейчас " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        if (P_DATE.matcher(lower).find())
            return "Сегодня " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy, EEEE"));

        if (P_STAT.matcher(lower).find())
            return String.format("Статистика чата:\nВсего сообщений: %d\nТвоих: %d\nМоих: %d",
                    totalMessages, userMessages, botMessages);

        if (m.usePattern(P_MUL).reset(lower).find())
            return formatResult(parseNum(m.group(1)) * parseNum(m.group(2)));

        if (m.usePattern(P_DIV).reset(lower).find()) {
            double b = parseNum(m.group(2));
            return b == 0 ? "На ноль делить нельзя!"
                    : formatResult(parseNum(m.group(1)) / b);
        }

        if (m.usePattern(P_ADD).reset(lower).find())
            return formatResult(parseNum(m.group(1)) + parseNum(m.group(2)));

        if (m.usePattern(P_SUB).reset(lower).find())
            return formatResult(parseNum(m.group(2)) - parseNum(m.group(1)));

        return "Не удалось выполнить команду.";
    }

    // ---------------------------------------------------------------
    // Вспомогательные
    // ---------------------------------------------------------------
    private double parseNum(String s) {
        return Double.parseDouble(s.replace(',', '.'));
    }

    private String formatResult(double result) {
        if (result == Math.floor(result) && !Double.isInfinite(result))
            return "Результат: " + (long) result;
        return "Результат: " + result;
    }

    private String getFallback(String input) {
        if (input.endsWith("?"))   return "Хороший вопрос! Я пока не знаю ответа.";
        if (input.length() < 4)    return "Можешь написать подробнее?";
        return "Интересно... Расскажи мне больше об этом.";
    }
}