package me.ihqqq.spring_blog.util;

public final class ReadingTimeUtils {

    private ReadingTimeUtils() {}

    private static final int WORDS_PER_MINUTE = 200;

    public static int calculate(String content) {
        if (content == null || content.isBlank()) return 1;

        String plainText = content
                .replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (plainText.isEmpty()) return 1;

        int wordCount = plainText.split("\\s+").length;
        int minutes = (int) Math.ceil((double) wordCount / WORDS_PER_MINUTE);

        return Math.max(1, minutes);
    }
}