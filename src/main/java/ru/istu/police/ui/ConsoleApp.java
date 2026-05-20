package ru.istu.police.ui;

import ru.istu.police.dao.DetentionDao;
import ru.istu.police.model.DetentionRecord;
import ru.istu.police.model.DetentionStatus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Консольный интерфейс приложения.
 */
public class ConsoleApp {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final DetentionDao dao = new DetentionDao();
    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        printHeader();
        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine("Выберите пункт меню: ");
            try {
                running = handleChoice(choice);
            } catch (SQLException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка ввода: " + e.getMessage());
            }
            System.out.println();
        }
        System.out.println("Работа завершена. Данные сохранены в папке data/");
    }

    private void printHeader() {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║   УЧЁТ ЗАДЕРЖАНИЙ — ОТДЕЛЕНИЕ ПОЛИЦИИ               ║");
        System.out.println("║   Курсовая работа (Java + JDBC + H2)                 ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private void printMenu() {
        System.out.println("--- Главное меню ---");
        System.out.println(" 1. Добавить запись о задержании");
        System.out.println(" 2. Показать все записи");
        System.out.println(" 3. Найти по ФИО");
        System.out.println(" 4. Найти по статусу");
        System.out.println(" 5. Просмотреть запись по номеру");
        System.out.println(" 6. Изменить запись");
        System.out.println(" 7. Удалить запись");
        System.out.println(" 8. Статистика");
        System.out.println(" 0. Выход");
    }

    private boolean handleChoice(String choice) throws SQLException {
        switch (choice.trim()) {
            case "1" -> addRecord();
            case "2" -> listAll();
            case "3" -> searchByName();
            case "4" -> searchByStatus();
            case "5" -> viewById();
            case "6" -> updateRecord();
            case "7" -> deleteRecord();
            case "8" -> showStatistics();
            case "0" -> {
                return false;
            }
            default -> System.out.println("Неверный пункт меню.");
        }
        return true;
    }

    private void addRecord() throws SQLException {
        System.out.println("\n--- Новое задержание ---");
        DetentionRecord record = readRecordFromUser(null);
        long id = dao.insert(record);
        System.out.println("Запись успешно добавлена. Номер: " + id);
    }

    private void listAll() throws SQLException {
        List<DetentionRecord> records = dao.findAll();
        printRecordList(records);
    }

    private void searchByName() throws SQLException {
        String name = readLine("Введите часть ФИО: ");
        if (name.isBlank()) {
            System.out.println("ФИО не может быть пустым.");
            return;
        }
        printRecordList(dao.findByName(name));
    }

    private void searchByStatus() throws SQLException {
        DetentionStatus status = readStatus();
        printRecordList(dao.findByStatus(status));
    }

    private void viewById() throws SQLException {
        long id = readLong("Введите номер записи: ");
        Optional<DetentionRecord> record = dao.findById(id);
        if (record.isPresent()) {
            System.out.println(record.get());
        } else {
            System.out.println("Запись с номером " + id + " не найдена.");
        }
    }

    private void updateRecord() throws SQLException {
        long id = readLong("Введите номер записи для изменения: ");
        Optional<DetentionRecord> existing = dao.findById(id);
        if (existing.isEmpty()) {
            System.out.println("Запись не найдена.");
            return;
        }
        System.out.println("Текущие данные:");
        System.out.println(existing.get());
        System.out.println("Введите новые данные (Enter — оставить без изменений):");
        DetentionRecord updated = readRecordFromUser(existing.get());
        updated.setId(id);
        if (dao.update(updated)) {
            System.out.println("Запись обновлена.");
        } else {
            System.out.println("Не удалось обновить запись.");
        }
    }

    private void deleteRecord() throws SQLException {
        long id = readLong("Введите номер записи для удаления: ");
        String confirm = readLine("Удалить запись №" + id + "? (да/нет): ");
        if (!confirm.equalsIgnoreCase("да")) {
            System.out.println("Удаление отменено.");
            return;
        }
        if (dao.delete(id)) {
            System.out.println("Запись удалена.");
        } else {
            System.out.println("Запись не найдена.");
        }
    }

    private void showStatistics() throws SQLException {
        int total = dao.countAll();
        int detained = dao.countByStatus(DetentionStatus.DETAINED);
        int released = dao.countByStatus(DetentionStatus.RELEASED);
        int transferred = dao.countByStatus(DetentionStatus.TRANSFERRED);

        System.out.println("\n--- Статистика ---");
        System.out.println("Всего записей: " + total);
        System.out.println("Задержаны: " + detained);
        System.out.println("Отпущены: " + released);
        System.out.println("Переданы в СИЗО: " + transferred);
    }

    private void printRecordList(List<DetentionRecord> records) {
        if (records.isEmpty()) {
            System.out.println("Записей не найдено.");
            return;
        }
        System.out.println("Найдено записей: " + records.size());
        System.out.println("────────────────────────────────────────");
        for (DetentionRecord r : records) {
            System.out.println(r);
        }
    }

    private DetentionRecord readRecordFromUser(DetentionRecord defaults) {
        DetentionRecord r = defaults != null ? copy(defaults) : new DetentionRecord();

        String fullName = readOptional("ФИО", defaults != null ? defaults.getFullName() : null);
        if (fullName != null && !fullName.isBlank()) {
            r.setFullName(fullName.trim());
        } else if (defaults == null) {
            throw new IllegalArgumentException("ФИО обязательно для заполнения");
        }

        LocalDate birth = readOptionalDate("Дата рождения (дд.мм.гггг)",
                defaults != null ? defaults.getBirthDate() : null);
        if (birth != null) {
            r.setBirthDate(birth);
        } else if (defaults == null) {
            throw new IllegalArgumentException("Дата рождения обязательна");
        }

        LocalDateTime detained = readOptionalDateTime("Дата и время задержания (дд.мм.гггг чч:мм)",
                defaults != null ? defaults.getDetentionDateTime() : LocalDateTime.now());
        if (detained != null) {
            r.setDetentionDateTime(detained);
        }

        String reason = readOptional("Причина задержания",
                defaults != null ? defaults.getReason() : null);
        if (reason != null && !reason.isBlank()) {
            r.setReason(reason.trim());
        } else if (defaults == null) {
            throw new IllegalArgumentException("Причина обязательна");
        }

        String article = readOptional("Статья УК (необязательно)",
                defaults != null ? defaults.getArticle() : null);
        if (article != null) {
            r.setArticle(article.isBlank() ? null : article.trim());
        }

        String officer = readOptional("ФИО сотрудника",
                defaults != null ? defaults.getOfficerName() : null);
        if (officer != null && !officer.isBlank()) {
            r.setOfficerName(officer.trim());
        } else if (defaults == null) {
            throw new IllegalArgumentException("ФИО сотрудника обязательно");
        }

        String cell = readOptional("Номер камеры (необязательно)",
                defaults != null ? defaults.getCellNumber() : null);
        if (cell != null) {
            r.setCellNumber(cell.isBlank() ? null : cell.trim());
        }

        if (defaults == null) {
            r.setStatus(readStatus());
        } else {
            System.out.print("Статус (1-Задержан, 2-Отпущен, 3-СИЗО) [Enter — без изменений]: ");
            String statusInput = scanner.nextLine().trim();
            if (!statusInput.isEmpty()) {
                r.setStatus(parseStatusChoice(statusInput));
            }
        }

        if (r.getStatus() != DetentionStatus.DETAINED) {
            LocalDateTime release = readOptionalDateTime(
                    "Дата и время освобождения/передачи (дд.мм.гггг чч:мм)",
                    defaults != null ? defaults.getReleaseDateTime() : LocalDateTime.now());
            r.setReleaseDateTime(release);
        } else if (defaults == null) {
            r.setReleaseDateTime(null);
        }

        String notes = readOptional("Примечание",
                defaults != null ? defaults.getNotes() : null);
        if (notes != null) {
            r.setNotes(notes.isBlank() ? null : notes.trim());
        }

        return r;
    }

    private DetentionRecord copy(DetentionRecord src) {
        return new DetentionRecord(
                src.getId(), src.getFullName(), src.getBirthDate(),
                src.getDetentionDateTime(), src.getReason(), src.getArticle(),
                src.getOfficerName(), src.getCellNumber(), src.getStatus(),
                src.getReleaseDateTime(), src.getNotes()
        );
    }

    private DetentionStatus readStatus() {
        System.out.println("Статус:");
        System.out.println(" 1 — Задержан");
        System.out.println(" 2 — Отпущен");
        System.out.println(" 3 — Передан в СИЗО");
        String input = readLine("Выберите статус (1-3): ");
        return parseStatusChoice(input);
    }

    private DetentionStatus parseStatusChoice(String input) {
        return switch (input.trim()) {
            case "1" -> DetentionStatus.DETAINED;
            case "2" -> DetentionStatus.RELEASED;
            case "3" -> DetentionStatus.TRANSFERRED;
            default -> throw new IllegalArgumentException("Выберите 1, 2 или 3");
        };
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private String readOptional(String fieldName, String current) {
        if (current != null) {
            System.out.print(fieldName + " [" + current + "]: ");
        } else {
            System.out.print(fieldName + ": ");
        }
        return scanner.nextLine();
    }

    private LocalDate readOptionalDate(String fieldName, LocalDate current) {
        String currentStr = current != null ? current.format(DATE_FMT) : null;
        String input = readOptional(fieldName, currentStr);
        if (input == null || input.isBlank()) {
            return current;
        }
        try {
            return LocalDate.parse(input.trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Неверный формат даты. Используйте дд.мм.гггг");
        }
    }

    private LocalDateTime readOptionalDateTime(String fieldName, LocalDateTime current) {
        String currentStr = current != null ? current.format(DATETIME_FMT) : null;
        String input = readOptional(fieldName, currentStr);
        if (input == null || input.isBlank()) {
            return current;
        }
        try {
            return LocalDateTime.parse(input.trim(), DATETIME_FMT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Неверный формат. Используйте дд.мм.гггг чч:мм");
        }
    }

    private long readLong(String prompt) {
        while (true) {
            String input = readLine(prompt).trim();
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число.");
            }
        }
    }
}
