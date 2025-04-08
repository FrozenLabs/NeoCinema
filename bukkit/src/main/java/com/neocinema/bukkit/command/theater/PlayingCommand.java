package com.neocinema.bukkit.command.theater;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.theater.Theater;
import com.neocinema.bukkit.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class PlayingCommand extends TheaterCommandExecutor {
    public PlayingCommand(NeoCinemaPlugin neoCinemaPlugin) {
        super(neoCinemaPlugin);
    }

    @Override
    public boolean onTheaterCommand(Player player, Command command, String label, String[] args, Theater theater) {
        ChatUtil.showPlaying(player, theater, false);
        return true;
    }
}
