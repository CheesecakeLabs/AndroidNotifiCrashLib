package io.ckl.notificrash.events;

public abstract class EventsListener {

    public abstract EventBuilder beforeCapture(EventBuilder builder);

}
