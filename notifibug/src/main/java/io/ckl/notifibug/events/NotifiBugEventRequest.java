package io.ckl.notifibug.events;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.UUID;

public class NotifiBugEventRequest implements Serializable {
    private String requestData;
    private UUID uuid;

    public NotifiBugEventRequest(NotifiBugEventBuilder builder) {
        this.requestData = new JSONObject(builder.event).toString();
        this.uuid = UUID.randomUUID();
    }

    /**
     * @return String
     */
    public String getRequestData() {
        return requestData;
    }

    /**
     * @return UUID
     */
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object other) {
        NotifiBugEventRequest otherRequest = (NotifiBugEventRequest) other;

        if (this.uuid != null && otherRequest.uuid != null) {
            return uuid.equals(otherRequest.uuid);
        }

        return false;
    }
}
