package com.neocinema.bukkit.command;

import com.neocinema.bukkit.NeoCinemaPlugin;
import com.neocinema.bukkit.util.NetworkUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VolumeCommand implements CommandExecutor {

    private final NeoCinemaPlugin neoCinemaPlugin;

    public VolumeCommand(NeoCinemaPlugin neoCinemaPlugin) {
        this.neoCinemaPlugin = neoCinemaPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;
        NetworkUtil.sendOpenSettingsScreenPacket(neoCinemaPlugin, player);
        return true;
    }

}
