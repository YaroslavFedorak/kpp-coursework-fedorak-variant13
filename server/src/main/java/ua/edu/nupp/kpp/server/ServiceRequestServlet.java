package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;

public class ServiceRequestServlet extends BaseJsonServlet {

    private final transient ServiceRequestService service = new ServiceRequestService();

    @Override
    protected Object list() throws Exception {
        return service.getAll();
    }

    @Override
    protected void handle(String action, HttpServletRequest req) throws Exception {
        switch (action) {
            case "add" -> service.add(read(req, null));
            case "update" -> service.update(read(req, longParam(req, "id", "ID")));
            case "transition" -> service.changeState(longParam(req, "id", "ID"), state(req),
                    optionalLong(req, "technicianId", "Фахівець"));
            case "delete" -> service.delete(longParam(req, "id", "ID"));
            default -> throw unknownAction();
        }
    }

    private ServiceRequest read(HttpServletRequest req, Long id) {
        ServiceRequest r = new ServiceRequest();
        r.setId(id);
        r.setRoomId(longParam(req, "roomId", "Номер"));
        r.setDescription(text(req, "description"));
        r.setGuestImpact(intParam(req, "guestImpact", "Вплив на гостя"));
        r.setSeverity(intParam(req, "severity", "Тяжкість"));
        r.setDowntimeHours(doubleParam(req, "downtimeHours", "Простій, год"));
        return r;
    }

    private RequestState state(HttpServletRequest req) {
        String value = text(req, "state");
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Не задано новий стан заявки");
        }
        try {
            return RequestState.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Невідомий стан заявки: " + value);
        }
    }
}
