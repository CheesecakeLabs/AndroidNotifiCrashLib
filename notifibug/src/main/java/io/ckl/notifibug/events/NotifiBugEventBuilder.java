package io.ckl.notifibug.events;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import io.ckl.notifibug.NotifiBug;

public class NotifiBugEventBuilder implements Serializable {

    private static final String CRASH_EVENT_ID = "event_id";

    private static final String CRASH = "crash";
    private static final String CRASH_NAME = "exception_name";
    private static final String CRASH_TIMESTAMP = "crash_time";
    private static final String CRASH_TYPE = "exception_reason";
    private static final String CRASH_CLASS = "class_name";
    private static final String CRASH_LINE = "line_number";
    private static final String CRASH_METHOD = "method_name";
    private static final String CRASH_STACKTRACE = "stack_trace";

    private static final String CRASH_MESSAGE = "crash_message";
    private static final String CRASH_LEVEL = "crash_level";

    private static final String APP_SERIAL_NUMBER = "serial_number";

    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());

    static {
        sdf.setTimeZone(TimeZone.getDefault());
    }

    public Map<String, Object> event;

    public NotifiBugEventBuilder() {
        event = new HashMap<>();
        event.put(CRASH_EVENT_ID, UUID.randomUUID().toString().replace("-", ""));
        event.put(APP_SERIAL_NUMBER, NotifiBug.getSerialNumber());
    }

    public NotifiBugEventBuilder(Throwable t, NotifiBugEventLevel level) {
        this();
        this.setCrash(getCrash(t));
    }

    /**
     * "exception_name": "NullPointerException"
     *
     * @param name String
     * @return NotifiBugEventBuilder
     */
    public NotifiBugEventBuilder setName(String name) {
        event.put(CRASH_NAME, name);
        return this;
    }

    /**
     * "stack_message": "Some message"
     *
     * @param message String
     * @return NotifiBugEventBuilder
     */
    public NotifiBugEventBuilder setMessage(String message) {
        event.put(CRASH_MESSAGE, message);
        return this;
    }

    /**
     * @param crash JSONObject
     * @return NotifiBugEventBuilder
     */
    public NotifiBugEventBuilder setCrash(JSONObject crash) {
        event.put(CRASH, crash);
        return this;
    }

    /**
     * "crash_time": "2011-05-02T17:41:36Z"
     *
     * @param timestamp long
     * @return NotifiBugEventBuilder
     */
    public NotifiBugEventBuilder setTimestamp(long timestamp) {
        event.put(CRASH_TIMESTAMP, sdf.format(new Date(timestamp)));
        return this;
    }

    /**
     * "line_number": 0
     *
     * @param lineNumber int
     * @return NotifiBugEventBuilder
     */
    public NotifiBugEventBuilder setLineNumber(int lineNumber) {
        event.put(CRASH_LINE, lineNumber);
        return this;
    }

    /**
     * "crash_method": "MethodName"
     *
     * @param method String
     * @return NotifiBugEventBuilder
     */
    public NotifiBugEventBuilder setMethod(String method) {
        event.put(CRASH_METHOD, method);
        return this;
    }

    /**
     * "level": "warning"
     *
     * @param level CheeseBugEventLevel
     * @return CheeseBugEventBuilder
     */
    public NotifiBugEventBuilder setCrashLevel(NotifiBugEventLevel level) {
        event.put(CRASH_LEVEL, level.getValue());
        return this;
    }

    /**
     * "exception_reason": "2011-05-02T17:41:36Z"
     *
     * @param crashType String
     * @return NotifiBugEventBuilder
     */
    public NotifiBugEventBuilder setCrashType(String crashType) {
        event.put(CRASH_TYPE, crashType);
        return this;
    }

    /**
     * "class_name": "String.java"
     *
     * @param crashClass String
     * @return NotifiBugEventBuilder
     */
    public NotifiBugEventBuilder setCrashClass(String crashClass) {
        event.put(CRASH_CLASS, crashClass);
        return this;
    }

    /**
     * "stack_trace": "Full stacktrace"
     *
     * @param stacktrace String
     * @return NotifiBugEventBuilder
     */
    public NotifiBugEventBuilder setStacktrace(String stacktrace) {
        event.put(CRASH_STACKTRACE, stacktrace);
        return this;
    }

    /**
     * "logger": "my.logger.name"
     *
     * @param logger String
     * @return NotifiBugEventBuilder
     */
    public NotifiBugEventBuilder setLogger(String logger) {
        event.put("logger", logger);
        return this;
    }

    /**
     * @param tags Map
     * @return NotifiBugEventBuilder
     */
    public NotifiBugEventBuilder setTags(Map<String, String> tags) {
        setTags(new JSONObject(tags));
        return this;
    }

    /**
     * @param tags JSONObject
     * @return NotifiBugEventBuilder
     */
    public NotifiBugEventBuilder setTags(JSONObject tags) {
        event.put("tags", tags);
        return this;
    }

    /**
     * @return JSONObject
     */
    public JSONObject getTags() {
        if (!event.containsKey("tags")) {
            setTags(new HashMap<String, String>());
        }

        return (JSONObject) event.get("tags");
    }

    /**
     * @param extra Map
     * @return NotifiBugEventBuilder
     */
    public NotifiBugEventBuilder setExtra(Map<String, String> extra) {
        setExtra(new JSONObject(extra));
        return this;
    }

    public NotifiBugEventBuilder setExtra(JSONObject extra) {
        event.put("extra", extra);
        return this;
    }

    /**
     * @return JSONObject
     */
    public JSONObject getExtra() {
        if (!event.containsKey("extra")) {
            setExtra(new HashMap<String, String>());
        }

        return (JSONObject) event.get("extra");
    }

    /**
     * @param t Throwable
     * @return NotifiBugEventBuilder
     */
    public JSONObject getCrash(Throwable t) {

        JSONObject crash = new JSONObject();

        try {
            crash.put(CRASH_NAME, t.getClass().getSimpleName());
            crash.put(CRASH_TYPE, t.getMessage());
            crash.put(CRASH_LINE, NotifiBug.getLineNumber(t));
            crash.put(CRASH_CLASS, NotifiBug.getCrashClassName(t));
            crash.put(CRASH_CLASS, NotifiBug.getCrashClassName(t));
            crash.put(CRASH_TIMESTAMP, sdf.format(new Date(System.currentTimeMillis())));
            crash.put(CRASH_STACKTRACE, NotifiBug.getStackTrace(t));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return crash;
    }
}