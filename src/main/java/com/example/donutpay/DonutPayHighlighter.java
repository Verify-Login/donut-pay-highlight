package com.example.donutpay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DonutPayHighlighter implements ClientModInitializer {

    // Minimum payment that triggers the recolor.
    private static final double THRESHOLD = 3_000_000;

    // Adjust this if DonutSMP's pay message format differs.
    // Matches e.g. "Steve paid you $3.5M", "Steve paid you $3,500,000", "$500K"
    private static final Pattern PAY_PATTERN = Pattern.compile(
            "(?i)\\b\\w{3,16}\\b\\s+(?:has\\s+)?paid\\s+you\\s+\\$?\\s*([\\d,]+(?:\\.\\d+)?)\\s*([kmbt])?");

    // Gradient runs from START_COLOR (left) to END_COLOR (right).
    // Pure black is hard to see on chat's dark background; try 0x331A00 for a softer start.
    private static final int START_COLOR = 0x000000;
    private static final int END_COLOR = 0xFF8C00;

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> {
            if (overlay) return message; // ignore action bar

            String raw = message.getString();
            Matcher m = PAY_PATTERN.matcher(raw);
            if (!m.find()) return message;

            double amount = parseAmount(m.group(1), m.group(2));
            if (amount <= THRESHOLD) return message;

            return recolor(raw);
        });
    }

    private static double parseAmount(String number, String suffix) {
        double value = Double.parseDouble(number.replace(",", ""));
        if (suffix == null) return value;
        return switch (suffix.toLowerCase()) {
            case "k" -> value * 1_000;
            case "m" -> value * 1_000_000;
            case "b" -> value * 1_000_000_000;
            case "t" -> value * 1_000_000_000_000.0;
            default -> value;
        };
    }

    // Fades each character from START_COLOR to END_COLOR across the message.
    private static Text recolor(String raw) {
        MutableText result = Text.empty();
        int n = raw.length();
        for (int i = 0; i < n; i++) {
            float t = n > 1 ? (float) i / (n - 1) : 1f;
            int rgb = lerp(START_COLOR, END_COLOR, t);
            result.append(Text.literal(String.valueOf(raw.charAt(i)))
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb))));
        }
        return result;
    }

    private static int lerp(int a, int b, float t) {
        int r = Math.round(((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
        int g = Math.round(((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
        int bl = Math.round((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
