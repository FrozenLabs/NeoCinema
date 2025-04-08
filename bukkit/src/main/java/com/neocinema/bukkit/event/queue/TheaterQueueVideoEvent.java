package com.neocinema.bukkit.event.queue;

import com.neocinema.bukkit.theater.Theater;
import com.neocinema.bukkit.video.Video;

public class TheaterQueueVideoEvent extends TheaterQueueChangeEvent {

    private final Video video;

    public TheaterQueueVideoEvent(Theater theater, Video video) {
        super(theater);
        this.video = video;
    }

    public Video getVideo() {
        return video;
    }

}
