package ua.edu.nupp.kpp.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomCsvParserTest {

    @Test
    void correctLineIsParsed() {
        Room r = RoomCsvParser.parse("206;2;Напівлюкс;3;Кондиціонер, телевізор, міні-бар, сейф");
        assertEquals("206", r.getNumber());
        assertEquals(2, r.getFloor());
        assertEquals("Напівлюкс", r.getCategory());
        assertEquals(3, r.getCapacity());
        assertEquals("Кондиціонер, телевізор, міні-бар, сейф", r.getEquipment());
    }

    @Test
    void notNumericFloorIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RoomCsvParser.parse("105;перший;Стандарт;2;Телевізор"));
    }

    @Test
    void lineWithWrongColumnCountIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RoomCsvParser.parse("105;1;Стандарт"));
    }

    @Test
    void headerIsRecognized() {
        assertTrue(RoomCsvParser.isValidHeader("﻿room_number;floor;category;capacity;equipment"));
        assertFalse(RoomCsvParser.isValidHeader("number;floor"));
    }
}
