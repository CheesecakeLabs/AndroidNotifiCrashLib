package io.ckl.notificrash.events;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import io.ckl.notificrash.NotifiCrash;
import io.ckl.notificrash.helpers.CrashHelper;

public class EventBuilder implements Serializable {

    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String CRASH_EVENT_ID = "event_id";

    private static final String CRASH_NAME = "name";
    private static final String CRASH_TIMESTAMP = "time";
    private static final String CRASH_REASON = "reason";
    private static final String CRASH_APP_VERSION = "app_version";
    private static final String CRASH_OS_VERSION = "os_version";
    private static final String CRASH_DEVICE_MODEL = "device_model";
    private static final String CRASH_CLASS = "class_name";
    private static final String CRASH_LINE = "line_number";
    private static final String CRASH_METHOD = "method_name";
    private static final String CRASH_STACKTRACE = "stack_trace";

    private static final String CRASH_MESSAGE = "crash_message";
    private static final String CRASH_LEVEL = "crash_level";

    private static final String APP_SERIAL_NUMBER = "application";
    private static final String EXTRA_ARGUMENTS = "extra";

    private final static SimpleDateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault());

    static {
        dateFormat.setTimeZone(TimeZone.getDefault());
    }

    private Map<String, Object> mEvent;

    public EventBuilder() {
        mEvent = new HashMap<>();
        mEvent.put(CRASH_EVENT_ID, UUID.randomUUID().toString().replace("-", ""));
        mEvent.put(APP_SERIAL_NUMBER, NotifiCrash.getSerialNumber());
    }

    public EventBuilder(Throwable throwable) {
        this();

        this.setDeviceInfo();
        this.setAppVersion();
        this.setAndroidVersion();
        this.setExtraArguments();

        this.setName(CrashHelper.findName(throwable));
        this.setReason(CrashHelper.findReason(throwable));
        this.setTimestamp(CrashHelper.findTime());
        this.setLineNumber(CrashHelper.findLineNumber(throwable));
        this.setClassName(CrashHelper.findClassName(throwable));
        this.setMethodName(CrashHelper.findMethod(throwable));
        this.setStacktrace(CrashHelper.findStackTrace(throwable));
    }

    /**
     * "name": "NullPointerException"
     *
     * @param name String
     * @return EventBuilder
     */
    public EventBuilder setName(String name) {
        mEvent.put(CRASH_NAME, name);
        return this;
    }

    /**
     * "reason": "divide by zero"
     *
     * @param crashType String
     * @return EventBuilder
     */
    public EventBuilder setReason(String crashType) {
        mEvent.put(CRASH_REASON, crashType);
        return this;
    }

    /**
     * "time": "2011-05-02T17:41:36+0000"
     *
     * @param timestamp long
     * @return EventBuilder
     */
    public EventBuilder setTimestamp(long timestamp) {
        mEvent.put(CRASH_TIMESTAMP, dateFormat.format(new Date(timestamp)));
        return this;
    }

    /**
     * "class_name": "MainActivity.java"
     *
     * @param crashClass String
     * @return EventBuilder
     */
    public EventBuilder setClassName(String crashClass) {
        mEvent.put(CRASH_CLASS, crashClass);
        return this;
    }

    /**
     * "method": "MethodName"
     *
     * @param method String
     * @return EventBuilder
     */
    public EventBuilder setMethodName(String method) {
        mEvent.put(CRASH_METHOD, method);
        return this;
    }

    /**
     * "line_number": 0
     *
     * @param lineNumber int
     * @return EventBuilder
     */
    public EventBuilder setLineNumber(int lineNumber) {
        mEvent.put(CRASH_LINE, lineNumber);
        return this;
    }

    /**
     * "stack_message": "Some error stack trace"
     *
     * @param message String
     * @return EventBuilder
     */
    public EventBuilder setMessage(String message) {
        mEvent.put(CRASH_MESSAGE, message);
        return this;
    }

    /**
     * "level": "warning"
     *
     * @param level CheeseBugEventLevel
     * @return CheeseBugEventBuilder
     */
    public EventBuilder setCrashLevel(EventLevel level) {
        mEvent.put(CRASH_LEVEL, level.getValue());
        return this;
    }

    /**
     * "stack_trace": "Full stacktrace"
     *
     * @param stacktrace String
     * @return EventBuilder
     */
    public EventBuilder setStacktrace(String stacktrace) {
        mEvent.put(CRASH_STACKTRACE, stacktrace);
        return this;
    }

    /**
     * "device_model": "Full stacktrace"
     *
     * @return EventBuilder
     */
    public EventBuilder setDeviceInfo() {
        mEvent.put(CRASH_DEVICE_MODEL, NotifiCrash.getInstance().getDeviceInfo());
        return this;
    }

    /**
     * "app_version": "1.1"
     *
     * @return EventBuilder
     */
    public EventBuilder setAppVersion() {
        mEvent.put(CRASH_APP_VERSION, NotifiCrash.getInstance().getAppVersion());
        return this;
    }

    /**
     * "os_version": "Android 4.4"
     *
     * @return EventBuilder
     */
    public EventBuilder setAndroidVersion() {
        mEvent.put(CRASH_OS_VERSION, NotifiCrash.getInstance().getAndroidVersion());
        return this;
    }

    /**
     * "extra": "1.1"
     *
     * @return EventBuilder
     */
    public EventBuilder setExtraArguments() {
        HashMap<String, String> extraArguments = NotifiCrash.getInstance().getExtraArguments();
        if (extraArguments != null) {
            mEvent.put(EXTRA_ARGUMENTS, NotifiCrash.getInstance().getExtraArguments());
        }
        return this;
    }

    public Map<String, Object> getEvent() {
        return mEvent;
    }
}