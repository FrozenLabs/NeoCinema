package com.neocinema.bukkit.util;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.service.VideoServiceType;
import com.neocinema.bukkit.theater.PrivateTheater;
import com.neocinema.bukkit.theater.Theater;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public final class ChatUtil {

    public static final ChatColor MAIN_COLOR = ChatColor.of("#8F2121");
    public static final ChatColor SECONDARY_COLOR = ChatColor.of("#5e6061");
    private static final NeoCinemaPlugin NEO_CINEMA_PLUGIN = NeoCinemaPlugin.getPlugin(NeoCinemaPlugin.class);

    public static void sendPaddedMessage(Player player, String... lines) {
        for (String line : lines) {
            if (line != null)
                player.sendMessage(line);
        }
    }

    public static void showPlaying(Player player, Theater theater, boolean showOriginUrl) {
        if (!theater.isPlaying()) {
            sendPaddedMessage(player,
                    ChatColor.BOLD + "> Now Playing",
                    SECONDARY_COLOR + "Request a video with /request");
        } else {
            sendPaddedMessage(player,
                    ChatColor.BOLD + "> Now Playing",
                    SECONDARY_COLOR + theater.getPlaying().getVideoInfo().getTitle(),
                    SECONDARY_COLOR + "Requested by: " + theater.getPlaying().getRequester().getName(),
                    showOriginUrl ? SECONDARY_COLOR + theater.getPlaying().getVideoInfo().getServiceType().getOriginUrl(theater.getPlaying().getVideoInfo().getId()) : null);

            if (theater.getPlaying().getVideoInfo().getServiceType() == VideoServiceType.TWITCH) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "The Twitch stream may have a 30 second disclaimer before it starts.");
            }
        }
    }
}