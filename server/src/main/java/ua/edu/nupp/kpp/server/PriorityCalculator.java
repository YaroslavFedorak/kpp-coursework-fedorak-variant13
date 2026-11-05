package ua.edu.nupp.kpp.server;

// Розрахунок інтегрального показника пріоритету заявки I = 0,40*F1 + 0,35*F2 + 0,25*F3
public class PriorityCalculator {

    // простій номера 48 год і більше вважається максимальним впливом на доступність
    public static final double MAX_DOWNTIME_HOURS = 48.0;

    // F1 - вплив на гостя: оцінка 0..10 переводиться у шкалу 0..100
    public static double factor1(int guestImpact) {
        if (guestImpact < 0 || guestImpact > 10) {
            throw new IllegalArgumentException("Вплив на гостя оцінюється від 0 до 10");
        }
        return guestImpact * 10.0;
    }

    // F2 - тяжкість несправності: рівень 1..5 -> 0, 25, 50, 75, 100
    public static double factor2(int severity) {
        if (severity < 1 || severity > 5) {
            throw new IllegalArgumentException("Тяжкість несправності має бути від 1 до 5");
        }
        return (severity - 1) * 25.0;
    }

    // F3 - вплив на доступність номера: очікуваний простій у годинах відносно 48 год
    public static double factor3(double downtimeHours) {
        if (downtimeHours < 0) {
            throw new IllegalArgumentException("Тривалість простою не може бути від'ємною");
        }
        return round(Math.min(100.0, downtimeHours / MAX_DOWNTIME_HOURS * 100.0));
    }

    public static double calculate(double f1, double f2, double f3) {
        check(f1);
        check(f2);
        check(f3);
        return round(0.40 * f1 + 0.35 * f2 + 0.25 * f3);
    }

    public static String level(double index) {
        if (index >= 75) {
            return "КРИТИЧНИЙ";
        } else if (index >= 50) {
            return "ВИСОКИЙ";
        } else if (index >= 25) {
            return "СЕРЕДНІЙ";
        }
        return "НИЗЬКИЙ";
    }

    private static void check(double value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Фактор має бути в діапазоні 0..100");
        }
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
