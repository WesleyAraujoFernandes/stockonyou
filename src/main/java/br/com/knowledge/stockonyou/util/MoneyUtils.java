package br.com.knowledge.stockonyou.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyUtils {
    public static final int SCALE = 2;

    public static BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
