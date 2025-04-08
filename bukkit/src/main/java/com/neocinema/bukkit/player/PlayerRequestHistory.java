package com.neocinema.bukkit.player;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.video.VideoInfo;
import com.neocinema.bukkit.video.VideoRequest;

import java.util.*;

public class PlayerRequestHistory {

    private UUID requester;
    private Map<VideoInfo, VideoRequest> requests;

    public PlayerRequestHistory(UUID requester, Set<VideoRequest> requests) {
        this.requester = requester;
        this.requests = new HashMap<>();
        requests.forEach(request -> this.requests.put(request.getVideoInfo(), request));
    }

    public PlayerRequestHistory(UUID requester) {
        this(requester, new HashSet<>());
    }

    public Collection<VideoRequest> getRequests() {
        return requests.values();
    }

    public boolean hasRequested(VideoInfo videoInfo) {
        return requests.containsKey(videoInfo);
    }

    public VideoRequest getRequestFor(VideoInfo videoInfo) {
        return requests.get(videoInfo);
    }

    public void addRequest(VideoInfo videoInfo, NeoCinemaPlugin neoCinemaPlugin) {
        final VideoRequest request;

        if (requests.containsKey(videoInfo)) {
            request = requests.get(videoInfo);
            request.updateLastRequested();
            request.incrementTimeRequested();
            request.setHidden(false);
        } else {
            request = new VideoRequest(requester, videoInfo, System.currentTimeMillis(), 1, false);
            requests.put(videoInfo, request);
        }

        neoCinemaPlugin.getVideoStorage().saveVideoRequest(request);
    }

}
