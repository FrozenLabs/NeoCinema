package com.neocinema.bukkit.command.theater;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.theater.Theater;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class ForceSkipCommand extends TheaterOwnerCommandExecutor {

    public ForceSkipCommand(NeoCinemaPlugin neoCinemaPlugin) {
        super(neoCinemaPlugin);
    }

    @Override
    public boolean onTheaterOwnerCommand(Player player, Command command, String label, String[] args, Theater theater) {
        if (theater.isPlaying()) {
            theater.forceSkip();
            player.sendMessage(ChatColor.GOLD + "The video has been force skipped!");
        } else {
            player.sendMessage(ChatColor.RED + "This theater is not playing anything!");
        }

        return true;
    }
}
