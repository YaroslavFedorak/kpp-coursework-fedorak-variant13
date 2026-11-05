package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// Спільна частина для всіх Servlet: GET повертає список, POST виконує action і повертає оновлений список.
public abstract class BaseJsonServlet extends HttpServlet {

    private final transient ObjectMapper mapper = new ObjectMapper();
    private final transient Logger log = Logger.getLogger(getClass().getName());

    protected abstract Object list() throws Exception;

    protected abstract void handle(String action, HttpServletRequest req) throws Exception;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            writeJson(resp, list());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            String action = req.getParameter("action");
            if (action == null || action.isBlank()) {
                throw new IllegalArgumentException("Не задано операцію (параметр action)");
            }
            handle(action, req);
            writeJson(resp, list());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    private void writeJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        mapper.writeValue(resp.getWriter(), data);
    }

    private void writeError(HttpServletResponse resp, Exception e) throws IOException {
        boolean clientError = e instanceof IllegalArgumentException;
        String message;
        if (clientError) {
            message = e.getMessage() == null ? "Некоректний запит" : e.getMessage();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.warning("Відхилено запит: " + message);
        } else {
            message = "Внутрішня помилка сервера";
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.log(Level.SEVERE, "Помилка під час обробки запиту", e);
        }
        writeJson(resp, Map.of("error", message));
    }

    protected IllegalArgumentException unknownAction() {
        return new IllegalArgumentException("Невідома операція");
    }

    protected String text(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        return value == null ? null : value.trim();
    }

    protected long longParam(HttpServletRequest req, String name, String title) {
        try {
            return Long.parseLong(text(req, name));
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Поле «" + title + "» має бути цілим числом");
        }
    }

    protected Long optionalLong(HttpServletRequest req, String name, String title) {
        String value = text(req, name);
        return value == null || value.isEmpty() ? null : longParam(req, name, title);
    }

    protected int intParam(HttpServletRequest req, String name, String title) {
        try {
            return Integer.parseInt(text(req, name));
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Поле «" + title + "» має бути цілим числом");
        }
    }

    protected double doubleParam(HttpServletRequest req, String name, String title) {
        try {
            return Double.parseDouble(text(req, name).replace(',', '.'));
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Поле «" + title + "» має бути числом");
        }
    }
}
