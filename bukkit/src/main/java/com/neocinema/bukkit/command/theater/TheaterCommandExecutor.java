package com.neocinema.bukkit.command.theater;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.theater.StaticTheater;
import com.neocinema.bukkit.theater.Theater;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a command which is run while inside a theater
 */
public abstract class TheaterCommandExecutor implements CommandExecutor {

    private final NeoCinemaPlugin neoCinemaPlugin;

    public TheaterCommandExecutor(NeoCinemaPlugin neoCinemaPlugin) {
        this.neoCinemaPlugin = neoCinemaPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        Theater theater = neoCinemaPlugin.getTheaterManager().getCurrentTheater(player);

        if (theater == null || theater instanceof StaticTheater) {
            player.sendMessage(ChatColor.RED + "You must be in a theater to use this command.");
            return true;
        }

        return onTheaterCommand(player, command, label, args, theater);
    }

    public abstract boolean onTheaterCommand(Player player, Command command, String label, String[] args, Theater theater);

}
