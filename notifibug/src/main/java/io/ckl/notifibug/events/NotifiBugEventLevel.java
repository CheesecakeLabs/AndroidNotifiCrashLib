package io.ckl.notifibug.events;

public enum NotifiBugEventLevel {

    FATAL("fatal"),
    ERROR("error"),
    WARNING("warning"),
    INFO("info"),
    DEBUG("debug");

    private String value;

    NotifiBugEventLevel(String value) {
        this.value = value;
    }

    /**
     * @return String
     */
    public String getValue() {
        return value;
    }
}
