package ru.etyosft.aurorawars.events;


import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.etyosft.aurorawars.wars.War;

public class WarEndedEvent extends Event {

    private War war;

    public WarEndedEvent(War war)
    {
        this.war = war;
    }

    public War getWar() {
        return war;
    }

    private static final HandlerList handlers = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
