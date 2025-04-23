package com.neocinema.fabric.util;

import com.neocinema.fabric.NeoCinemaClient;
import com.neocinema.fabric.screen.Screen;
import com.neocinema.fabric.video.playback.VideoLanPlayback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.player.base.StatusApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class PlaybackStatusHudUtil {
    public static void render(DrawContext ctx) {
        List<String> lines = buildDebugStrings();

        AtomicInteger y = new AtomicInteger(30);
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        lines.forEach(line -> {
            ctx.drawText(
                    tr,
                    line,
                    ctx.getScaledWindowWidth() - tr.getWidth(line) - 30,
                    y.get(),
                    -1,
                    true
            );
            y.addAndGet(tr.fontHeight);
            y.addAndGet(5);
        });


    }

    private static @NotNull List<String> buildDebugStrings() {
        Screen screen = NeoCinemaClient.getInstance().getScreenManager().getCurrentScreen();
        if (screen == null) return new ArrayList<>();

        VideoLanPlayback playback = screen.getPlayer();
        StatusApi status = playback.mediaPlayer().status();

        List<String> lines = new ArrayList<>();

        lines.add("Playing: %s".formatted(screen.getVideo().getVideoInfo().getTitle()));
        lines.add("Duration: %s".formatted(screen.getVideo().getVideoInfo().getDurationString()));
        lines.add("Requested By: %s".formatted(screen.getVideo().getVideoInfo().getPoster()));
        lines.add("");
        lines.add("Time: %s".formatted(status.time()));
        lines.add("State: %s".formatted(status.state()));
        return lines;
    }
}
