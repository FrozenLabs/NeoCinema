package com.neocinema.bukkit.event;

import com.neocinema.bukkit.theater.Theater;
import org.bukkit.entity.Player;

public class PlayerVoteSkipEvent extends PlayerTheaterEvent {

    public PlayerVoteSkipEvent(Player player, Theater theater) {
        super(player, theater);
    }

}
