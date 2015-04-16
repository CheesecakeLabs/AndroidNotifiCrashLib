package io.ckl.notifibug.events;

public abstract class EventsListener {

    public abstract EventBuilder beforeCapture(EventBuilder builder);

}
