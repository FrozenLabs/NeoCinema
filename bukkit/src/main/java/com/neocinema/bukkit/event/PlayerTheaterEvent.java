package com.neocinema.bukkit.event;

import com.neocinema.bukkit.theater.Theater;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PlayerTheaterEvent extends TheaterEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;

    public PlayerTheaterEvent(Player player, Theater theater) {
        super(theater);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
