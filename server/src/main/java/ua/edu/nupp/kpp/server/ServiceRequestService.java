package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

public class ServiceRequestService {
    private static final Logger LOG = Logger.getLogger(ServiceRequestService.class.getName());

    private final ServiceRequestDAO dao;
    private final RoomDAO roomDAO;
    private final TechnicianDAO technicianDAO;
    private final WorkRecordDAO workRecordDAO;

    public ServiceRequestService() {
        this(new ServiceRequestDAO(), new RoomDAO(), new TechnicianDAO(), new WorkRecordDAO());
    }

    ServiceRequestService(ServiceRequestDAO dao, RoomDAO roomDAO, TechnicianDAO technicianDAO,
                          WorkRecordDAO workRecordDAO) {
        this.dao = dao;
        this.roomDAO = roomDAO;
        this.technicianDAO = technicianDAO;
        this.workRecordDAO = workRecordDAO;
    }

    public List<ServiceRequest> getAll() throws SQLException {
        return dao.getAll();
    }

    public void add(ServiceRequest r) throws SQLException {
        validate(r);
        requireRoom(r.getRoomId());
        r.setState(RequestState.NEW);
        r.setTechnicianId(null);
        r.setCreatedDate(LocalDate.now().toString());
        applyFactors(r);
        dao.add(r);
        LOG.info("Створено заявку, I = " + r.getPriorityIndex());
    }

    // редагуються дані заявки, а стан, фахівець і дата створення залишаються попередніми
    public void update(ServiceRequest r) throws SQLException {
        validate(r);
        ServiceRequest old = require(r.getId());
        requireRoom(r.getRoomId());
        r.setState(old.getState());
        r.setTechnicianId(old.getTechnicianId());
        r.setCreatedDate(old.getCreatedDate());
        applyFactors(r);
        dao.update(r);
        LOG.info("Оновлено заявку id=" + r.getId() + ", I = " + r.getPriorityIndex());
    }

    public void changeState(long id, RequestState next, Long technicianId) throws SQLException {
        if (next == null) {
            throw new IllegalArgumentException("Не задано новий стан заявки");
        }
        ServiceRequest current = require(id);
        if (!isAllowedTransition(current.getState(), next)) {
            throw new IllegalArgumentException("Недопустимий перехід стану: "
                    + current.getState().getTitle() + " → " + next.getTitle());
        }
        Long technician = current.getTechnicianId();
        if (next == RequestState.ASSIGNED) {
            if (technicianId == null) {
                throw new IllegalArgumentException("Для призначення заявки потрібно вибрати фахівця");
            }
            if (technicianDAO.getById(technicianId) == null) {
                throw new IllegalArgumentException("Фахівця не знайдено");
            }
            technician = technicianId;
        }
        if (next == RequestState.DONE && workRecordDAO.countByRequest(id) == 0) {
            throw new IllegalArgumentException("Заявку не можна позначити виконаною без жодного запису виконання");
        }
        dao.updateState(id, next, technician);
        LOG.info("Змінено стан заявки id=" + id + ": " + current.getState() + " -> " + next);
    }

    public void delete(long id) throws SQLException {
        require(id);
        if (workRecordDAO.countByRequest(id) > 0) {
            throw new IllegalArgumentException("Спочатку видаліть записи виконання цієї заявки");
        }
        dao.delete(id);
        LOG.info("Видалено заявку id=" + id);
    }

    public static boolean isAllowedTransition(RequestState from, RequestState to) {
        return (from == RequestState.NEW && to == RequestState.ASSIGNED)
                || (from == RequestState.ASSIGNED && to == RequestState.IN_PROGRESS)
                || (from == RequestState.IN_PROGRESS && to == RequestState.DONE);
    }

    static void applyFactors(ServiceRequest r) {
        double f1 = PriorityCalculator.factor1(r.getGuestImpact());
        double f2 = PriorityCalculator.factor2(r.getSeverity());
        double f3 = PriorityCalculator.factor3(r.getDowntimeHours());
        r.setF1(f1);
        r.setF2(f2);
        r.setF3(f3);
        r.setPriorityIndex(PriorityCalculator.calculate(f1, f2, f3));
    }

    static void validate(ServiceRequest r) {
        if (r == null || r.getRoomId() <= 0) {
            throw new IllegalArgumentException("Не вибрано номер для заявки");
        }
        if (r.getDescription() == null || r.getDescription().isBlank()) {
            throw new IllegalArgumentException("Опис несправності є обов'язковим");
        }
        if (r.getGuestImpact() < 0 || r.getGuestImpact() > 10) {
            throw new IllegalArgumentException("Вплив на гостя оцінюється від 0 до 10");
        }
        if (r.getSeverity() < 1 || r.getSeverity() > 5) {
            throw new IllegalArgumentException("Тяжкість несправності має бути від 1 до 5");
        }
        if (r.getDowntimeHours() < 0 || r.getDowntimeHours() > 720) {
            throw new IllegalArgumentException("Очікуваний простій має бути від 0 до 720 год");
        }
    }

    private ServiceRequest require(Long id) throws SQLException {
        ServiceRequest r = id == null ? null : dao.getById(id);
        if (r == null) {
            throw new IllegalArgumentException("Заявку не знайдено");
        }
        return r;
    }

    private void requireRoom(long roomId) throws SQLException {
        if (roomDAO.getById(roomId) == null) {
            throw new IllegalArgumentException("Номер не знайдено");
        }
    }
}
