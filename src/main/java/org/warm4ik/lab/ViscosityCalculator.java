package org.warm4ik.lab;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ViscosityCalculator {

    public List<Double> generateValues(double min, double max, double step) {
        List<Double> values = new ArrayList<>();
        BigDecimal current = BigDecimal.valueOf(min);
        BigDecimal end = BigDecimal.valueOf(max);
        BigDecimal stepBD = BigDecimal.valueOf(step);
        int scale = 10;
        while (current.compareTo(end) <= 0) {
            values.add(current.doubleValue());
            current = current.add(stepBD).setScale(scale, RoundingMode.HALF_UP);
        }
        return values;
    }

    /**
     * метод рассчитывает вязкость по заданной формуле.
     * @param cb - концентрация
     * @param t  - температура
     */
    public double calculateViscosity(double cb, double t) {
        double x = cb / (1900 - 18 * cb);
        double lgEta = 22.46 * x - 0.114 + ((30 - t) / (91 + t)) * (1.1 + 43.1 * Math.pow(x, 1.25));
        return Math.pow(10, lgEta);
    }

    /** метод рассчитывет процентную ошибку между экспериментальным и рассчитанным значением. */
    public double calculatePercentageError(double experimental, double calculated) {
        return Math.abs((experimental - calculated) / experimental) * 100;
    }

    public String formatValue(double value) {
        DecimalFormat df = new DecimalFormat("0.000");
        return df.format(value);
    }
}
