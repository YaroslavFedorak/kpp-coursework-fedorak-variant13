package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;

public class WorkRecordService {
    private static final Logger LOG = Logger.getLogger(WorkRecordService.class.getName());

    private final WorkRecordDAO dao;
    private final ServiceRequestDAO requestDAO;
    private final TechnicianDAO technicianDAO;

    public WorkRecordService() {
        this(new WorkRecordDAO(), new ServiceRequestDAO(), new TechnicianDAO());
    }

    WorkRecordService(WorkRecordDAO dao, ServiceRequestDAO requestDAO, TechnicianDAO technicianDAO) {
        this.dao = dao;
        this.requestDAO = requestDAO;
        this.technicianDAO = technicianDAO;
    }

    public List<WorkRecord> getAll() throws SQLException {
        return dao.getAll();
    }

    public void add(WorkRecord w) throws SQLException {
        validate(w);
        ServiceRequest request = requireRequest(w.getRequestId());
        if (request.getState() != RequestState.IN_PROGRESS) {
            throw new IllegalArgumentException("Запис виконання можна додати лише до заявки у стані "
                    + RequestState.IN_PROGRESS.getTitle());
        }
        requireTechnician(w.getTechnicianId());
        dao.add(w);
        LOG.info("Додано запис виконання до заявки id=" + w.getRequestId());
    }

    public void update(WorkRecord w) throws SQLException {
        validate(w);
        require(w.getId());
        requireRequest(w.getRequestId());
        requireTechnician(w.getTechnicianId());
        dao.update(w);
        LOG.info("Оновлено запис виконання id=" + w.getId());
    }

    public void delete(long id) throws SQLException {
        WorkRecord w = require(id);
        ServiceRequest request = requestDAO.getById(w.getRequestId());
        if (request != null && request.getState() == RequestState.DONE && dao.countByRequest(request.getId()) <= 1) {
            throw new IllegalArgumentException("Не можна видалити єдиний запис виконання виконаної заявки");
        }
        dao.delete(id);
        LOG.info("Видалено запис виконання id=" + id);
    }

    static void validate(WorkRecord w) {
        if (w == null || w.getRequestId() <= 0 || w.getTechnicianId() <= 0) {
            throw new IllegalArgumentException("Потрібно вибрати заявку та фахівця");
        }
        if (w.getDescription() == null || w.getDescription().isBlank()) {
            throw new IllegalArgumentException("Опис виконаних робіт є обов'язковим");
        }
        if (w.getHoursSpent() <= 0 || w.getHoursSpent() > 24) {
            throw new IllegalArgumentException("Витрачений час має бути більше 0 і не більше 24 год");
        }
        LocalDate date;
        try {
            date = LocalDate.parse(w.getWorkDate() == null ? "" : w.getWorkDate());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Дата виконання має бути у форматі РРРР-ММ-ДД");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата виконання не може бути в майбутньому");
        }
    }

    private WorkRecord require(Long id) throws SQLException {
        WorkRecord w = id == null ? null : dao.getById(id);
        if (w == null) {
            throw new IllegalArgumentException("Запис виконання не знайдено");
        }
        return w;
    }

    private ServiceRequest requireRequest(long requestId) throws SQLException {
        ServiceRequest r = requestDAO.getById(requestId);
        if (r == null) {
            throw new IllegalArgumentException("Заявку не знайдено");
        }
        return r;
    }

    private void requireTechnician(long technicianId) throws SQLException {
        if (technicianDAO.getById(technicianId) == null) {
            throw new IllegalArgumentException("Фахівця не знайдено");
        }
    }
}
