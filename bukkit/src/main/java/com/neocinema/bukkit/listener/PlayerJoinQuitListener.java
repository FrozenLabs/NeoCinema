package com.neocinema.bukkit.listener;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.theater.Theater;
import com.neocinema.bukkit.theater.screen.Screen;
import com.neocinema.bukkit.util.NetworkUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerJoinQuitListener implements Listener {

    private final NeoCinemaPlugin neoCinemaPlugin;

    public PlayerJoinQuitListener(NeoCinemaPlugin neoCinemaPlugin) {
        this.neoCinemaPlugin = neoCinemaPlugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Run 3 seconds after login
        neoCinemaPlugin.getServer().getScheduler().runTaskLater(neoCinemaPlugin, () -> {
            if (!player.isOnline()) return;

            neoCinemaPlugin.getPlayerDataManager().getData(player.getUniqueId());

            NetworkUtil.sendRegisterServicesPacket(neoCinemaPlugin, player);

            List<Screen> screens = neoCinemaPlugin.getTheaterManager().getTheaters()
                    .stream()
                    .map(Theater::getScreen)
                    .collect(Collectors.toList());

            NetworkUtil.sendScreensPacket(neoCinemaPlugin, player, screens);

            neoCinemaPlugin.getTheaterManager().getTheaters().forEach(t -> t.sendUpdatePreviewScreensPacket(player));
        }, 20L * 3);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        neoCinemaPlugin.getPlayerDataManager().unload(player.getUniqueId());
    }

}
