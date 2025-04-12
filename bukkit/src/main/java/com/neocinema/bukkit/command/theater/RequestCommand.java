package com.neocinema.bukkit.command.theater;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.service.VideoURLParser;
import com.neocinema.bukkit.service.infofetcher.FileVideoInfoFetcher;
import com.neocinema.bukkit.service.infofetcher.HLSVideoInfoFetcher;
import com.neocinema.bukkit.service.infofetcher.VideoInfoFetcher;
import com.neocinema.bukkit.theater.Theater;
import com.neocinema.bukkit.video.VideoInfo;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.neocinema.bukkit.video.Video.reduceFormattedDuration;

public class RequestCommand extends TheaterCommandExecutor implements TabCompleter {

    private final NeoCinemaPlugin neoCinemaPlugin;
    private final Set<Player> lock;

    public RequestCommand(NeoCinemaPlugin neoCinemaPlugin) {
        super(neoCinemaPlugin);
        this.neoCinemaPlugin = neoCinemaPlugin;
        this.lock = new HashSet<>();
    }

    @Override
    public boolean onTheaterCommand(Player player, Command command, String label, String[] args, Theater theater) {
        if (lock.contains(player)) {
            player.sendMessage(ChatColor.RED + "Wait to use this command again.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Invalid URL. /" + label + " <url> [title]");
            return true;
        }

        String url = args[0];
        String title = args.length >= 2 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : null;

        VideoURLParser parser = new VideoURLParser(neoCinemaPlugin, url);
        parser.parse(player);

        if (!parser.found()) {
            player.sendMessage(ChatColor.RED + "Unsupported or unrecognized URL.");
            return true;
        }

        VideoInfoFetcher fetcher = parser.getInfoFetcher();

        if (!player.hasPermission(fetcher.getPermission())) {
            player.sendMessage(ChatColor.RED + "You do not have permission to request this video.");
            return true;
        }

        if ((fetcher instanceof FileVideoInfoFetcher || fetcher instanceof HLSVideoInfoFetcher) && title == null) {
            player.sendMessage(ChatColor.RED + "Please provide a title for the video.");
            player.sendMessage(ChatColor.RED + "Usage: /" + label + " <url> <title>");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "Please wait...");
        lock.add(player);

        fetcher.fetch().thenAccept(videoInfo -> {
            lock.remove(player);

            if (!player.isOnline()) return;
            if (videoInfo == null) {
                player.sendMessage(ChatColor.RED + "Unable to fetch video information.");
                return;
            }

            if (title != null) videoInfo.setTitle(title);

            if (!theater.isViewer(player)) {
                player.sendMessage(ChatColor.RED + "You left the theater before your request was added.");
                return;
            }

            player.sendMessage(ChatColor.GOLD + "> Queued: " + videoInfo.getTitle() + " (" + format(videoInfo.getDurationSeconds() * 1000L) + ")");
            theater.getVideoQueue().processPlayerRequest(videoInfo, player);
        });

        return true;
    }

    private String format(long durationMillis) {
        String currentDurationFormatted = DurationFormatUtils.formatDuration(durationMillis, "H:mm:ss");
        return reduceFormattedDuration(currentDurationFormatted);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        if (args.length == 2) {
            String url = args[0];
            VideoURLParser parser = new VideoURLParser(neoCinemaPlugin, url);
            parser.parse(null);
            if (parser.getType().equals("hls") || parser.getType().equals("file")) {
                return Collections.singletonList("<title>");
            }
        }

        return Collections.emptyList();
    }
}
