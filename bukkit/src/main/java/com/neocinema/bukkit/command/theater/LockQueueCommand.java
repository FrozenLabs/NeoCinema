package com.neocinema.bukkit.command.theater;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.theater.Theater;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class LockQueueCommand extends TheaterOwnerCommandExecutor {

    public LockQueueCommand(NeoCinemaPlugin neoCinemaPlugin) {
        super(neoCinemaPlugin);
    }

    @Override
    public boolean onTheaterOwnerCommand(Player player, Command command, String label, String[] args, Theater theater) {
        boolean wasLocked = theater.getVideoQueue().isLocked();
        theater.getVideoQueue().setLocked(!wasLocked);

        if (wasLocked) {
            player.sendMessage(ChatColor.GOLD + "The video queue is now unlocked!");
        } else {
            player.sendMessage(ChatColor.GOLD + "The video queue is now locked!");
        }

        return true;
    }

}
