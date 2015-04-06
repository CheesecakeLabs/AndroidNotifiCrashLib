package io.ckl.notifibug.events;

public abstract class NotifiBugEventsListener {

    public abstract NotifiBugEventBuilder beforeCapture(NotifiBugEventBuilder builder);

}
