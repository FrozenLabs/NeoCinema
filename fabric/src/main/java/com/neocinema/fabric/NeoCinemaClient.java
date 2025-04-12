package com.neocinema.fabric;

import com.neocinema.fabric.block.preview.PreviewScreenBlock;
import com.neocinema.fabric.block.preview.PreviewScreenBlockEntity;
import com.neocinema.fabric.block.screen.ScreenBlock;
import com.neocinema.fabric.block.screen.ScreenBlockEntity;
import com.neocinema.fabric.block.preview.PreviewScreenBlockEntityRenderer;
import com.neocinema.fabric.block.screen.ScreenBlockEntityRenderer;
import com.neocinema.fabric.gui.VideoQueueScreen;
import com.neocinema.fabric.screen.preview.PreviewScreenManager;
import com.neocinema.fabric.screen.ScreenManager;
import com.neocinema.fabric.settings.NeoCinemaSettings;
import com.neocinema.fabric.util.NetworkUtil;
import com.neocinema.fabric.video.list.VideoListManager;
import com.neocinema.fabric.video.queue.VideoQueue;
import net.fabricmc.api.ClientModInitializer;

public class NeoCinemaClient implements ClientModInitializer {
    private static NeoCinemaClient instance;

    public static NeoCinemaClient getInstance() {
        return instance;
    }

    private ScreenManager screenManager;
    private PreviewScreenManager previewScreenManager;
    private NeoCinemaSettings settings;
    private VideoListManager videoListManager;
    private VideoQueue videoQueue;

    public ScreenManager getScreenManager() {
        return screenManager;
    }

    public PreviewScreenManager getPreviewScreenManager() {
        return previewScreenManager;
    }

    public NeoCinemaSettings getSettings() {
        return settings;
    }

    public VideoListManager getVideoListManager() {
        return videoListManager;
    }

    public VideoQueue getVideoQueue() {
        return videoQueue;
    }

    @Override
    public void onInitializeClient() {
        instance = this;

        // Register ScreenBlock
        ScreenBlock.register();
        ScreenBlockEntity.register();
        ScreenBlockEntityRenderer.register();

        // Register PreviewScreenBlock
        PreviewScreenBlock.register();
        PreviewScreenBlockEntity.register();
        PreviewScreenBlockEntityRenderer.register();

        NetworkUtil.registerReceivers();

        screenManager = new ScreenManager();
        previewScreenManager = new PreviewScreenManager();
        videoListManager = new VideoListManager();
        videoQueue = new VideoQueue();

        settings = NeoCinemaSettings.load();

        VideoQueueScreen.registerKeyInput();
    }
}
