package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;

public class WorkRecordServlet extends BaseJsonServlet {

    private final transient WorkRecordService service = new WorkRecordService();

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

    private WorkRecord read(HttpServletRequest req, Long id) {
        return new WorkRecord(id,
                longParam(req, "requestId", "Заявка"),
                longParam(req, "technicianId", "Фахівець"),
                text(req, "workDate"),
                text(req, "description"),
                doubleParam(req, "hoursSpent", "Витрачено, год"));
    }
}
