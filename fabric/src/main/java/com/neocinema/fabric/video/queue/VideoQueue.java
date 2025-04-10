package com.neocinema.fabric.video.queue;

import com.neocinema.fabric.video.VideoInfo;
import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;
import java.util.List;

public class VideoQueue {

    private List<QueuedVideo> videos;

    public VideoQueue() {
        videos = new ArrayList<>();
    }

    public List<QueuedVideo> getVideos() {
        return videos;
    }

    public void clear() {
        videos.clear();
    }

    public void setVideos(List<QueuedVideo> videos) {
        this.videos = videos;
    }

    public VideoQueue fromBytes(PacketByteBuf buf) {
        List<QueuedVideo> videos = new ArrayList<>();
        int length = buf.readInt();
        for (int i = 0; i < length; i++) {
            VideoInfo videoInfo = new VideoInfo().fromBytes(buf);
            int score = buf.readInt();
            int clientState = buf.readInt();
            boolean owner = buf.readBoolean();
            videos.add(new QueuedVideo(videoInfo, score, clientState, owner));
        }
        this.videos = videos;
        return this;
    }

}
