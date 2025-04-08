package com.neocinema.bukkit.event.queue;

import com.neocinema.bukkit.event.TheaterEvent;
import com.neocinema.bukkit.theater.Theater;

public class TheaterQueueChangeEvent extends TheaterEvent {

    public TheaterQueueChangeEvent(Theater theater) {
        super(theater);
    }

}
