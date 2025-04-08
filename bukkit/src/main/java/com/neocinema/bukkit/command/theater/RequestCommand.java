package com.neocinema.bukkit.command.theater;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.service.VideoURLParser;
import com.neocinema.bukkit.theater.Theater;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class RequestCommand extends TheaterCommandExecutor {

    private final NeoCinemaPlugin neoCinemaPlugin;
    private final Set<Player> lock;

    public RequestCommand(NeoCinemaPlugin neoCinemaPlugin) {
        super(neoCinemaPlugin);
        this.neoCinemaPlugin = neoCinemaPlugin;
        lock = new HashSet<>();
    }

    @Override
    public boolean onTheaterCommand(Player player, Command command, String label, String[] args, Theater theater) {
        if (lock.contains(player)) {
            player.sendMessage(ChatColor.RED + "Wait to use this command again.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Invalid URL. /" + label + " <url>");
            player.sendMessage(ChatColor.RED + "Example: /" + label + " https://www.youtube.com/watch?v=dQw4w9WgXcQ");
            return true;
        }

        String url = args[0];
        VideoURLParser parser = new VideoURLParser(neoCinemaPlugin, url);

        parser.parse(player);

        if (!parser.found()) {
            player.sendMessage(ChatColor.RED + "This URL or video type is not supported.");
            return true;
        }

        if (!player.hasPermission(parser.getInfoFetcher().getPermission())) {
            player.sendMessage(ChatColor.RED + "You do not have permission to request videos.");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "Please wait...");

        lock.add(player);

        parser.getInfoFetcher().fetch().thenAccept(videoInfo -> {
            lock.remove(player);

            if (!player.isOnline()) return;

            if (!theater.isViewer(player)) {
                player.sendMessage(ChatColor.RED + "The video you requested was not queued because you left the theater.");
                return;
            }

            player.sendMessage(ChatColor.GOLD + videoInfo.toString());

            if (videoInfo == null) {
                player.sendMessage(ChatColor.RED + "Unable to fetch video information.");
                return;
            }

            theater.getVideoQueue().processPlayerRequest(videoInfo, player);
        });

        return true;
    }

}
