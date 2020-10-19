package me.aleksilassila.islands.commands.subcommands;

import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.commands.Subcommand;
import me.aleksilassila.islands.utils.Messages;
import me.aleksilassila.islands.utils.Permissions;
import org.bukkit.entity.Player;

import java.util.List;

public class deleteSubcommand extends Subcommand {
    private final Islands plugin;
    private final IslandLayout layout;

    public deleteSubcommand(Islands plugin) {
        this.plugin = plugin;
        this.layout = plugin.layout;
    }

    @Override
    public void onCommand(Player player, String[] args, boolean confirmed) {
        if (!Permissions.checkPermission(player, Permissions.command.delete)) {
            player.sendMessage(Messages.error.NO_PERMISSION);
            return;
        }

        if (!player.getWorld().equals(plugin.islandsWorld)) {
            player.sendMessage(Messages.error.WRONG_WORLD);
            return;
        }

        String islandId = layout.getIslandId(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

        if (islandId == null) {
            player.sendMessage(Messages.error.UNAUTHORIZED);
            return;
        }

        if (!layout.getUUID(islandId).equals(player.getUniqueId().toString())
                && !Permissions.checkPermission(player, Permissions.bypass.delete)) {
            player.sendMessage(Messages.error.UNAUTHORIZED);
            return;
        }

        if (!confirmed) {
            player.sendMessage(Messages.info.CONFIRM);
            return;
        }

        layout.deleteIsland(islandId);
        player.sendMessage(Messages.success.DELETED);
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return null;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String help() {
        return Messages.help.DELETE;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
