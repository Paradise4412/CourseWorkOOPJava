package domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RecordEntity {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private Long id;
    private String fullName;
    private LocalDate birthDate;
    private LocalDateTime detentionDateTime;
    private String reason;
    private String article;
    private String officerName;
    private String cellNumber;
    private RecordStatus status;
    private LocalDateTime releaseDateTime;
    private String notes;

    public RecordEntity() {
    }

    public RecordEntity(Long id, String fullName, LocalDate birthDate,
                           LocalDateTime detentionDateTime, String reason, String article,
                           String officerName, String cellNumber, RecordStatus status,
                           LocalDateTime releaseDateTime, String notes) {
        this.id = id;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.detentionDateTime = detentionDateTime;
        this.reason = reason;
        this.article = article;
        this.officerName = officerName;
        this.cellNumber = cellNumber;
        this.status = status;
        this.releaseDateTime = releaseDateTime;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDateTime getDetentionDateTime() {
        return detentionDateTime;
    }

    public void setDetentionDateTime(LocalDateTime detentionDateTime) {
        this.detentionDateTime = detentionDateTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public String getOfficerName() {
        return officerName;
    }

    public void setOfficerName(String officerName) {
        this.officerName = officerName;
    }

    public String getCellNumber() {
        return cellNumber;
    }

    public void setCellNumber(String cellNumber) {
        this.cellNumber = cellNumber;
    }

    public RecordStatus getStatus() {
        return status;
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }

    public LocalDateTime getReleaseDateTime() {
        return releaseDateTime;
    }

    public void setReleaseDateTime(LocalDateTime releaseDateTime) {
        this.releaseDateTime = releaseDateTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Запись №").append(id).append(" ---\n");
        sb.append("ФИО: ").append(fullName).append("\n");
        sb.append("Дата рождения: ").append(birthDate != null ? birthDate.format(DATE_FMT) : "—").append("\n");
        sb.append("Задержан: ").append(detentionDateTime != null ? detentionDateTime.format(DATETIME_FMT) : "—").append("\n");
        sb.append("Причина: ").append(reason).append("\n");
        sb.append("Статья: ").append(article != null && !article.isBlank() ? article : "—").append("\n");
        sb.append("Сотрудник: ").append(officerName).append("\n");
        sb.append("Камера: ").append(cellNumber != null && !cellNumber.isBlank() ? cellNumber : "—").append("\n");
        sb.append("Статус: ").append(status != null ? status.getLabel() : "—").append("\n");
        if (releaseDateTime != null) {
            sb.append("Освобождён/передача: ").append(releaseDateTime.format(DATETIME_FMT)).append("\n");
        }
        if (notes != null && !notes.isBlank()) {
            sb.append("Примечание: ").append(notes).append("\n");
        }
        return sb.toString();
    }
}
