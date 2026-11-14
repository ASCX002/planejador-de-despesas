package view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class Money {
    private static final NumberFormat FORMAT = NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("pt-BR"));

    private Money() {
    }

    public static long parseCents(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Informe um valor em reais.");
        }
        String normalized = input.trim().replace("R$", "").replaceAll("\\s", "");
        if (normalized.contains(",") && normalized.contains(".")) {
            normalized = normalized.replace(".", "").replace(',', '.');
        } else if (normalized.contains(",")) {
            normalized = normalized.replace(',', '.');
        }
        try {
            return new BigDecimal(normalized).setScale(2, RoundingMode.UNNECESSARY)
                    .movePointRight(2).longValueExact();
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Valor inválido. Use, por exemplo, 125,50.", exception);
        }
    }

    public static String format(long cents) {
        return FORMAT.format(cents / 100.0);
    }

    public static String editValue(long cents) {
        return BigDecimal.valueOf(cents, 2).toPlainString().replace('.', ',');
    }
}
