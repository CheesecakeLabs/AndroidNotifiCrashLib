package io.ckl.notificrash.events;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.UUID;

public class EventRequest implements Serializable {

    private String mEventData;
    private UUID uuid;

    public EventRequest(EventBuilder builder) {
        mEventData = new JSONObject(builder.getEvent()).toString();
        uuid = UUID.randomUUID();
    }

    /**
     * @return String
     */
    public String getEventData() {
        return mEventData;
    }

    /**
     * @return UUID
     */
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object other) {
        EventRequest otherRequest = (EventRequest) other;

        if (this.uuid != null && otherRequest.uuid != null) {
            return uuid.equals(otherRequest.uuid);
        }

        return false;
    }
}
