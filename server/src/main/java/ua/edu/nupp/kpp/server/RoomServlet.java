package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;

public class RoomServlet extends BaseJsonServlet {

    private final transient RoomService service = new RoomService();

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

    private Room read(HttpServletRequest req, Long id) {
        return new Room(id,
                text(req, "number"),
                intParam(req, "floor", "Поверх"),
                text(req, "category"),
                intParam(req, "capacity", "Місткість"),
                text(req, "equipment"));
    }
}
