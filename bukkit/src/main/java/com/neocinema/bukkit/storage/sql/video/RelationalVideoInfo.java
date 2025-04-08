package com.neocinema.bukkit.storage.sql.video;

import com.neocinema.bukkit.video.VideoInfo;

public class RelationalVideoInfo extends VideoInfo {

    private final int relId;

    public RelationalVideoInfo(VideoInfo videoInfo, int relId) {
        super(videoInfo.getServiceType(), videoInfo.getId(), videoInfo.getTitle(), videoInfo.getPoster(), videoInfo.getThumbnailUrl(), videoInfo.getDurationSeconds());
        this.relId = relId;
    }

    public int getRelId() {
        return relId;
    }

}
