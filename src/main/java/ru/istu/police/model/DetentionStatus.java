package ru.istu.police.model;

/**
 * Статус задержанного лица.
 */
public enum DetentionStatus {
    DETAINED("Задержан"),
    RELEASED("Отпущен"),
    TRANSFERRED("Передан в СИЗО");

    private final String label;

    DetentionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static DetentionStatus fromLabel(String label) {
        for (DetentionStatus status : values()) {
            if (status.label.equalsIgnoreCase(label) || status.name().equalsIgnoreCase(label)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус: " + label);
    }
}
