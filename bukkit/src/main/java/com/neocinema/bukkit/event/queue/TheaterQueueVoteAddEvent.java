package com.neocinema.bukkit.event.queue;

import com.neocinema.bukkit.theater.Theater;
import com.neocinema.bukkit.video.queue.QueueVoteType;
import org.bukkit.entity.Player;

public class TheaterQueueVoteAddEvent extends TheaterQueueVoteEvent {

    private final QueueVoteType voteType;

    public TheaterQueueVoteAddEvent(Theater theater, Player voter, QueueVoteType voteType) {
        super(theater, voter);
        this.voteType = voteType;
    }

    public QueueVoteType getVoteType() {
        return voteType;
    }

}
