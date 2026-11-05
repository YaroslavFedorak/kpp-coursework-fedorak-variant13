package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    @Test
    void roomWithoutNumberIsRejected() {
        Room room = new Room(null, "", 1, "Стандарт", 2, "Телевізор");
        assertThrows(IllegalArgumentException.class, () -> RoomService.validate(room));
    }

    @Test
    void technicianWithWrongPhoneIsRejected() {
        Technician t = new Technician(null, "Іваненко Іван", "Електрик", "телефон");
        assertThrows(IllegalArgumentException.class, () -> TechnicianService.validate(t));
    }

    @Test
    void requestWithGuestImpactAbove10IsRejected() {
        ServiceRequest r = new ServiceRequest();
        r.setRoomId(1);
        r.setDescription("Не працює телевізор");
        r.setGuestImpact(11);
        r.setSeverity(2);
        assertThrows(IllegalArgumentException.class, () -> ServiceRequestService.validate(r));
    }

    @Test
    void factorsAreCalculatedForRequest() {
        ServiceRequest r = new ServiceRequest();
        r.setGuestImpact(8);
        r.setSeverity(4);
        r.setDowntimeHours(12);

        ServiceRequestService.applyFactors(r);

        assertEquals(80.0, r.getF1());
        assertEquals(75.0, r.getF2());
        assertEquals(25.0, r.getF3());
        assertEquals(64.5, r.getPriorityIndex());
    }

    @Test
    void workRecordWithZeroHoursIsRejected() {
        WorkRecord w = new WorkRecord(null, 3, 1, "2026-10-29", "Заміна вимикача", 0);
        assertThrows(IllegalArgumentException.class, () -> WorkRecordService.validate(w));
    }

    @Test
    void workRecordWithWrongDateIsRejected() {
        WorkRecord w = new WorkRecord(null, 3, 1, "29.10.2026", "Заміна вимикача", 1.5);
        assertThrows(IllegalArgumentException.class, () -> WorkRecordService.validate(w));
    }
}
