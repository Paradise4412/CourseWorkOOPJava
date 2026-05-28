package presentation;

import domain.DetentionEntity;
import domain.DetentionStatus;
import persistence.DetentionRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Консольный пользовательский интерфейс приложения.
 */
public class ConsoleApplication {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final DetentionRepository repository = new DetentionRepository();
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
        System.out.println("Работа завершена. Данные сохранены в папке database/");
    }

    private void printHeader() {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║   УЧЁТ ЗАДЕРЖАНИЙ — ОТДЕЛЕНИЕ ПОЛИЦИИ               ║");
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
        DetentionEntity entity = readEntityFromUser(null);
        long id = repository.insert(entity);
        System.out.println("Запись успешно добавлена. Номер: " + id);
    }

    private void listAll() throws SQLException {
        List<DetentionEntity> entities = repository.findAll();
        printEntityList(entities);
    }

    private void searchByName() throws SQLException {
        String name = readLine("Введите часть ФИО: ");
        if (name.isBlank()) {
            System.out.println("ФИО не может быть пустым.");
            return;
        }
        printEntityList(repository.findByName(name));
    }

    private void searchByStatus() throws SQLException {
        DetentionStatus status = readStatus();
        printEntityList(repository.findByStatus(status));
    }

    private void viewById() throws SQLException {
        long id = readLong("Введите номер записи: ");
        Optional<DetentionEntity> entity = repository.findById(id);
        if (entity.isPresent()) {
            System.out.println(entity.get());
        } else {
            System.out.println("Запись с номером " + id + " не найдена.");
        }
    }

    private void updateRecord() throws SQLException {
        long id = readLong("Введите номер записи для изменения: ");
        Optional<DetentionEntity> existing = repository.findById(id);
        if (existing.isEmpty()) {
            System.out.println("Запись не найдена.");
            return;
        }
        System.out.println("Текущие данные:");
        System.out.println(existing.get());
        System.out.println("Введите новые данные (Enter — оставить без изменений):");
        DetentionEntity updated = readEntityFromUser(existing.get());
        updated.setId(id);
        if (repository.update(updated)) {
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
        if (repository.delete(id)) {
            System.out.println("Запись удалена.");
        } else {
            System.out.println("Запись не найдена.");
        }
    }

    private void showStatistics() throws SQLException {
        int total = repository.countAll();
        int detained = repository.countByStatus(DetentionStatus.DETAINED);
        int released = repository.countByStatus(DetentionStatus.RELEASED);
        int transferred = repository.countByStatus(DetentionStatus.TRANSFERRED);

        System.out.println("\n--- Статистика ---");
        System.out.println("Всего записей: " + total);
        System.out.println("Задержаны: " + detained);
        System.out.println("Отпущены: " + released);
        System.out.println("Переданы в СИЗО: " + transferred);
    }

    private void printEntityList(List<DetentionEntity> entities) {
        if (entities.isEmpty()) {
            System.out.println("Записей не найдено.");
            return;
        }
        System.out.println("Найдено записей: " + entities.size());
        System.out.println("────────────────────────────────────────");
        for (DetentionEntity entity : entities) {
            System.out.println(entity);
        }
    }

    private DetentionEntity readEntityFromUser(DetentionEntity defaults) {
        DetentionEntity entity = defaults != null ? copy(defaults) : new DetentionEntity();

        String fullName = readOptional("ФИО", defaults != null ? defaults.getFullName() : null);
        if (fullName != null && !fullName.isBlank()) {
            entity.setFullName(fullName.trim());
        } else if (defaults == null) {
            throw new IllegalArgumentException("ФИО обязательно для заполнения");
        }

        LocalDate birth = readOptionalDate("Дата рождения (дд.мм.гггг)",
                defaults != null ? defaults.getBirthDate() : null);
        if (birth != null) {
            entity.setBirthDate(birth);
        } else if (defaults == null) {
            throw new IllegalArgumentException("Дата рождения обязательна");
        }

        LocalDateTime detained = readOptionalDateTime("Дата и время задержания (дд.мм.гггг чч:мм)",
                defaults != null ? defaults.getDetentionDateTime() : LocalDateTime.now());
        if (detained != null) {
            entity.setDetentionDateTime(detained);
        }

        String reason = readOptional("Причина задержания",
                defaults != null ? defaults.getReason() : null);
        if (reason != null && !reason.isBlank()) {
            entity.setReason(reason.trim());
        } else if (defaults == null) {
            throw new IllegalArgumentException("Причина обязательна");
        }

        String article = readOptional("Статья УК (необязательно)",
                defaults != null ? defaults.getArticle() : null);
        if (article != null) {
            entity.setArticle(article.isBlank() ? null : article.trim());
        }

        String officer = readOptional("ФИО сотрудника",
                defaults != null ? defaults.getOfficerName() : null);
        if (officer != null && !officer.isBlank()) {
            entity.setOfficerName(officer.trim());
        } else if (defaults == null) {
            throw new IllegalArgumentException("ФИО сотрудника обязательно");
        }

        String cell = readOptional("Номер камеры (необязательно)",
                defaults != null ? defaults.getCellNumber() : null);
        if (cell != null) {
            entity.setCellNumber(cell.isBlank() ? null : cell.trim());
        }

        if (defaults == null) {
            entity.setStatus(readStatus());
        } else {
            System.out.print("Статус (1-Задержан, 2-Отпущен, 3-СИЗО) [Enter — без изменений]: ");
            String statusInput = scanner.nextLine().trim();
            if (!statusInput.isEmpty()) {
                entity.setStatus(parseStatusChoice(statusInput));
            }
        }

        if (entity.getStatus() != DetentionStatus.DETAINED) {
            LocalDateTime release = readOptionalDateTime(
                    "Дата и время освобождения/передачи (дд.мм.гггг чч:мм)",
                    defaults != null ? defaults.getReleaseDateTime() : LocalDateTime.now());
            entity.setReleaseDateTime(release);
        } else if (defaults == null) {
            entity.setReleaseDateTime(null);
        }

        String notes = readOptional("Примечание",
                defaults != null ? defaults.getNotes() : null);
        if (notes != null) {
            entity.setNotes(notes.isBlank() ? null : notes.trim());
        }

        return entity;
    }

    private DetentionEntity copy(DetentionEntity source) {
        return new DetentionEntity(
                source.getId(), source.getFullName(), source.getBirthDate(),
                source.getDetentionDateTime(), source.getReason(), source.getArticle(),
                source.getOfficerName(), source.getCellNumber(), source.getStatus(),
                source.getReleaseDateTime(), source.getNotes()
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
