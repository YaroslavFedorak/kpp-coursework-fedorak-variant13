package ua.edu.nupp.kpp.client;

// значення збігаються із серверним enum, бо стан передається через JSON за назвою
public enum RequestState {
    NEW("НОВА"),
    ASSIGNED("ПРИЗНАЧЕНА"),
    IN_PROGRESS("В_РОБОТІ"),
    DONE("ВИКОНАНО");

    private final String title;

    RequestState(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
