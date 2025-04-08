package com.neocinema.bukkit.event;

import com.neocinema.bukkit.theater.Theater;
import org.bukkit.entity.Player;

public class PlayerEnterTheaterEvent extends PlayerTheaterEvent {

    public PlayerEnterTheaterEvent(Player who, Theater theater) {
        super(who, theater);
    }

}
