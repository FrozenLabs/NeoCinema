package com.neocinema.bukkit.event;

import com.neocinema.bukkit.theater.Theater;
import org.bukkit.entity.Player;

public class PlayerLeaveTheaterEvent extends PlayerTheaterEvent {

    public PlayerLeaveTheaterEvent(Player who, Theater theater) {
        super(who, theater);
    }

}
