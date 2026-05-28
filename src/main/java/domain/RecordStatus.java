package domain;

public enum RecordStatus {
    DETAINED("Задержан"),
    RELEASED("Отпущен"),
    TRANSFERRED("Передан в СИЗО");

    private final String label;

    RecordStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static RecordStatus fromLabel(String label) {
        for (RecordStatus status : values()) {
            if (status.label.equalsIgnoreCase(label) || status.name().equalsIgnoreCase(label)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус: " + label);
    }
}
