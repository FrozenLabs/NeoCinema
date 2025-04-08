package com.neocinema.bukkit.theater;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.service.VideoURLParser;
import com.neocinema.bukkit.theater.screen.Screen;
import com.neocinema.bukkit.video.Video;

public class StaticTheater extends Theater {

    private String url;
    private int resWidth;
    private int resHeight;
    private VideoURLParser parser;

    public StaticTheater(NeoCinemaPlugin neoCinemaPlugin, String id, String name, boolean hidden, Screen screen, String url, int resWidth, int resHeight) {
        super(neoCinemaPlugin, id, name, hidden, screen);
        this.url = url;
        this.resWidth = resWidth;
        this.resHeight = resHeight;

        parser = new VideoURLParser(neoCinemaPlugin, url);
        parser.parse(null);
        if (parser.found()) {
            parser.getInfoFetcher().fetch().thenAccept(videoInfo -> {
                if (videoInfo == null) return;
                Video video = new Video(videoInfo, null);
                setPlaying(video);
                video.start();
            });
        }
    }

    public String getUrl() {
        return url;
    }

    public int getResWidth() {
        return resWidth;
    }

    public int getResHeight() {
        return resHeight;
    }

    @Override
    public void forceSkip() {

    }

    @Override
    public void tick(NeoCinemaPlugin neoCinemaPlugin) {
        if (isPlaying()) {
            if (getPlaying().hasEnded()) {
                // Loop video forever
                getPlaying().start();
            }

            updateViewers();
        }
    }

}
