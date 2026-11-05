package ua.edu.nupp.kpp.client;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MainApp extends Application {
    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());
    private static final Path DEFAULT_CSV = Path.of("sample-data", "rooms.csv");

    private final HttpHotelClient api = new HttpHotelClient();

    private final TableView<Room> rooms = new TableView<>();
    private final TableView<Technician> technicians = new TableView<>();
    private final TableView<ServiceRequest> requests = new TableView<>();
    private final TableView<WorkRecord> workRecords = new TableView<>();

    private final Map<Long, Room> roomById = new HashMap<>();
    private final Map<Long, Technician> technicianById = new HashMap<>();

    private final ComboBox<Room> requestRoom = new ComboBox<>();
    private final ComboBox<Technician> assignTechnician = new ComboBox<>();
    private final ComboBox<ServiceRequest> recordRequest = new ComboBox<>();
    private final ComboBox<Technician> recordTechnician = new ComboBox<>();

    private ImportRoomsTask importTask;

    @Override
    public void init() {
        setupLogging();
    }

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane(
                new Tab("Номери", roomPane()),
                new Tab("Фахівці", technicianPane()),
                new Tab("Заявки", requestPane()),
                new Tab("Записи виконання", workRecordPane()));
        tabs.getTabs().forEach(t -> t.setClosable(false));

        stage.setScene(new Scene(tabs, 1440, 760));
        stage.setTitle("Технічні заявки готельного комплексу");
        stage.show();
        LOG.info("Клієнт запущено");
        refreshAll();
    }

    // ---------------- Номери ----------------
    private Pane roomPane() {
        rooms.getColumns().setAll(List.of(
                col("ID", "id", 45), col("Номер", "number", 70), col("Поверх", "floor", 65),
                col("Категорія", "category", 100), col("Місткість", "capacity", 80),
                col("Обладнання", "equipment", 380)));

        TextField number = new TextField();
        TextField floor = new TextField();
        TextField category = new TextField();
        TextField capacity = new TextField();
        TextArea equipment = new TextArea();
        equipment.setPrefRowCount(3);
        equipment.setWrapText(true);

        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> safe(() -> {
            api.addRoom(readRoom(null, number, floor, category, capacity, equipment));
            refreshRooms();
        }));
        update.setOnAction(e -> safe(() -> {
            Room selected = selected(rooms);
            api.updateRoom(readRoom(selected.getId(), number, floor, category, capacity, equipment));
            refreshRooms();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteRoom(selected(rooms).getId());
            refreshRooms();
        }));
        refresh.setOnAction(e -> refreshRooms());

        rooms.getSelectionModel().selectedItemProperty().addListener((o, old, r) -> {
            if (r != null) {
                number.setText(r.getNumber());
                floor.setText(String.valueOf(r.getFloor()));
                category.setText(r.getCategory());
                capacity.setText(String.valueOf(r.getCapacity()));
                equipment.setText(r.getEquipment());
            }
        });

        VBox form = form(
                new Label("Номер кімнати"), number,
                new Label("Поверх"), floor,
                new Label("Категорія (Стандарт, Напівлюкс, Люкс)"), category,
                new Label("Місткість, осіб"), capacity,
                new Label("Обладнання (через кому)"), equipment,
                buttons(add, update, delete, refresh),
                new Separator(),
                csvImportBox());
        return layout(rooms, form);
    }

    private VBox csvImportBox() {
        Label title = new Label("Імпорт номерів та обладнання з CSV");
        title.setStyle("-fx-font-weight: bold;");
        TextField path = new TextField(DEFAULT_CSV.toString());
        Button choose = new Button("Обрати…");
        HBox.setHgrow(path, Priority.ALWAYS);
        Button start = new Button("Імпортувати");
        Button cancel = new Button("Скасувати");
        cancel.setDisable(true);
        ProgressBar progress = new ProgressBar(0);
        progress.setMaxWidth(Double.MAX_VALUE);
        Label status = new Label("Імпорт не запущено");
        status.setWrapText(true);

        choose.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Виберіть CSV-файл");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            File folder = DEFAULT_CSV.getParent().toFile();
            if (folder.isDirectory()) {
                chooser.setInitialDirectory(folder.getAbsoluteFile());
            }
            File file = chooser.showOpenDialog(path.getScene().getWindow());
            if (file != null) {
                path.setText(file.getPath());
            }
        });

        start.setOnAction(e -> safe(() -> {
            Path file = Path.of(requireText(path, "Файл CSV"));
            if (!Files.isRegularFile(file)) {
                throw new IllegalArgumentException("Файл не знайдено: " + file.toAbsolutePath());
            }
            importTask = new ImportRoomsTask(file, api);
            progress.progressProperty().bind(importTask.progressProperty());
            status.textProperty().bind(importTask.messageProperty());
            start.setDisable(true);
            cancel.setDisable(false);

            importTask.setOnSucceeded(x -> {
                finishImport(progress, status, start, cancel);
                ImportResult result = importTask.getValue();
                status.setText("Готово. Імпортовано: " + result.imported() + ", пропущено: " + result.skipped());
                refreshRooms();
                showImportSummary(result);
            });
            importTask.setOnFailed(x -> {
                finishImport(progress, status, start, cancel);
                status.setText("Імпорт завершився з помилкою");
                refreshRooms();
                showError(importTask.getException());
            });
            importTask.setOnCancelled(x -> {
                finishImport(progress, status, start, cancel);
                status.setText("Імпорт скасовано користувачем");
                refreshRooms();
            });

            Thread thread = new Thread(importTask, "csv-import");
            thread.setDaemon(true);
            thread.start();
        }));
        cancel.setOnAction(e -> {
            if (importTask != null) {
                importTask.cancel();
            }
        });

        return new VBox(6, title, new HBox(6, path, choose), new HBox(8, start, cancel), progress, status);
    }

    private void finishImport(ProgressBar progress, Label status, Button start, Button cancel) {
        progress.progressProperty().unbind();
        status.textProperty().unbind();
        start.setDisable(false);
        cancel.setDisable(true);
    }

    private void showImportSummary(ImportResult result) {
        StringBuilder text = new StringBuilder("Імпортовано записів: " + result.imported()
                + "\nПропущено рядків: " + result.skipped());
        if (!result.errors().isEmpty()) {
            text.append("\n\n").append(String.join("\n", result.errors()));
        }
        dialog(Alert.AlertType.INFORMATION, "Результат CSV-імпорту", text.toString());
    }

    private Room readRoom(Long id, TextField number, TextField floor, TextField category,
                          TextField capacity, TextArea equipment) {
        return new Room(id, requireText(number, "Номер кімнати"), toInt(floor, "Поверх"),
                requireText(category, "Категорія"), toInt(capacity, "Місткість"),
                requireText(equipment, "Обладнання"));
    }

    // ---------------- Фахівці ----------------
    private Pane technicianPane() {
        technicians.getColumns().setAll(List.of(
                col("ID", "id", 45), col("ПІБ", "fullName", 260),
                col("Спеціалізація", "specialization", 220), col("Телефон", "phone", 150)));

        TextField fullName = new TextField();
        TextField specialization = new TextField();
        TextField phone = new TextField();

        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> safe(() -> {
            api.addTechnician(new Technician(null, requireText(fullName, "ПІБ"),
                    requireText(specialization, "Спеціалізація"), requireText(phone, "Телефон")));
            refreshTechnicians();
        }));
        update.setOnAction(e -> safe(() -> {
            Technician t = selected(technicians);
            api.updateTechnician(new Technician(t.getId(), requireText(fullName, "ПІБ"),
                    requireText(specialization, "Спеціалізація"), requireText(phone, "Телефон")));
            refreshTechnicians();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteTechnician(selected(technicians).getId());
            refreshTechnicians();
        }));
        refresh.setOnAction(e -> refreshTechnicians());

        technicians.getSelectionModel().selectedItemProperty().addListener((o, old, t) -> {
            if (t != null) {
                fullName.setText(t.getFullName());
                specialization.setText(t.getSpecialization());
                phone.setText(t.getPhone());
            }
        });

        return layout(technicians, form(
                new Label("ПІБ"), fullName,
                new Label("Спеціалізація"), specialization,
                new Label("Телефон"), phone,
                buttons(add, update, delete, refresh)));
    }

    // ---------------- Заявки ----------------
    private Pane requestPane() {
        requests.getColumns().setAll(List.of(
                col("ID", "id", 35),
                text("Номер", r -> roomLabel(r.getRoomId()), 55),
                col("Опис несправності", "description", 175),
                col("Вплив", "guestImpact", 58),
                col("Тяжк.", "severity", 50),
                col("Простій", "downtimeHours", 70),
                col("F1", "f1", 46), col("F2", "f2", 46), col("F3", "f3", 46),
                col("I", "priorityIndex", 52),
                col("Пріоритет", "priorityLevel", 92),
                col("Стан", "state", 112),
                text("Фахівець", r -> technicianLabel(r.getTechnicianId()), 110),
                col("Створено", "createdDate", 95)));

        TextArea description = new TextArea();
        description.setPrefRowCount(3);
        description.setWrapText(true);
        Spinner<Integer> guestImpact = new Spinner<>(0, 10, 5);
        Spinner<Integer> severity = new Spinner<>(1, 5, 3);
        TextField downtime = new TextField("0");
        requestRoom.setMaxWidth(Double.MAX_VALUE);

        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> safe(() -> {
            api.addRequest(readRequest(null, description, guestImpact, severity, downtime));
            refreshRequests();
        }));
        update.setOnAction(e -> safe(() -> {
            ServiceRequest selected = selected(requests);
            api.updateRequest(readRequest(selected.getId(), description, guestImpact, severity, downtime));
            refreshRequests();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteRequest(selected(requests).getId());
            refreshRequests();
        }));
        refresh.setOnAction(e -> refreshRequests());

        ComboBox<RequestState> nextState = new ComboBox<>(FXCollections.observableArrayList(RequestState.values()));
        nextState.setMaxWidth(Double.MAX_VALUE);
        assignTechnician.setMaxWidth(Double.MAX_VALUE);
        Button change = new Button("Змінити стан");
        change.setOnAction(e -> safe(() -> {
            ServiceRequest selected = selected(requests);
            if (nextState.getValue() == null) {
                throw new IllegalArgumentException("Виберіть новий стан");
            }
            Technician t = assignTechnician.getValue();
            api.changeRequestState(selected.getId(), nextState.getValue(), t == null ? null : t.getId());
            refreshRequests();
        }));

        requests.getSelectionModel().selectedItemProperty().addListener((o, old, r) -> {
            if (r != null) {
                requestRoom.setValue(roomById.get(r.getRoomId()));
                description.setText(r.getDescription());
                guestImpact.getValueFactory().setValue(r.getGuestImpact());
                severity.getValueFactory().setValue(r.getSeverity());
                downtime.setText(String.valueOf(r.getDowntimeHours()));
                assignTechnician.setValue(r.getTechnicianId() == null ? null : technicianById.get(r.getTechnicianId()));
            }
        });

        Label lifecycle = new Label("Життєвий цикл: НОВА → ПРИЗНАЧЕНА → В_РОБОТІ → ВИКОНАНО");
        lifecycle.setWrapText(true);
        lifecycle.setStyle("-fx-font-weight: bold;");

        return layout(requests, form(
                new Label("Номер"), requestRoom,
                new Label("Опис несправності"), description,
                new Label("Вплив на гостя (0–10)"), guestImpact,
                new Label("Тяжкість несправності (1–5)"), severity,
                new Label("Очікуваний простій номера, год"), downtime,
                buttons(add, update, delete, refresh),
                new Separator(),
                lifecycle,
                new Label("Новий стан"), nextState,
                new Label("Фахівець (для призначення)"), assignTechnician,
                change), 0.75);
    }

    private ServiceRequest readRequest(Long id, TextArea description, Spinner<Integer> guestImpact,
                                       Spinner<Integer> severity, TextField downtime) {
        Room room = requestRoom.getValue();
        if (room == null) {
            throw new IllegalArgumentException("Виберіть номер");
        }
        return new ServiceRequest(id, room.getId(), requireText(description, "Опис несправності"),
                guestImpact.getValue(), severity.getValue(), toDouble(downtime, "Очікуваний простій"));
    }

    // ---------------- Записи виконання ----------------
    private Pane workRecordPane() {
        workRecords.getColumns().setAll(List.of(
                col("ID", "id", 45),
                text("Заявка", w -> "№" + w.getRequestId(), 70),
                text("Фахівець", w -> technicianLabel(w.getTechnicianId()), 200),
                col("Дата", "workDate", 95),
                col("Виконані роботи", "description", 300),
                col("Витрачено, год", "hoursSpent", 125)));

        recordRequest.setMaxWidth(Double.MAX_VALUE);
        recordTechnician.setMaxWidth(Double.MAX_VALUE);
        DatePicker date = new DatePicker(LocalDate.now());
        TextArea description = new TextArea();
        description.setPrefRowCount(3);
        description.setWrapText(true);
        TextField hours = new TextField();

        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> safe(() -> {
            api.addWorkRecord(readWorkRecord(null, date, description, hours));
            refreshWorkRecords();
        }));
        update.setOnAction(e -> safe(() -> {
            WorkRecord selected = selected(workRecords);
            api.updateWorkRecord(readWorkRecord(selected.getId(), date, description, hours));
            refreshWorkRecords();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteWorkRecord(selected(workRecords).getId());
            refreshWorkRecords();
        }));
        refresh.setOnAction(e -> refreshWorkRecords());

        workRecords.getSelectionModel().selectedItemProperty().addListener((o, old, w) -> {
            if (w != null) {
                recordRequest.setValue(recordRequest.getItems().stream()
                        .filter(r -> r.getId() == w.getRequestId()).findFirst().orElse(null));
                recordTechnician.setValue(technicianById.get(w.getTechnicianId()));
                date.setValue(LocalDate.parse(w.getWorkDate()));
                description.setText(w.getDescription());
                hours.setText(String.valueOf(w.getHoursSpent()));
            }
        });

        return layout(workRecords, form(
                new Label("Заявка (запис додається до заявки у стані В_РОБОТІ)"), recordRequest,
                new Label("Фахівець"), recordTechnician,
                new Label("Дата виконання"), date,
                new Label("Виконані роботи"), description,
                new Label("Витрачено, год"), hours,
                buttons(add, update, delete, refresh)));
    }

    private WorkRecord readWorkRecord(Long id, DatePicker date, TextArea description, TextField hours) {
        ServiceRequest request = recordRequest.getValue();
        Technician technician = recordTechnician.getValue();
        if (request == null || technician == null) {
            throw new IllegalArgumentException("Виберіть заявку та фахівця");
        }
        if (date.getValue() == null) {
            throw new IllegalArgumentException("Вкажіть дату виконання");
        }
        return new WorkRecord(id, request.getId(), technician.getId(), date.getValue().toString(),
                requireText(description, "Виконані роботи"), toDouble(hours, "Витрачено, год"));
    }

    // ---------------- Оновлення даних із сервера ----------------
    private void refreshAll() {
        refreshRooms();
        refreshTechnicians();
        refreshRequests();
        refreshWorkRecords();
    }

    private void refreshRooms() {
        safe(() -> {
            List<Room> list = api.getRooms();
            roomById.clear();
            list.forEach(r -> roomById.put(r.getId(), r));
            rooms.setItems(FXCollections.observableArrayList(list));
            requestRoom.setItems(FXCollections.observableArrayList(list));
            requests.refresh();
        });
    }

    private void refreshTechnicians() {
        safe(() -> {
            List<Technician> list = api.getTechnicians();
            technicianById.clear();
            list.forEach(t -> technicianById.put(t.getId(), t));
            technicians.setItems(FXCollections.observableArrayList(list));
            assignTechnician.setItems(FXCollections.observableArrayList(list));
            recordTechnician.setItems(FXCollections.observableArrayList(list));
            requests.refresh();
            workRecords.refresh();
        });
    }

    private void refreshRequests() {
        safe(() -> {
            List<ServiceRequest> list = api.getRequests();
            requests.setItems(FXCollections.observableArrayList(list));
            recordRequest.setItems(FXCollections.observableArrayList(list));
        });
    }

    private void refreshWorkRecords() {
        safe(() -> workRecords.setItems(FXCollections.observableArrayList(api.getWorkRecords())));
    }

    // ---------------- Допоміжні методи ----------------
    private String roomLabel(long roomId) {
        Room r = roomById.get(roomId);
        return r == null ? "id " + roomId : r.getNumber();
    }

    private String technicianLabel(Long technicianId) {
        if (technicianId == null) {
            return "—";
        }
        Technician t = technicianById.get(technicianId);
        return t == null ? "id " + technicianId : t.getFullName();
    }

    private String requireText(TextInputControl field, String name) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Заповніть поле «" + name + "»");
        }
        return value;
    }

    private int toInt(TextField field, String name) {
        try {
            return Integer.parseInt(requireText(field, name));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + name + "» має бути цілим числом");
        }
    }

    private double toDouble(TextField field, String name) {
        try {
            return Double.parseDouble(requireText(field, name).replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + name + "» має бути числом");
        }
    }

    private <T> T selected(TableView<T> table) {
        T value = table.getSelectionModel().getSelectedItem();
        if (value == null) {
            throw new IllegalArgumentException("Виберіть запис у таблиці");
        }
        return value;
    }

    private void safe(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            LOG.warning(e.getMessage());
            showError(e);
        }
    }

    private void showError(Throwable e) {
        String message = e == null || e.getMessage() == null ? "Невідома помилка" : e.getMessage();
        dialog(Alert.AlertType.ERROR, "Операцію не виконано", message);
    }

    private void dialog(Alert.AlertType type, String header, String text) {
        Label content = new Label(text);
        content.setWrapText(true);
        content.setMaxWidth(460);
        Alert dialog = new Alert(type, "", ButtonType.OK);
        dialog.setHeaderText(header);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        dialog.showAndWait();
    }

    private VBox form(javafx.scene.Node... nodes) {
        VBox box = new VBox(6, nodes);
        box.setPadding(new Insets(0, 0, 0, 10));
        return box;
    }

    private HBox buttons(Button... buttons) {
        return new HBox(8, buttons);
    }

    private Pane layout(TableView<?> table, VBox form) {
        return layout(table, form, 0.66);
    }

    private Pane layout(TableView<?> table, VBox form, double divider) {
        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        SplitPane split = new SplitPane(table, scroll);
        split.setDividerPositions(divider);
        BorderPane root = new BorderPane(split);
        root.setPadding(new Insets(10));
        return root;
    }

    private <S, T> TableColumn<S, T> col(String title, String property, double width) {
        TableColumn<S, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(width);
        return c;
    }

    private <S> TableColumn<S, String> text(String title, Function<S, String> value, double width) {
        TableColumn<S, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cell -> new SimpleStringProperty(value.apply(cell.getValue())));
        c.setPrefWidth(width);
        return c;
    }

    private static void setupLogging() {
        try {
            FileHandler handler = new FileHandler("client.log", true);
            handler.setEncoding(StandardCharsets.UTF_8.name());
            handler.setFormatter(new SimpleFormatter());
            Logger.getLogger("ua.edu.nupp.kpp.client").addHandler(handler);
        } catch (IOException e) {
            System.err.println("Не вдалося відкрити client.log: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
