package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class TechnicianService {
    private static final Logger LOG = Logger.getLogger(TechnicianService.class.getName());

    private final TechnicianDAO dao;
    private final ServiceRequestDAO requestDAO;
    private final WorkRecordDAO workRecordDAO;

    public TechnicianService() {
        this(new TechnicianDAO(), new ServiceRequestDAO(), new WorkRecordDAO());
    }

    TechnicianService(TechnicianDAO dao, ServiceRequestDAO requestDAO, WorkRecordDAO workRecordDAO) {
        this.dao = dao;
        this.requestDAO = requestDAO;
        this.workRecordDAO = workRecordDAO;
    }

    public List<Technician> getAll() throws SQLException {
        return dao.getAll();
    }

    public void add(Technician t) throws SQLException {
        validate(t);
        dao.add(t);
        LOG.info("Додано фахівця " + t.getFullName());
    }

    public void update(Technician t) throws SQLException {
        validate(t);
        require(t.getId());
        dao.update(t);
        LOG.info("Оновлено фахівця " + t.getFullName());
    }

    public void delete(long id) throws SQLException {
        Technician t = require(id);
        if (requestDAO.countByTechnician(id) > 0 || workRecordDAO.countByTechnician(id) > 0) {
            throw new IllegalArgumentException("Неможливо видалити фахівця: за ним є заявки або записи виконання");
        }
        dao.delete(id);
        LOG.info("Видалено фахівця " + t.getFullName());
    }

    static void validate(Technician t) {
        if (t == null || blank(t.getFullName()) || blank(t.getSpecialization()) || blank(t.getPhone())) {
            throw new IllegalArgumentException("ПІБ, спеціалізація та телефон фахівця є обов'язковими");
        }
        if (!t.getPhone().matches("\\+?[0-9()\\- ]{7,20}")) {
            throw new IllegalArgumentException("Некоректний номер телефону");
        }
    }

    private Technician require(Long id) throws SQLException {
        Technician t = id == null ? null : dao.getById(id);
        if (t == null) {
            throw new IllegalArgumentException("Фахівця не знайдено");
        }
        return t;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
