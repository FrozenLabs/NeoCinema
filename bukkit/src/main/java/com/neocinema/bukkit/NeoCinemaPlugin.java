package com.neocinema.bukkit;

import com.neocinema.bukkit.command.HistoryCommand;
import com.neocinema.bukkit.command.VolumeCommand;
import com.neocinema.bukkit.command.theater.*;
import com.neocinema.bukkit.command.theater.*;
import dev.lunasa.cinema.bukkit.command.theater.*;
import com.neocinema.bukkit.listener.PlayerJoinQuitListener;
import com.neocinema.bukkit.listener.PlayerTheaterListener;
import com.neocinema.bukkit.listener.PlayerVideoTimelineListener;
import com.neocinema.bukkit.player.PlayerDataManager;
import com.neocinema.bukkit.storage.VideoStorage;
import com.neocinema.bukkit.storage.sql.MySQLDriver;
import com.neocinema.bukkit.storage.sql.SQLDriver;
import com.neocinema.bukkit.storage.sql.SQLiteDriver;
import com.neocinema.bukkit.storage.sql.video.SQLVideoStorage;
import com.neocinema.bukkit.task.PlayerListUpdateTask;
import com.neocinema.bukkit.theater.TheaterManager;
import com.neocinema.bukkit.util.NetworkUtil;
import com.neocinema.bukkit.util.ProtocolLibUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class NeoCinemaPlugin extends JavaPlugin {

    private NeoCinemaConfig neoCinemaConfig;
    private TheaterManager theaterManager;
    private VideoStorage videoStorage;
    private PlayerDataManager playerDataManager;

    public NeoCinemaConfig getneocinemaConfig() {
        return neoCinemaConfig;
    }

    public TheaterManager getTheaterManager() {
        return theaterManager;
    }

    public VideoStorage getVideoStorage() {
        return videoStorage;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();

        neoCinemaConfig = new NeoCinemaConfig();
        neoCinemaConfig.youtubeDataApiKey = getConfig().getString("youtube-data-api-key");
        neoCinemaConfig.enableTabTheaterList = getConfig().getBoolean("enable-tab-theater-list");
        neoCinemaConfig.useMysql = getConfig().getBoolean("storage.mysql.use");
        neoCinemaConfig.mysqlHost = getConfig().getString("storage.mysql.host");
        neoCinemaConfig.mysqlPort = getConfig().getInt("storage.mysql.port");
        neoCinemaConfig.mysqlDatabase = getConfig().getString("storage.mysql.database");
        neoCinemaConfig.mysqlUsername = getConfig().getString("storage.mysql.username");
        neoCinemaConfig.mysqlPassword = getConfig().getString("storage.mysql.password");

        neoCinemaConfig.autogenCubicRegions = getConfig().getBoolean("autogenCubicRegions");

        if (neoCinemaConfig.youtubeDataApiKey.length() != 39) {
            getLogger().warning("Invalid YouTube Data API V3 key. YouTube videos will not be able to be requested.");
        }

        theaterManager = new TheaterManager(this);
        theaterManager.loadFromConfig(Objects.requireNonNull(getConfig().getConfigurationSection("theaters")));

        SQLDriver sqlDriver = null;

        if (neoCinemaConfig.useMysql) {
            sqlDriver = new MySQLDriver(neoCinemaConfig);
        } else if (neoCinemaConfig.useSqlite) {
            File dbFile = new File(getDataFolder(), "video_storage.db");
            try {
                sqlDriver = new SQLiteDriver(dbFile);
            } catch (IOException ignored) {
                getLogger().warning("Unable to create or load database file");
            }
        }

        if (sqlDriver == null) {
            getLogger().warning("Could not initialize video storage");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            videoStorage = new SQLVideoStorage(sqlDriver);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        playerDataManager = new PlayerDataManager(this);

        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerTheaterListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerVideoTimelineListener(this), this);
        getServer().getScheduler().runTaskTimer(this, () -> theaterManager.tickTheaters(), 20L, 20L);

        if (neoCinemaConfig.enableTabTheaterList) {
            getServer().getScheduler().runTaskTimer(this, new PlayerListUpdateTask(this), 20L, 20L);
        }

        getCommand("request").setExecutor(new RequestCommand(this));
        getCommand("forceskip").setExecutor(new ForceSkipCommand(this));
        getCommand("voteskip").setExecutor(new VoteSkipCommand(this));
        getCommand("lockqueue").setExecutor(new LockQueueCommand(this));
        getCommand("volume").setExecutor(new VolumeCommand(this));
        getCommand("playing").setExecutor(new PlayingCommand(this));
        getCommand("history").setExecutor(new HistoryCommand(this));

        NetworkUtil.registerChannels(this);
        ProtocolLibUtil.registerSoundPacketListener(this);
    }

}
