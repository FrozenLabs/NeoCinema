package com.neocinema.bukkit.task;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.theater.Theater;
import com.neocinema.bukkit.util.ChatUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.StringJoiner;

public class PlayerListUpdateTask implements Runnable {

    private final NeoCinemaPlugin neoCinemaPlugin;

    public PlayerListUpdateTask(NeoCinemaPlugin neoCinemaPlugin) {
        this.neoCinemaPlugin = neoCinemaPlugin;
    }

    @Override
    public void run() {
        StringJoiner footerBuilder = new StringJoiner("\n");

        footerBuilder.add("");
        footerBuilder.add("");
        footerBuilder.add(ChatColor.BOLD + "NOW PLAYING" + ChatColor.RESET);
        footerBuilder.add(ChatColor.STRIKETHROUGH + "                    " + ChatColor.RESET);

        for (Theater theater : neoCinemaPlugin.getTheaterManager().getTheaters()) {
            if (theater.isHidden()) {
                continue;
            }

            final String playing;

            if (!theater.isPlaying()) {
                playing = "Nothing";
            } else {
                playing = theater.getPlaying().getVideoInfo().getTitleShort();
            }

            // TODO: is tabular formatting in tab possible?
            String theaterLine = ChatUtil.MAIN_COLOR + theater.getName() + ChatColor.RESET + " || " + ChatUtil.SECONDARY_COLOR + playing;

            footerBuilder.add(theaterLine);
        }

        String footer = footerBuilder.toString();

        for (Player player : neoCinemaPlugin.getServer().getOnlinePlayers()) {
            player.setPlayerListFooter(footer);
        }
    }

}
