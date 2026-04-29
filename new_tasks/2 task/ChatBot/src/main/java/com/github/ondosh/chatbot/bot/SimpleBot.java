package com.github.ondosh.chatbot.bot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Обрабатывает два типа входящих сообщений:
 * — «фразы» (приветствия, прощания, вопросы о боте и т.д.) с заранее заготовленными ответами;
 * — «команды» (время, дата, арифметика, статистика) с вычисляемым результатом.
 * Также ведёт счётчики сообщений для команды «статистика».
 */
public class SimpleBot implements IBot {

    // ---------------------------------------------------------------
    // Статистика сообщений
    // ---------------------------------------------------------------

    /** Общее количество сообщений в чате (пользователь + бот). */
    private static int totalMessages = 0;

    /** Количество сообщений от пользователя. */
    private static int userMessages  = 0;

    /** Количество сообщений от бота.*/
    private static int botMessages   = 0;

    /** Вызывается из ChatController при каждом сообщении пользователя. */
    public void countUserMessage() { userMessages++; totalMessages++; }

    /** Вызывается из ChatController при каждом ответе бота. */
    public void countBotMessage()  { botMessages++;  totalMessages++; }

    public int getTotalMessages() { return totalMessages; }
    public int getUserMessages() { return userMessages; }
    public int getBotMessages() { return botMessages; }

    public void setStats(int total, int user, int bot) {
        totalMessages = total;
        userMessages = user;
        botMessages = bot;
    }
    // ---------------------------------------------------------------
    // Фразовые паттерны — заготовленные ответы на типовые реплики
    // Ключ — скомпилированный регулярный Pattern, значение — текст ответа
    // ---------------------------------------------------------------
    private static final Map<Pattern, String> PHRASE_PATTERNS = new LinkedHashMap<>();

    static {
        // Приветствия
        PHRASE_PATTERNS.put(re("привет|здравствуй|хай|добрый\\s+день|добрый\\s+вечер|доброе\\s+утро"),
                "Привет! Если хочешь узнать команды, то напиши \"помоги\". ");

        // Прощания
        PHRASE_PATTERNS.put(re("пока|до\\s+свидания|увидимся|до\\s+встречи"),
                "Пока! Было приятно пообщаться.");

        // Вопросы об имени бота
        PHRASE_PATTERNS.put(re("как\\s+тебя\\s+зовут|твоё\\s+имя|твое\\s+имя"),
                "Меня зовут ChatBot. А тебя?");

        // Вопросы о природе бота
        PHRASE_PATTERNS.put(re("кто\\s+ты|что\\s+ты\\s+такое"),
                "Я чат-бот, написанный на Java.");

        // Вопросы о состоянии бота
        PHRASE_PATTERNS.put(re("как\\s+(ты|дела|жизнь|поживаешь)"),
                "Всё отлично, спасибо что спросил!");

        // Благодарности
        PHRASE_PATTERNS.put(re("спасибо|благодарю|благодарен"),
                "Пожалуйста! Обращайся.");

        // Справка — объединяет «что умеешь», «помоги», «команды».
        // Показывает полный список доступных возможностей бота.
        PHRASE_PATTERNS.put(re("что\\s+ты\\s+умеешь|что\\s+умеешь|список\\s+команд|команды|помощь|помоги|помогите"),
                """
                        Вот что я умею:
                        • «который час» / «время» — текущее время
                        • «какая дата» / «какой день» — сегодняшняя дата
                        • «умножь X на Y» — умножение
                        • «раздели X на Y» — деление
                        • «сложи X и Y» — сложение
                        • «вычти X из Y» — вычитание
                        • «статистика» — сколько сообщений в чате
                        • Простые фразы: привет, пока, как дела и т.д.
                        • Отвечаю на любые вопросы, обращаясь к нейросети.""");
    }

