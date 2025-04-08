package com.neocinema.fabric.gui.widget;

import com.neocinema.fabric.util.NetworkUtil;
import com.neocinema.fabric.video.list.VideoListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Selectable;

import java.util.List;

public class VideoHistoryListWidgetEntry extends VideoListWidgetEntry {

    public VideoHistoryListWidgetEntry(VideoListWidget parent, VideoListEntry video, MinecraftClient client) {
        super(parent, video, client);
    }

    @Override
    protected void trashButtonAction(VideoListEntry video) {
        NetworkUtil.sendDeleteHistoryPacket(video.getVideoInfo());
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return null;
    }

}
