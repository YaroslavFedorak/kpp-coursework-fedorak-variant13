package ua.edu.nupp.kpp.server;

// життєвий цикл заявки: НОВА -> ПРИЗНАЧЕНА -> В_РОБОТІ -> ВИКОНАНО
public enum RequestState {
    NEW("НОВА"),
    ASSIGNED("ПРИЗНАЧЕНА"),
    IN_PROGRESS("В_РОБОТІ"),
    DONE("ВИКОНАНО");

    private final String title;

    RequestState(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
