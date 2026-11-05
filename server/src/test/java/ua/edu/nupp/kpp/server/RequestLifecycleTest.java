package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestLifecycleTest {

    @Test
    void newToAssignedAllowed() {
        assertTrue(ServiceRequestService.isAllowedTransition(RequestState.NEW, RequestState.ASSIGNED));
    }

    @Test
    void assignedToInProgressAllowed() {
        assertTrue(ServiceRequestService.isAllowedTransition(RequestState.ASSIGNED, RequestState.IN_PROGRESS));
    }

    @Test
    void inProgressToDoneAllowed() {
        assertTrue(ServiceRequestService.isAllowedTransition(RequestState.IN_PROGRESS, RequestState.DONE));
    }

    @Test
    void skippingStatesIsRejected() {
        assertFalse(ServiceRequestService.isAllowedTransition(RequestState.NEW, RequestState.DONE));
        assertFalse(ServiceRequestService.isAllowedTransition(RequestState.NEW, RequestState.IN_PROGRESS));
    }

    @Test
    void backwardTransitionIsRejected() {
        assertFalse(ServiceRequestService.isAllowedTransition(RequestState.DONE, RequestState.NEW));
        assertFalse(ServiceRequestService.isAllowedTransition(RequestState.IN_PROGRESS, RequestState.ASSIGNED));
    }

    @Test
    void assignWithoutTechnicianIsRejected() {
        FakeRequestDAO requests = new FakeRequestDAO();
        requests.put(request(1L, RequestState.NEW));
        ServiceRequestService service = new ServiceRequestService(requests, new RoomDAO(),
                new FakeTechnicianDAO(), new FakeWorkRecordDAO(0));

        assertThrows(IllegalArgumentException.class, () -> service.changeState(1, RequestState.ASSIGNED, null));
        assertEquals(RequestState.NEW, requests.data.get(1L).getState());
    }

    @Test
    void assignWithTechnicianChangesState() throws Exception {
        FakeRequestDAO requests = new FakeRequestDAO();
        requests.put(request(1L, RequestState.NEW));
        ServiceRequestService service = new ServiceRequestService(requests, new RoomDAO(),
                new FakeTechnicianDAO(), new FakeWorkRecordDAO(0));

        service.changeState(1, RequestState.ASSIGNED, 2L);

        assertEquals(RequestState.ASSIGNED, requests.data.get(1L).getState());
        assertEquals(2L, requests.data.get(1L).getTechnicianId());
    }

    @Test
    void doneWithoutWorkRecordIsRejected() {
        FakeRequestDAO requests = new FakeRequestDAO();
        requests.put(request(3L, RequestState.IN_PROGRESS));
        ServiceRequestService service = new ServiceRequestService(requests, new RoomDAO(),
                new FakeTechnicianDAO(), new FakeWorkRecordDAO(0));

        assertThrows(IllegalArgumentException.class, () -> service.changeState(3, RequestState.DONE, null));
    }

    private static ServiceRequest request(Long id, RequestState state) {
        ServiceRequest r = new ServiceRequest();
        r.setId(id);
        r.setRoomId(1);
        r.setDescription("Тестова заявка");
        r.setState(state);
        return r;
    }

    // Прості замінники DAO, щоб перевірити правила Service без MySQL
    static class FakeRequestDAO extends ServiceRequestDAO {
        final Map<Long, ServiceRequest> data = new HashMap<>();

        void put(ServiceRequest r) {
            data.put(r.getId(), r);
        }

        @Override
        public ServiceRequest getById(long id) {
            return data.get(id);
        }

        @Override
        public void updateState(long id, RequestState state, Long technicianId) {
            data.get(id).setState(state);
            data.get(id).setTechnicianId(technicianId);
        }
    }

    static class FakeTechnicianDAO extends TechnicianDAO {
        @Override
        public Technician getById(long id) {
            return new Technician(id, "Тестовий фахівець", "Електрик", "+380500000000");
        }
    }

    static class FakeWorkRecordDAO extends WorkRecordDAO {
        private final int records;

        FakeWorkRecordDAO(int records) {
            this.records = records;
        }

        @Override
        public int countByRequest(long requestId) {
            return records;
        }
    }
}