    /**
     * Вспомогательный метод для компиляции паттернов.
     * Флаги CASE_INSENSITIVE и UNICODE_CASE обеспечивают регистронезависимость для кириллицы.
     *
     * @param regex строка регулярного выражения
     * @return скомпилированный Pattern
     */
    private static Pattern re(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    // ---------------------------------------------------------------
    // Командные паттерны — вычисляемые ответы
    // ---------------------------------------------------------------

    /** Запрос текущего времени. */
    private static final Pattern P_TIME = Pattern.compile(
            "который час|сколько времени|какое время|время", Pattern.CASE_INSENSITIVE);

    /** Запрос текущей даты. */
    private static final Pattern P_DATE = Pattern.compile(
            "какая дата|какой день|какое сегодня", Pattern.CASE_INSENSITIVE);

    /** Запрос статистики сообщений. */
    private static final Pattern P_STAT = Pattern.compile(
            "статистика|сколько сообщений", Pattern.CASE_INSENSITIVE);

    /** Умножение: «умножь X на Y». Группы 1 и 2 — операнды. */
    private static final Pattern P_MUL  = Pattern.compile(
            "умножь\\s+(-?[\\d.,]+)\\s+на\\s+(-?[\\d.,]+)", Pattern.CASE_INSENSITIVE);

    /** Деление: «раздели X на Y». Группы 1 и 2 — операнды. */
    private static final Pattern P_DIV  = Pattern.compile(
            "раздели\\s+(-?[\\d.,]+)\\s+на\\s+(-?[\\d.,]+)", Pattern.CASE_INSENSITIVE);

    /** Сложение: «сложи X и Y». Группы 1 и 2 — операнды. */
    private static final Pattern P_ADD  = Pattern.compile(
            "сложи\\s+(-?[\\d.,]+)\\s+и\\s+(-?[\\d.,]+)", Pattern.CASE_INSENSITIVE);

    /**
     * Вычитание: «вычти X из Y».
     * Группа 1 — вычитаемое (X), группа 2 — уменьшаемое (Y).
     * Результат вычисляется как group(2) - group(1).
     */
    private static final Pattern P_SUB  = Pattern.compile(
            "вычти\\s+(-?[\\d.,]+)\\s+из\\s+(-?[\\d.,]+)", Pattern.CASE_INSENSITIVE);

    /** Сложение в виде выражения: «2+2». Группы 1 и 2 — операнды. */
    private static final Pattern P_EXPR_ADD = Pattern.compile(
            "(-?[\\d.,]+)\\s*\\+\\s*(-?[\\d.,]+)");

    /** Вычитание в виде выражения: «2-2». Группы 1 и 2 — операнды. */
    private static final Pattern P_EXPR_SUB = Pattern.compile(
            "(-?[\\d.,]+)\\s*-\\s*(-?[\\d.,]+)");

    /** Умножение в виде выражения: «2*2». Группы 1 и 2 — операнды. */
    private static final Pattern P_EXPR_MUL = Pattern.compile(
            "(-?[\\d.,]+)\\s*[*×]\\s*(-?[\\d.,]+)");

    /** Деление в виде выражения: «2/2». Группы 1 и 2 — операнды. */
    private static final Pattern P_EXPR_DIV = Pattern.compile(
            "(-?[\\d.,]+)\\s*[/÷]\\s*(-?[\\d.,]+)");
    // ---------------------------------------------------------------
    // Публичный API
    // ---------------------------------------------------------------


    /**
     * Проверяет, является ли сообщение простой фразой, и если да — возвращает ответ.
     * <p>
     * Фраза считается простой только если вся значимая часть сообщения покрывается
     * паттерном — остаток после совпадения не длиннее порога (5 символов).
     * Это защищает от ложных срабатываний вроде «привет, объясни квантовую физику»,
     * где слово «привет» есть, но смысл сообщения требует нейросети.
     *
     * @param input входное сообщение пользователя
     * @return текст ответа если фраза распознана, иначе {@code null}
     */
    public String tryPhrase(String input) {
        String lower = input.toLowerCase().trim();
        for (Map.Entry<Pattern, String> entry : PHRASE_PATTERNS.entrySet()) {
            var m = entry.getKey().matcher(lower);
            if (m.find()) {
                String leftover = (lower.substring(0, m.start()) + lower.substring(m.end()))
                        .replaceAll("[\\s,!?.]+", "");
                if (leftover.length() <= 5) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Определяет, является ли сообщение командой с вычисляемым результатом
     * (время, дата, арифметика, статистика).
     *
     * @param input входное сообщение пользователя
     * @return true, если сообщение соответствует одному из командных паттернов
     */
    public boolean isCommand(String input) {
        String lower = input.toLowerCase().trim();
        return P_TIME.matcher(lower).find()
                || P_DATE.matcher(lower).find()
                || P_STAT.matcher(lower).find()
                || P_MUL.matcher(lower).find()
                || P_DIV.matcher(lower).find()
                || P_ADD.matcher(lower).find()
                || P_SUB.matcher(lower).find()
                || P_EXPR_ADD.matcher(input).find()
                || P_EXPR_SUB.matcher(input).find()
                || P_EXPR_MUL.matcher(input).find()
                || P_EXPR_DIV.matcher(input).find();
    }

    /**
     * Выполняет команду и возвращает результат.
     * Проверка идёт в порядке: время → дата → статистика → арифметика.
     * Для арифметики один {@link java.util.regex.Matcher} переиспользуется
     * через {@code usePattern/reset}, чтобы не создавать лишние объекты.
     *
     * @param input входное сообщение пользователя
     * @return строка с результатом команды
     */
    public String executeCommand(String input) {
        String lower = input.toLowerCase().trim();

        var m = P_MUL.matcher(lower);
        var me = P_EXPR_ADD.matcher(input);

        if (me.find())
            return formatResult(parseNum(me.group(1)) + parseNum(me.group(2)));

        if (me.usePattern(P_EXPR_SUB).reset(input).find())
            return formatResult(parseNum(me.group(1)) - parseNum(me.group(2)));

        if (me.usePattern(P_EXPR_MUL).reset(input).find())
            return formatResult(parseNum(me.group(1)) * parseNum(me.group(2)));

        if (me.usePattern(P_EXPR_DIV).reset(input).find()) {
            double b = parseNum(me.group(2));
            return b == 0 ? "На ноль делить нельзя!"
                    : formatResult(parseNum(me.group(1)) / b);
        }

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

        // group(1) — вычитаемое, group(2) — уменьшаемое (см. P_SUB)
        if (m.usePattern(P_SUB).reset(lower).find())
            return formatResult(parseNum(m.group(2)) - parseNum(m.group(1)));

        return "Не удалось выполнить команду.";
    }

    // ---------------------------------------------------------------
    // Вспомогательные методы
    // ---------------------------------------------------------------

    /**
     * Парсит строку в число, заменяя запятую на точку для поддержки обоих форматов.
     * Например, «12,5» и «12.5» оба корректны.
     *
     * @param s строка с числом
     * @return числовое значение
     */
    private double parseNum(String s) {
        return Double.parseDouble(s.replace(',', '.'));
    }

    /**
     * Форматирует результат арифметической операции.
     * Если результат целый — выводит без дробной части, иначе — с десятичными знаками.
     *
     * @param result результат вычисления
     * @return отформатированная строка
     */
    private String formatResult(double result) {
        if (result == Math.floor(result) && !Double.isInfinite(result))
            return "Результат: " + (long) result;
        return "Результат: " + result;
    }

    /**
     * Возвращает ответ на сообщение пользователя.
     * Сначала проверяет команды, затем фразы.
     * Если ничего не совпало — возвращает fallback-ответ.
     *
     * @param input входное сообщение пользователя
     * @return текст ответа
     */
    @Override
    public String getResponse(String input) {
        if (isCommand(input)) {
            return executeCommand(input);
        }

        String phrase = tryPhrase(input);
        if (phrase != null) {
            return phrase;
        }

        return "Я не понимаю. Напиши \"помоги\" чтобы узнать что я умею.";
    }

    /** Идентификатор бота. */
    @Override
    public String getBotName() {
        return "SimpleBot";
    }

    /**
     * SimpleBot работает локально и всегда доступен.
     */
    @Override
    public boolean isAvailable() {
        return true;
    }

}