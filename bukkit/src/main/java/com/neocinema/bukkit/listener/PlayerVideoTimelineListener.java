package com.neocinema.bukkit.listener;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.theater.StaticTheater;
import com.neocinema.bukkit.theater.Theater;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class PlayerVideoTimelineListener implements Listener {

    private final NeoCinemaPlugin neoCinemaPlugin;

    public PlayerVideoTimelineListener(NeoCinemaPlugin neoCinemaPlugin) {
        this.neoCinemaPlugin = neoCinemaPlugin;
    }

    @EventHandler
    public void onPlayerChangeItem(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Theater theater = neoCinemaPlugin.getTheaterManager().getCurrentTheater(player);

        if (theater == null || theater instanceof StaticTheater) {
            return;
        }

        if (!theater.isPlaying()) {
            return;
        }

        theater.showBossBars(neoCinemaPlugin, player);
    }

}
