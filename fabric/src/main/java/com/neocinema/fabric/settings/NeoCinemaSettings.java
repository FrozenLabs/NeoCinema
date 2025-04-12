package com.neocinema.fabric.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.neocinema.fabric.NeoCinema;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NeoCinemaSettings {
    private static final Path PATH = MinecraftClient.getInstance().runDirectory
            .toPath()
            .resolve("config")
            .resolve(NeoCinema.MODID)
            .resolve(NeoCinema.MODID + ".json");

    private static final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public final VideoSettings video = new VideoSettings();
    public final AudioSettings audio = new AudioSettings();

    public static class VideoSettings {
        public boolean hideCrosshair = false;
    }

    public static class AudioSettings {
        public float volume = 1.0f;
        public boolean muteWhenOutOfFocus = true;
    }

    public static NeoCinemaSettings load() {
        if (!Files.exists(PATH)) {
            return new NeoCinemaSettings();
        }

        try {
            String json = new String(Files.readAllBytes(PATH));
            return gson.fromJson(json, NeoCinemaSettings.class);
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Failed to load settings: " + e.getMessage());
            return new NeoCinemaSettings();
        }
    }

    public void save() {
        saveExecutor.execute(() -> {
            try {
                String json = gson.toJson(this);
                Files.write(PATH, json.getBytes());
            } catch (IOException e) {
                System.err.println("Failed to save settings: " + e.getMessage());
            }
        });
    }
}