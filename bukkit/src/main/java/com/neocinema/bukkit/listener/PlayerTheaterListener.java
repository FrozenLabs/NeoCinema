package com.neocinema.bukkit.listener;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.event.*;
import com.neocinema.bukkit.event.*;
import com.neocinema.bukkit.event.*;
import com.neocinema.bukkit.theater.PrivateTheater;
import com.neocinema.bukkit.theater.StaticTheater;
import com.neocinema.bukkit.theater.Theater;
import com.neocinema.bukkit.util.ChatUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTheaterListener implements Listener {

    private final NeoCinemaPlugin neoCinemaPlugin;

    public PlayerTheaterListener(NeoCinemaPlugin neoCinemaPlugin) {
        this.neoCinemaPlugin = neoCinemaPlugin;
    }

    @EventHandler
    public void onPlayerEnterTheater(PlayerEnterTheaterEvent event) {
        Player player = event.getPlayer();
        Theater theater = event.getTheater();

        if (!(theater instanceof StaticTheater)) {
            ChatUtil.showPlaying(player, theater, false);
            theater.showBossBars(neoCinemaPlugin, player);
        }
    }

    @EventHandler
    public void onPlayerLeaveTheater(PlayerLeaveTheaterEvent event) {
        Player player = event.getPlayer();
        Theater theater = event.getTheater();

        if (theater instanceof PrivateTheater) {
            PrivateTheater privateTheater = (PrivateTheater) theater;

            if (privateTheater.isOwner(player)) {
                player.sendMessage(ChatColor.GOLD + "You've lost ownership of the theater because you left.");
                privateTheater.setOwner(null);
            }
        }
    }

    @EventHandler
    public void onTheaterStartVideo(TheaterStartVideoEvent event) {
        Theater theater = event.getTheater();

        if (!(theater instanceof StaticTheater)) {
            for (Player viewer : theater.getViewers()) {
                ChatUtil.showPlaying(viewer, theater, false);
                theater.showBossBars(neoCinemaPlugin, viewer);
            }
        }
    }

    @EventHandler
    public void onVoteSkipAdd(PlayerVoteSkipEvent event) {
        for (Player viewer : event.getTheater().getViewers()) {
            viewer.sendMessage(ChatColor.GOLD + event.getPlayer().getName()
                    + " has voted to skip the current video ("
                    + event.getTheater().getVoteSkips().size()
                    + "/"
                    + event.getTheater().getRequiredVoteSkips()
                    + ")");
        }
    }

    @EventHandler
    public void onVideoVoteSkipped(TheaterVideoVoteSkippedEvent event) {
        for (Player viewer : event.getTheater().getViewers()) {
            viewer.sendMessage(ChatColor.GOLD + "The video has been vote skipped.");
        }
    }

    @EventHandler
    public void onTheaterSetOwner(TheaterSetOwnerEvent event) {
        event.getPlayer().sendMessage(ChatColor.GOLD + "You are now the owner of this theater.");
    }

}
