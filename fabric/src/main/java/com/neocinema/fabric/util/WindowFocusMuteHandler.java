package com.neocinema.fabric.util;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.NeoCinemaClient;
import com.neocinema.fabric.screen.Screen;

public class WindowFocusMuteHandler {
    public static void loseFocus() {
        try {
            for (Screen screen : NeoCinemaClient.getInstance().getScreenManager().getScreens()) {
                screen.setVideoVolume(0f);
            }
        } catch (Exception e) {
            NeoCinema.LOGGER.warn("Failed to mute videos on focus loss", e);
        }
    }

    public static void gainFocus() {
        try {
            float volume = NeoCinemaClient.getInstance().getVideoSettings().getVolume();
            for (Screen screen : NeoCinemaClient.getInstance().getScreenManager().getScreens()) {
                screen.setVideoVolume(volume);
            }
        } catch (Exception e) {
            NeoCinema.LOGGER.warn("Failed to restore video volume on focus gain", e);
        }
    }
}
