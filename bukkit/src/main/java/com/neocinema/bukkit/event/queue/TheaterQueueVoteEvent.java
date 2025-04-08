package com.neocinema.bukkit.event.queue;

import com.neocinema.bukkit.theater.Theater;
import org.bukkit.entity.Player;

public class TheaterQueueVoteEvent extends TheaterQueueChangeEvent {

    private final Player voter;

    public TheaterQueueVoteEvent(Theater theater, Player voter) {
        super(theater);
        this.voter = voter;
    }

    public Player getVoter() {
        return voter;
    }

}
