package io.ckl.notificrash.events;

public enum EventLevel {

    FATAL("fatal"),
    ERROR("error"),
    WARNING("warning"),
    INFO("info"),
    DEBUG("debug");

    private String value;

    EventLevel(String value) {
        this.value = value;
    }

    /**
     * @return String
     */
    public String getValue() {
        return value;
    }
}
