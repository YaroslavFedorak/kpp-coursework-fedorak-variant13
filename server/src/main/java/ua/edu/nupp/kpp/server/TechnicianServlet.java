package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;

public class TechnicianServlet extends BaseJsonServlet {

    private final transient TechnicianService service = new TechnicianService();

    @Override
    protected Object list() throws Exception {
        return service.getAll();
    }

    @Override
    protected void handle(String action, HttpServletRequest req) throws Exception {
        switch (action) {
            case "add" -> service.add(read(req, null));
            case "update" -> service.update(read(req, longParam(req, "id", "ID")));
            case "delete" -> service.delete(longParam(req, "id", "ID"));
            default -> throw unknownAction();
        }
    }

    private Technician read(HttpServletRequest req, Long id) {
        return new Technician(id, text(req, "fullName"), text(req, "specialization"), text(req, "phone"));
    }
}
