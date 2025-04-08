package com.neocinema.bukkit.service.infofetcher;

import com.neocinema.bukkit.service.VideoServiceType;
import com.neocinema.bukkit.video.VideoInfo;

import java.util.concurrent.CompletableFuture;

public class TwitchVideoInfoFetcher extends VideoInfoFetcher {

    private static final String TWITCH_THUMBNAIL_URL = "https://cinemamodstatic.ewr1.vultrobjects.com/images/twitch_thumbnail.jpg";

    private final String twitchUser;

    public TwitchVideoInfoFetcher(String twitchUser) {
        super("neocinema.request.twitch");
        this.twitchUser = twitchUser;
    }

    @Override
    public CompletableFuture<VideoInfo> fetch() {
        String title = twitchUser + "'s Twitch Stream";
        VideoInfo videoInfo = new VideoInfo(
                VideoServiceType.TWITCH,
                twitchUser,
                title,
                twitchUser,
                TWITCH_THUMBNAIL_URL,
                0);
        return CompletableFuture.completedFuture(videoInfo);
    }

}
