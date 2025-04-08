package com.neocinema.bukkit.player;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.video.VideoInfo;
import com.neocinema.bukkit.video.VideoRequest;
import com.neocinema.bukkit.util.NetworkUtil;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerData {

    private final UUID playerId;
    private PlayerRequestHistory requestHistory;

    public PlayerData(UUID playerId) {
        this.playerId = playerId;
        requestHistory = new PlayerRequestHistory(playerId);
    }

    public void setRequestHistory(PlayerRequestHistory requestHistory, NeoCinemaPlugin neoCinemaPlugin) {
        this.requestHistory = requestHistory;
        Player player = neoCinemaPlugin.getServer().getPlayer(playerId);
        if (player != null) {
            NetworkUtil.sendVideoListHistorySplitPacket(neoCinemaPlugin, player, getHistoryListEntries());
        }
    }

    public void addHistory(VideoInfo videoInfo, NeoCinemaPlugin neoCinemaPlugin) {
        requestHistory.addRequest(videoInfo, neoCinemaPlugin);
        VideoRequest request = requestHistory.getRequestFor(videoInfo);
        Player player = neoCinemaPlugin.getServer().getPlayer(playerId);
        if (player != null) {
            NetworkUtil.sendVideoListHistorySplitPacket(neoCinemaPlugin, player, List.of(request));
        }
    }

    public void deleteHistory(VideoInfo videoInfo, NeoCinemaPlugin neoCinemaPlugin) {
        VideoRequest request = requestHistory.getRequestFor(videoInfo);
        if (request != null) {
            request.setHidden(true);
            neoCinemaPlugin.getVideoStorage().saveVideoRequest(request);
        }
    }

    public List<VideoRequest> getHistoryListEntries() {
        return requestHistory.getRequests().stream()
                .filter(request -> !request.isHidden())
                .collect(Collectors.toList());
    }

}
