package com.neocinema.fabric;

import com.neocinema.fabric.block.PreviewScreenBlock;
import com.neocinema.fabric.block.PreviewScreenBlockEntity;
import com.neocinema.fabric.block.ScreenBlock;
import com.neocinema.fabric.block.ScreenBlockEntity;
import com.neocinema.fabric.block.render.PreviewScreenBlockEntityRenderer;
import com.neocinema.fabric.block.render.ScreenBlockEntityRenderer;
import com.neocinema.fabric.cef.CefUtil;
import com.neocinema.fabric.cef.Platform;
import com.neocinema.fabric.gui.VideoQueueScreen;
import com.neocinema.fabric.gui.VideoRequestBrowser;
import com.neocinema.fabric.screen.PreviewScreenManager;
import com.neocinema.fabric.screen.ScreenManager;
import com.neocinema.fabric.service.VideoServiceManager;
import com.neocinema.fabric.settings.VideoSettings;
import com.neocinema.fabric.util.NetworkUtil;
import com.neocinema.fabric.util.WindowFocusMuteThread;
import com.neocinema.fabric.video.list.VideoListManager;
import com.neocinema.fabric.video.queue.VideoQueue;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;

public class NeoCinemaClient implements ClientModInitializer {

    private static NeoCinemaClient instance;

    public static NeoCinemaClient getInstance() {
        return instance;
    }

    private VideoServiceManager videoServiceManager;
    private ScreenManager screenManager;
    private PreviewScreenManager previewScreenManager;
    private VideoSettings videoSettings;
    private VideoListManager videoListManager;
    private VideoQueue videoQueue;

    public VideoServiceManager getVideoServiceManager() {
        return videoServiceManager;
    }

    public ScreenManager getScreenManager() {
        return screenManager;
    }

    public PreviewScreenManager getPreviewScreenManager() {
        return previewScreenManager;
    }

    public VideoSettings getVideoSettings() {
        return videoSettings;
    }

    public VideoListManager getVideoListManager() {
        return videoListManager;
    }

    public VideoQueue getVideoQueue() {
        return videoQueue;
    }

    private static void initCefMac() {
        if (Platform.getPlatform().isMacOS()) {
            MinecraftClient.getInstance().execute(() -> {
                if (CefUtil.init()) {
                    NeoCinema.LOGGER.info("Chromium Embedded Framework initialized for macOS");
                } else {
                    NeoCinema.LOGGER.warn("Could not initialize Chromium Embedded Framework for macOS");
                }
            });
        }
    }

    @Override
    public void onInitializeClient() {
        instance = this;

        // Hack for initializing CEF on macOS
        initCefMac();

        // Register ScreenBlock
        ScreenBlock.register();
        ScreenBlockEntity.register();
        ScreenBlockEntityRenderer.register();

        // Register PreviewScreenBlock
        PreviewScreenBlock.register();
        PreviewScreenBlockEntity.register();
        PreviewScreenBlockEntityRenderer.register();

        NetworkUtil.registerReceivers();

        videoServiceManager = new VideoServiceManager();
        screenManager = new ScreenManager();
        previewScreenManager = new PreviewScreenManager();
        videoSettings = new VideoSettings();
        videoListManager = new VideoListManager();
        videoQueue = new VideoQueue();

        try {
            videoSettings.load();
        } catch (IOException e) {
            NeoCinema.LOGGER.error("Could not load video settings!", e);
        }

        new WindowFocusMuteThread().start();

        VideoQueueScreen.registerKeyInput();
        VideoRequestBrowser.registerKeyInput();
    }

}
