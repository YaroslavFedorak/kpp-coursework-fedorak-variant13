package ua.edu.nupp.kpp.client;

// Розбір одного рядка CSV з переліком номерів та обладнання
public class RoomCsvParser {

    public static final String HEADER = "room_number;floor;category;capacity;equipment";

    public static boolean isValidHeader(String line) {
        if (line == null) {
            return false;
        }
        String clean = line.replace("﻿", "").trim();
        return clean.equalsIgnoreCase(HEADER);
    }

    public static Room parse(String line) {
        String[] p = line.split(";", -1);
        if (p.length != 5) {
            throw new IllegalArgumentException("очікується 5 полів, а знайдено " + p.length);
        }
        String number = p[0].trim();
        String category = p[2].trim();
        String equipment = p[4].trim();
        if (number.isEmpty()) {
            throw new IllegalArgumentException("не вказано номер кімнати");
        }
        if (category.isEmpty() || equipment.isEmpty()) {
            throw new IllegalArgumentException("категорія та обладнання є обов'язковими");
        }
        int floor = toInt(p[1], "поверх");
        int capacity = toInt(p[3], "місткість");
        return new Room(null, number, floor, category, capacity, equipment);
    }

    private static int toInt(String value, String field) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("поле «" + field + "» має бути цілим числом: " + value.trim());
        }
    }
}
