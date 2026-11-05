package ua.edu.nupp.kpp.client;

// Помилка взаємодії з сервером. status = 0 означає, що сервер недоступний.
public class ApiException extends RuntimeException {
    private final int status;

    public ApiException(String message, int status) {
        super(message);
        this.status = status;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.status = 0;
    }

    public int getStatus() {
        return status;
    }

    public boolean isConnectionError() {
        return status == 0;
    }
}
