package com.neocinema.bukkit.theater;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.event.*;
import com.neocinema.bukkit.event.*;
import com.neocinema.bukkit.event.*;
import com.neocinema.bukkit.video.VideoInfo;
import com.neocinema.bukkit.theater.screen.PreviewScreen;
import com.neocinema.bukkit.theater.screen.Screen;
import com.neocinema.bukkit.util.ChatUtil;
import com.neocinema.bukkit.util.NetworkUtil;
import com.neocinema.bukkit.util.WorldGuardUtil;
import com.neocinema.bukkit.video.Video;
import com.neocinema.bukkit.video.queue.VideoQueue;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Theater {

    private final transient NeoCinemaPlugin neoCinemaPlugin;
    private final String id;
    private final String name;
    private final boolean hidden;
    private final Screen screen;
    private final List<PreviewScreen> previewScreens;
    private transient Video playing;
    private final transient VideoQueue videoQueue;
    private final transient List<ProtectedRegion> regions;
    private transient Set<Player> viewers;
    private final transient Set<Player> voteSkips;
    private transient BossBar titleBossBar;
    private transient BossBar timelineBossBar;

    public Theater(NeoCinemaPlugin neoCinemaPlugin, String id, String name, boolean hidden, Screen screen) {
        this.neoCinemaPlugin = neoCinemaPlugin;
        this.id = id;
        this.name = name;
        this.hidden = hidden;
        this.screen = screen;
        previewScreens = new ArrayList<>();

        videoQueue = new VideoQueue(neoCinemaPlugin, this);
        regions = WorldGuardUtil.guessTheaterRegions(this);

        if (regions.isEmpty() && this.neoCinemaPlugin.getneocinemaConfig().autogenCubicRegions) {
            Location screenLoc = this.getScreen().getLocation();
            Location cornerA = screenLoc.subtract(100,32,100);
            Location cornerB = screenLoc.add(100,32,100);
            ProtectedCuboidRegion newRegion = new ProtectedCuboidRegion("autogened_theater_" + id,
                    BlockVector3.at(cornerA.getBlockX(), cornerA.getBlockY(), cornerA.getBlockZ()),
                    BlockVector3.at(cornerB.getBlockX(), cornerB.getBlockY(), cornerB.getBlockZ()));
            regions.add(newRegion);
            neoCinemaPlugin.getLogger().info("Automatically generated a cuboid region with id " + newRegion.getId() + " as needed corners -> " + cornerA + " and " + cornerB);
        }

        if (regions.isEmpty()) {
            neoCinemaPlugin.getLogger().info("Theater '" + id + "' has no WorldGuard region");
        }

        viewers = new HashSet<>();
        voteSkips = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isHidden() {
        return hidden;
    }

    public Screen getScreen() {
        return screen;
    }

    public void addPreviewScreen(PreviewScreen previewScreen) {
        previewScreens.add(previewScreen);
    }

    public void removePreviewScreen(PreviewScreen previewScreen) {
        previewScreens.remove(previewScreen);
    }

    public List<PreviewScreen> getPreviewScreens() {
        return previewScreens;
    }

    public Video getPlaying() {
        return playing;
    }

    public void setPlaying(Video playing) {
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing != null;
    }

    public void forceSkip() {
        playing = null;
        voteSkips.clear();
        titleBossBar.removeAll();
        titleBossBar = null;
        timelineBossBar.removeAll();
        timelineBossBar = null;

        for (Player player : neoCinemaPlugin.getServer().getOnlinePlayers()) {
            sendUpdatePreviewScreensPacket(player);
        }
    }

    public VideoQueue getVideoQueue() {
        return videoQueue;
    }

    public List<ProtectedRegion> getRegions() {
        return regions;
    }

    public boolean regionsContain(Location location) {
        // TODO: also check world?
        for (ProtectedRegion region : regions) {
            if (region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                return true;
            }
        }
        return false;
    }

    public Set<Player> getViewers() {
        return viewers;
    }

    public Set<Player> getVoteSkips() {
        return voteSkips;
    }

    public boolean addVoteSkip(Player player) {
        if (isViewer(player) && voteSkips.add(player)) {
            PlayerVoteSkipEvent event = new PlayerVoteSkipEvent(player, this);
            neoCinemaPlugin.getServer().getPluginManager().callEvent(event);
            return true;
        } else {
            return false;
        }
    }

    public int getRequiredVoteSkips() {
        if (viewers.size() < 2) return 1;
        else if (viewers.size() == 2) return 2;
        return (int) ((2D / 3D) * viewers.size());
    }

    public void updateViewers() {
        Set<Player> previousViewers = getViewers();
        Set<Player> newViewers = new HashSet<>();

        for (ProtectedRegion region : regions) {
            newViewers.addAll(WorldGuardUtil.getPlayersInRegion(region));
        }

        viewers = newViewers;

        // Check for players who left the theater
        for (Player previousViewer : previousViewers) {
            if (!getViewers().contains(previousViewer)) {
                // Player left the theater
                PlayerLeaveTheaterEvent event = new PlayerLeaveTheaterEvent(previousViewer, this);
                neoCinemaPlugin.getServer().getPluginManager().callEvent(event);

                NetworkUtil.sendUnloadScreenPacket(neoCinemaPlugin, previousViewer, getScreen());
            }
        }

        // Check for players who entered the theater
        for (Player viewer : getViewers()) {
            if (!previousViewers.contains(viewer)) {
                // Player entered the theater
                PlayerEnterTheaterEvent event = new PlayerEnterTheaterEvent(viewer, this);
                neoCinemaPlugin.getServer().getPluginManager().callEvent(event);

                if (isPlaying()) {
                    NetworkUtil.sendLoadScreenPacket(neoCinemaPlugin, viewer, getScreen(), getPlaying());
                    NetworkUtil.sendVideoQueueStatePacket(neoCinemaPlugin, viewer, getVideoQueue());
                }
            }
        }
    }

    public void updateVoteSkips() {
        if (viewers.isEmpty()) {
            voteSkips.clear();
        } else {
            // If no longer a viewer, remove the vote skip
            voteSkips.removeIf(skipper -> !isViewer(skipper));

            if (voteSkips.size() >= getRequiredVoteSkips()) {
                TheaterVideoVoteSkippedEvent event = new TheaterVideoVoteSkippedEvent(this);
                neoCinemaPlugin.getServer().getPluginManager().callEvent(event);
                forceSkip();
            }
        }
    }

    public boolean isViewer(Player player) {
        return getViewers().contains(player);
    }

    public void reset() {
        getVideoQueue().setLocked(false);
        getVideoQueue().clear();
        forceSkip();
    }

    public void showBossBars(NeoCinemaPlugin neoCinemaPlugin, Player player) {
        if (isPlaying() && isViewer(player)) {
            if (titleBossBar.getPlayers().contains(player)
                    || timelineBossBar.getPlayers().contains(player)) {
                return;
            }

            titleBossBar.addPlayer(player);
            timelineBossBar.addPlayer(player);

            // Remove the boss bars 10 seconds later
            neoCinemaPlugin.getServer().getScheduler().runTaskLater(neoCinemaPlugin, () -> {
                if (player.isOnline()) {
                    if (titleBossBar != null) {
                        titleBossBar.removePlayer(player);
                    }

                    if (timelineBossBar != null) {
                        timelineBossBar.removePlayer(player);
                    }
                }
            }, 10 * 20L);
        }
    }

    public void tick(NeoCinemaPlugin neoCinemaPlugin) {
        boolean callStartVideoEvent = false;

        // Check if the video has ended
        if (isPlaying()) {
            if (playing.hasEnded()) {
                forceSkip();
            }
        }

        // Check the queue if there is no video playing
        if (!isPlaying()) {
            if (videoQueue.hasNext()) {
                // Set next video
                playing = videoQueue.poll();
                playing.start();

                callStartVideoEvent = true;

                for (Player viewer : viewers) {
                    NetworkUtil.sendLoadScreenPacket(neoCinemaPlugin, viewer, screen, playing);
                }

                for (Player player : neoCinemaPlugin.getServer().getOnlinePlayers()) {
                    sendUpdatePreviewScreensPacket(player);
                }
            } else {
                for (Player viewer : viewers) {
                    NetworkUtil.sendUnloadScreenPacket(neoCinemaPlugin, viewer, screen);
                }
            }
        }

        updateViewers();
        updateVoteSkips();

        // Update boss bars
        if (isPlaying()) {
            if (titleBossBar == null) {
                String name = playing.getVideoInfo().getTitle();
                BarColor barColor = BarColor.WHITE;
                BarStyle barStyle = BarStyle.SOLID;
                titleBossBar = neoCinemaPlugin.getServer().createBossBar(name, barColor, barStyle);
            }

            if (timelineBossBar == null) {
                BarColor barColor = BarColor.WHITE;
                BarStyle barStyle = BarStyle.SOLID;
                timelineBossBar = neoCinemaPlugin.getServer().createBossBar("", barColor, barStyle);
            }

            // Update boss bars
            titleBossBar.setTitle(ChatUtil.SECONDARY_COLOR + playing.getVideoInfo().getTitle());
            timelineBossBar.setTitle(playing.getTimeString());

            if (playing.getVideoInfo().isLivestream()) {
                timelineBossBar.setProgress(1.0);
            } else {
                double complete = playing.getPercentageComplete();
                if (complete < 1.0) {
                    timelineBossBar.setProgress(complete);
                }
            }
        }

        // Wait until the end of the tick to call start video event to avoid NPE
        if (callStartVideoEvent) {
            TheaterStartVideoEvent event = new TheaterStartVideoEvent(this);
            neoCinemaPlugin.getServer().getPluginManager().callEvent(event);
        }
    }

    public void sendUpdatePreviewScreensPacket(Player player) {
        VideoInfo videoInfo = playing == null ? null : playing.getVideoInfo();

        for (PreviewScreen previewScreen : previewScreens) {
            NetworkUtil.sendUpdatePreviewScreenPacket(neoCinemaPlugin, player, previewScreen, videoInfo);
        }
    }

}
