package com.neocinema.fabric.settings;

import com.neocinema.fabric.NeoCinema;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class VideoSettings {

    private static final Path PATH = MinecraftClient.getInstance().runDirectory
            .toPath()
            .resolve("config")
            .resolve(NeoCinema.MODID)
            .resolve(NeoCinema.MODID + ".properties");

    private float volume;
    private boolean muteWhenAltTabbed;
    private boolean hideCrosshair;
    private int browserResolution;
    private int browserRefreshRate;

    public VideoSettings(float volume, boolean muteWhenAltTabbed, boolean hideCrosshair, int browserResolution, int browserRefreshRate) {
        this.volume = volume;
        this.muteWhenAltTabbed = muteWhenAltTabbed;
        this.hideCrosshair = hideCrosshair;
        this.browserResolution = browserResolution;
        this.browserRefreshRate = browserRefreshRate;
    }

    public VideoSettings() {
        volume = 1.0f;
        muteWhenAltTabbed = true;
        hideCrosshair = true;
        browserResolution = 720;
        browserRefreshRate = 30;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public boolean isMuteWhenAltTabbed() {
        return muteWhenAltTabbed;
    }

    public void setMuteWhenAltTabbed(boolean muteWhenAltTabbed) {
        this.muteWhenAltTabbed = muteWhenAltTabbed;
    }

    public boolean isHideCrosshair() {
        return hideCrosshair;
    }

    public void setHideCrosshair(boolean hideCrosshair) {
        this.hideCrosshair = hideCrosshair;
    }

    public int getBrowserResolution() {
        return browserResolution;
    }

    public void setNextBrowserResolution() {
        if (browserResolution <= 240) {
            browserResolution = 360;
        } else if (browserResolution <= 360) {
            browserResolution = 480;
        } else if (browserResolution <= 480) {
            browserResolution = 720;
        } else if (browserResolution <= 720) {
            browserResolution = 1080;
        } else if (browserResolution >= 1080) {
            browserResolution = 240;
        }
    }

    public int getBrowserRefreshRate() {
        return browserRefreshRate;
    }

    public void setNextBrowserRefreshRate() {
        if (browserRefreshRate <= 24) {
            browserRefreshRate = 30;
        } else if (browserRefreshRate <= 30) {
            browserRefreshRate = 60;
        } else if (browserRefreshRate >= 60) {
            browserRefreshRate = 24;
        }
    }

    public void saveAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                save();
            } catch (IOException e) {
                NeoCinema.LOGGER.error("Failed to save video settings", e);
            }
        });
    }

    public void save() throws IOException {
        File file = PATH.toFile();

        file.getParentFile().mkdirs();

        if (!file.exists()) {
            if (!file.createNewFile()) { throw new IOException("Failed to create file " + file); }
        }

        Properties properties = new Properties();
        properties.setProperty("volume", String.valueOf(volume));
        properties.setProperty("mute-while-alt-tabbed", String.valueOf(muteWhenAltTabbed));
        properties.setProperty("hide-crosshair-while-screen-loaded", String.valueOf(hideCrosshair));
        properties.setProperty("browser-resolution", String.valueOf(browserResolution));
        properties.setProperty("browser-refresh-rate", String.valueOf(browserRefreshRate));

        try (FileOutputStream output = new FileOutputStream(file)) {
            properties.store(output, null);
        }
    }

    public void load() throws IOException {
        File file = PATH.toFile();

        if (!file.exists()) {
            save();
        }

        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
        }

        try {
            volume = Float.parseFloat(properties.getProperty("volume"));
            muteWhenAltTabbed = properties.getProperty("mute-while-alt-tabbed").equalsIgnoreCase("true");
            hideCrosshair = properties.getProperty("hide-crosshair-while-screen-loaded").equalsIgnoreCase("true");
            browserResolution = Integer.parseInt(properties.getProperty("browser-resolution"));
            browserRefreshRate = Integer.parseInt(properties.getProperty("browser-refresh-rate"));
        } catch (Exception e) {
            NeoCinema.LOGGER.warn("Failed to load video settings", e);
            if (!file.delete()) throw new IOException("Failed to delete file " + file.getAbsolutePath());
            save();
        }
    }
}
