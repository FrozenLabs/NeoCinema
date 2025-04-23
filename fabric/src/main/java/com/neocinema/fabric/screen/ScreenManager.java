package com.neocinema.fabric.screen;

import com.neocinema.fabric.video.playback.VideoLanPlayback;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ScreenManager {

    private final ConcurrentHashMap<BlockPos, Screen> screens;

    public ScreenManager() {
        screens = new ConcurrentHashMap<>();
    }

    public Collection<Screen> getScreens() {
        return screens.values();
    }

    public Screen getCurrentScreen() {
        for (Screen screen : screens.values()) {
            if (screen.hasPlayer()) {
                return screen;
            }
        }

        return null;
    }

    public void registerScreen(Screen screen) {
        if (screens.containsKey(screen.getPos())) {
            Screen old = screens.get(screen.getPos());
            old.unregister();
            old.release();
        }

        screen.register();

        screens.put(screen.getPos(), screen);
    }

    public Screen getScreen(BlockPos pos) {
        return screens.get(pos);
    }

    public boolean hasActiveScreen() {
        for (Screen screen : screens.values()) {
            if (screen.hasPlayer()) {
                return true;
            }
        }

        return false;
    }

    public void unloadAll() {
        for (Screen screen : screens.values()) {
            screen.release();
            screen.unregister();
        }

        screens.clear();
    }

}
