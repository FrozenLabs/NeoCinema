package com.neocinema.bukkit.event;

import com.neocinema.bukkit.theater.Theater;
import org.bukkit.entity.Player;

public class TheaterSetOwnerEvent extends PlayerTheaterEvent {

    public TheaterSetOwnerEvent(Player player, Theater theater) {
        super(player, theater);
    }

}
