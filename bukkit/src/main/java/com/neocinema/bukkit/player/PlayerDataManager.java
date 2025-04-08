package com.neocinema.bukkit.player;

import com.neocinema.bukkit.NeoCinemaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final NeoCinemaPlugin neoCinemaPlugin;
    private final Map<UUID, PlayerData> cache;

    public PlayerDataManager(NeoCinemaPlugin neoCinemaPlugin) {
        this.neoCinemaPlugin = neoCinemaPlugin;
        cache = new ConcurrentHashMap<>();
    }

    public PlayerData getData(UUID playerId) {
        if (cache.containsKey(playerId)) return cache.get(playerId);
        PlayerData playerData = new PlayerData(playerId);
        neoCinemaPlugin.getVideoStorage().loadVideoRequests(playerId).thenAccept(videoRequests -> {
            PlayerRequestHistory requestHistory = new PlayerRequestHistory(playerId, videoRequests);
            playerData.setRequestHistory(requestHistory, neoCinemaPlugin);
        });
        cache.put(playerId, playerData);
        return playerData;
    }

    public void unload(UUID playerId) {
        cache.remove(playerId);
    }

}
