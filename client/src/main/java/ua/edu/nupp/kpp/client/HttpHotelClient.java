package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

// Єдине місце, де JavaFX-клієнт працює з HTTP. Інтерфейс викликає лише методи цього класу.
public class HttpHotelClient {
    private static final Logger LOG = Logger.getLogger(HttpHotelClient.class.getName());
    private static final String DEFAULT_BASE = "http://localhost:8080/hotel-requests-server";

    private final String base;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public HttpHotelClient() {
        this(System.getProperty("server.url", DEFAULT_BASE));
    }

    public HttpHotelClient(String base) {
        this.base = base;
    }

    // ---- Номери ----
    public List<Room> getRooms() {
        return read(get("/rooms"), Room[].class);
    }

    public void addRoom(Room r) {
        post("/rooms", roomForm("add", r));
    }

    public void updateRoom(Room r) {
        post("/rooms", roomForm("update", r) + "&id=" + r.getId());
    }

    public void deleteRoom(long id) {
        post("/rooms", "action=delete&id=" + id);
    }

    // ---- Фахівці ----
    public List<Technician> getTechnicians() {
        return read(get("/technicians"), Technician[].class);
    }

    public void addTechnician(Technician t) {
        post("/technicians", technicianForm("add", t));
    }

    public void updateTechnician(Technician t) {
        post("/technicians", technicianForm("update", t) + "&id=" + t.getId());
    }

    public void deleteTechnician(long id) {
        post("/technicians", "action=delete&id=" + id);
    }

    // ---- Заявки ----
    public List<ServiceRequest> getRequests() {
        return read(get("/requests"), ServiceRequest[].class);
    }

    public void addRequest(ServiceRequest r) {
        post("/requests", requestForm("add", r));
    }

    public void updateRequest(ServiceRequest r) {
        post("/requests", requestForm("update", r) + "&id=" + r.getId());
    }

    public void changeRequestState(long id, RequestState state, Long technicianId) {
        String form = "action=transition&id=" + id + "&state=" + state.name();
        if (technicianId != null) {
            form += "&technicianId=" + technicianId;
        }
        post("/requests", form);
    }

    public void deleteRequest(long id) {
        post("/requests", "action=delete&id=" + id);
    }

    // ---- Записи виконання ----
    public List<WorkRecord> getWorkRecords() {
        return read(get("/work-records"), WorkRecord[].class);
    }

    public void addWorkRecord(WorkRecord w) {
        post("/work-records", workRecordForm("add", w));
    }

    public void updateWorkRecord(WorkRecord w) {
        post("/work-records", workRecordForm("update", w) + "&id=" + w.getId());
    }

    public void deleteWorkRecord(long id) {
        post("/work-records", "action=delete&id=" + id);
    }

    private String roomForm(String action, Room r) {
        return "action=" + action + "&number=" + enc(r.getNumber()) + "&floor=" + r.getFloor()
                + "&category=" + enc(r.getCategory()) + "&capacity=" + r.getCapacity()
                + "&equipment=" + enc(r.getEquipment());
    }

    private String technicianForm(String action, Technician t) {
        return "action=" + action + "&fullName=" + enc(t.getFullName())
                + "&specialization=" + enc(t.getSpecialization()) + "&phone=" + enc(t.getPhone());
    }

    private String requestForm(String action, ServiceRequest r) {
        return "action=" + action + "&roomId=" + r.getRoomId() + "&description=" + enc(r.getDescription())
                + "&guestImpact=" + r.getGuestImpact() + "&severity=" + r.getSeverity()
                + "&downtimeHours=" + r.getDowntimeHours();
    }

    private String workRecordForm(String action, WorkRecord w) {
        return "action=" + action + "&requestId=" + w.getRequestId() + "&technicianId=" + w.getTechnicianId()
                + "&workDate=" + enc(w.getWorkDate()) + "&description=" + enc(w.getDescription())
                + "&hoursSpent=" + w.getHoursSpent();
    }

    private String get(String path) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(base + path))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return send(req);
    }

    private String post(String path, String form) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(base + path))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();
        return send(req);
    }

    private String send(HttpRequest req) {
        HttpResponse<String> resp;
        try {
            resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOG.warning("Сервер недоступний: " + req.uri());
            throw new ApiException("Сервер недоступний. Перевірте, чи запущено Tomcat і MySQL.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Запит перервано", e);
        }
        if (resp.statusCode() >= 400) {
            String message = errorMessage(resp.body());
            LOG.warning("HTTP " + resp.statusCode() + " " + req.method() + " " + req.uri() + ": " + message);
            throw new ApiException(message, resp.statusCode());
        }
        return resp.body();
    }

    private String errorMessage(String body) {
        try {
            JsonNode node = mapper.readTree(body);
            if (node != null && node.has("error")) {
                return node.get("error").asText();
            }
        } catch (IOException ignored) {
            // відповідь не у форматі JSON
        }
        return "Помилка сервера";
    }

    private <T> List<T> read(String json, Class<T[]> type) {
        try {
            return Arrays.asList(mapper.readValue(json, type));
        } catch (IOException e) {
            throw new ApiException("Помилка JSON у відповіді сервера", e);
        }
    }

    private String enc(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
