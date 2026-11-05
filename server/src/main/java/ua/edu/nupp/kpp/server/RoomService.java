package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class RoomService {
    private static final Logger LOG = Logger.getLogger(RoomService.class.getName());

    private final RoomDAO dao;
    private final ServiceRequestDAO requestDAO;

    public RoomService() {
        this(new RoomDAO(), new ServiceRequestDAO());
    }

    RoomService(RoomDAO dao, ServiceRequestDAO requestDAO) {
        this.dao = dao;
        this.requestDAO = requestDAO;
    }

    public List<Room> getAll() throws SQLException {
        return dao.getAll();
    }

    public void add(Room r) throws SQLException {
        validate(r);
        if (dao.getByNumber(r.getNumber()) != null) {
            throw new IllegalArgumentException("Номер «" + r.getNumber() + "» уже існує");
        }
        dao.add(r);
        LOG.info("Додано номер " + r.getNumber());
    }

    public void update(Room r) throws SQLException {
        validate(r);
        require(r.getId());
        Room sameNumber = dao.getByNumber(r.getNumber());
        if (sameNumber != null && !sameNumber.getId().equals(r.getId())) {
            throw new IllegalArgumentException("Номер «" + r.getNumber() + "» уже існує");
        }
        dao.update(r);
        LOG.info("Оновлено номер " + r.getNumber());
    }

    public void delete(long id) throws SQLException {
        Room r = require(id);
        if (requestDAO.countByRoom(id) > 0) {
            throw new IllegalArgumentException("Неможливо видалити номер " + r.getNumber() + ": для нього є заявки");
        }
        dao.delete(id);
        LOG.info("Видалено номер " + r.getNumber());
    }

    static void validate(Room r) {
        if (r == null || blank(r.getNumber()) || blank(r.getCategory()) || blank(r.getEquipment())) {
            throw new IllegalArgumentException("Номер кімнати, категорія та обладнання є обов'язковими");
        }
        if (r.getNumber().length() > 10) {
            throw new IllegalArgumentException("Номер кімнати не може бути довшим за 10 символів");
        }
        if (r.getFloor() < 0 || r.getFloor() > 50) {
            throw new IllegalArgumentException("Поверх має бути в межах 0..50");
        }
        if (r.getCapacity() < 1 || r.getCapacity() > 10) {
            throw new IllegalArgumentException("Місткість номера має бути від 1 до 10 осіб");
        }
    }

    private Room require(Long id) throws SQLException {
        Room r = id == null ? null : dao.getById(id);
        if (r == null) {
            throw new IllegalArgumentException("Номер не знайдено");
        }
        return r;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
