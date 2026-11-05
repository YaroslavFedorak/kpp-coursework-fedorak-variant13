package ua.edu.nupp.kpp.client;

import javafx.concurrent.Task;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// Фоновий імпорт номерів та обладнання з CSV. Кожен рядок передається на сервер тим самим методом addRoom().
public class ImportRoomsTask extends Task<ImportResult> {
    private static final Logger LOG = Logger.getLogger(ImportRoomsTask.class.getName());

    // невелика пауза між рядками, щоб на демонстрації було видно хід імпорту
    private static final long PAUSE_MS = 300;

    private final Path file;
    private final HttpHotelClient api;

    public ImportRoomsTask(Path file, HttpHotelClient api) {
        this.file = file;
        this.api = api;
    }

    @Override
    protected ImportResult call() throws Exception {
        LOG.info("Початок CSV-імпорту: " + file);
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !RoomCsvParser.isValidHeader(lines.get(0))) {
            throw new IllegalArgumentException("Неправильний заголовок CSV. Очікується: " + RoomCsvParser.HEADER);
        }

        int total = Math.max(1, lines.size() - 1);
        int imported = 0;
        List<String> errors = new ArrayList<>();
        updateProgress(0, total);

        for (int i = 1; i < lines.size(); i++) {
            if (isCancelled()) {
                break;
            }
            String line = lines.get(i);
            if (!line.isBlank()) {
                try {
                    api.addRoom(RoomCsvParser.parse(line));
                    imported++;
                } catch (IllegalArgumentException | ApiException e) {
                    if (e instanceof ApiException apiError && apiError.isConnectionError()) {
                        throw e;
                    }
                    errors.add("Рядок " + (i + 1) + ": " + e.getMessage());
                    LOG.warning("CSV, рядок " + (i + 1) + " пропущено: " + e.getMessage());
                }
            }
            updateProgress(i, total);
            updateMessage("Опрацьовано " + i + " із " + total + ", імпортовано: " + imported);
            try {
                Thread.sleep(PAUSE_MS);
            } catch (InterruptedException e) {
                if (isCancelled()) {
                    break;
                }
            }
        }
        LOG.info("CSV-імпорт завершено: імпортовано " + imported + ", пропущено " + errors.size());
        return new ImportResult(imported, errors.size(), errors);
    }
}
