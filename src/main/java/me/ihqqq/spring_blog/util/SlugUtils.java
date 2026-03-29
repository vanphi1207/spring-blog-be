package me.ihqqq.spring_blog.util;

import java.text.Normalizer;



public final class SlugUtils {

    private SlugUtils() {}

    public static String toSlug(String input) {
        if(input == null || input.isBlank()) return "";

        String result = input.replace('đ', 'd').replace('Đ', 'D');
        String normalized = Normalizer.normalize(result, Normalizer.Form.NFD);

        return normalized
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");

    }

}
