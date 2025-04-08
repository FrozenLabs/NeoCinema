package com.neocinema.bukkit.event.queue;

import com.neocinema.bukkit.theater.Theater;
import org.bukkit.entity.Player;

public class TheaterQueueVoteRemoveEvent extends TheaterQueueVoteEvent {

    public TheaterQueueVoteRemoveEvent(Theater theater, Player voter) {
        super(theater, voter);
    }

}
