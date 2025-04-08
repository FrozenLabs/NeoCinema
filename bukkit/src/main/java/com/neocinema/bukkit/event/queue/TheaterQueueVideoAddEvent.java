package com.neocinema.bukkit.event.queue;

import com.neocinema.bukkit.theater.Theater;
import com.neocinema.bukkit.video.Video;

public class TheaterQueueVideoAddEvent extends TheaterQueueVideoEvent {

    public TheaterQueueVideoAddEvent(Theater theater, Video video) {
        super(theater, video);
    }

}
