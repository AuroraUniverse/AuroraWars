package ru.etyosft.aurorawars.commands;

import ru.etysoft.aurorauniverse.world.Town;

public class Request {

    private Town sender;
    private Town recipient;
    private Runnable onAccept;
    private String textInfo;
    private long timestamp;

    public Request(Town sender, Town recipient, Runnable onAccept, String textInfo)
    {
        this.sender = sender;
        this.recipient = recipient;
        this.onAccept = onAccept;
        this.textInfo = textInfo;
        timestamp = System.currentTimeMillis();
    }

    public boolean isExpired()
    {
        return (System.currentTimeMillis() - timestamp > 600000);
    }

    public Runnable getOnAccept() {
        return onAccept;
    }

    public String getTextInfo() {
        return textInfo;
    }

    public Town getRecipient() {
        return recipient;
    }

    public Town getSender() {
        return sender;
    }
}
