package me.aleksilassila.islands.commands;

import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.Main;
import me.aleksilassila.islands.generation.IslandGrid;
import me.aleksilassila.islands.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class IslandManagmentCommands extends ChatUtils implements CommandExecutor {
    private Main plugin;

    public IslandManagmentCommands(Main plugin) {
        this.plugin = plugin;

        plugin.getCommand("go").setExecutor(this);
        plugin.getCommand("island").setExecutor(this);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("create")) {
                    createIsland(player, args);

                    return true;
                } else if (args[0].equalsIgnoreCase("regenerate")) {
                    regenerateIsland(player, args);

                    return true;
                } else if (args[0].equalsIgnoreCase("give")) {
                    giveIsland(player, args);

                    return true;
                } else if (args[0].equalsIgnoreCase("name")) {
                    nameIsland(player, args);

                    return true;
                } else if (args[0].equalsIgnoreCase("unname")) {
                    unnameIsland(player, args);

                    return true;
                }
            }

            sendHelp(player);
        }

        return true;
    }

    private void giveIsland(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(Messages.Help.GIVE);
            return;
        }

        String islandId = null;

        try {
            islandId = plugin.islands.grid.getIslandId(player.getLocation());
        } catch (IslandGrid.IslandNotFound e) {
            player.sendMessage(Messages.Error.UNAUTHORIZED);
            return;
        }

        if (plugin.getIslandsConfig().getString("islands." + islandId + ".UUID").equals(player.getUniqueId().toString()) && plugin.getIslandsConfig().getInt("islands." + islandId + ".public") == 1) {
            plugin.getIslandsConfig().set("islands." + islandId + ".UUID", Bukkit.getPlayer(args[1]).getUniqueId().toString());
            plugin.saveIslandsConfig();

            plugin.getIslandsConfig().set("islands." + islandId + ".home", String.valueOf(plugin.islands.grid.getNumberOfIslands(Bukkit.getPlayer(args[1]).getUniqueId())));
            plugin.saveIslandsConfig();

            player.sendMessage(success("Island owner switched to " + args[1] + "."));
            return;
        } else {
            player.sendMessage(Messages.Error.UNAUTHORIZED);
        }

    }

    private void sendHelp(Player player) {
        player.sendMessage(success("Available /island subcommands:"));

        player.sendMessage(Messages.Help.CREATE);
        player.sendMessage(Messages.Help.REGENERATE);
        player.sendMessage(Messages.Help.NAME);
        player.sendMessage(Messages.Help.UNNAME);
        player.sendMessage(Messages.Help.GIVE);
    }

    private void unnameIsland(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(Messages.Help.UNNAME);
            return;
        }

        String islandId = null;

        try {
            islandId = plugin.islands.grid.getIslandId(player.getLocation());
        } catch (IslandGrid.IslandNotFound e) {
            player.sendMessage(Messages.Error.UNAUTHORIZED);
            return;
        }

        if (plugin.getIslandsConfig().getString("islands." + islandId + ".UUID").equals(player.getUniqueId().toString())) {
            plugin.getIslandsConfig().set("islands." + islandId + ".name", plugin.getIslandsConfig().getString("islands." + islandId + ".home"));
            plugin.getIslandsConfig().set("islands." + islandId + ".public", 0);
            plugin.saveIslandsConfig();

            player.sendMessage(success("Island unnamed and made private."));
            return;
        } else {
            player.sendMessage(Messages.Error.UNAUTHORIZED);
        }
    }

    private void nameIsland(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(Messages.Help.NAME);
            return;
        }

        String islandId = null;

        try {
            islandId = plugin.islands.grid.getIslandId(player.getLocation());
        } catch (IslandGrid.IslandNotFound e) {
            player.sendMessage(Messages.Error.UNAUTHORIZED);
            return;
        }

        if (plugin.getIslandsConfig().getString("islands." + islandId + ".UUID").equals(player.getUniqueId().toString())) {
            plugin.getIslandsConfig().set("islands." + islandId + ".name", args[1]);
            plugin.getIslandsConfig().set("islands." + islandId + ".public", 1);
            plugin.saveIslandsConfig();

            player.sendMessage(success("Island name changed to " + args[1] + ". Anyone with your island name can now visit it."));
            return;
        } else {
            player.sendMessage(Messages.Error.UNAUTHORIZED);
        }
    }

    private Biome getTargetBiome(String biome) {
         Biome targetBiome = null;

         for (Biome b : Biome.values()) {
             if (b.name().equalsIgnoreCase(biome)) {
                 targetBiome = b;
             }
         }

         return targetBiome;
    }

    private void createIsland(Player player, String[] args) {
        HashMap<Biome, List<Location>> availableLocations = plugin.islands.islandGeneration.biomes.availableLocations;

        if (args.length != 2) {
            player.sendMessage(Messages.Help.CREATE);

            for (Biome biome : availableLocations.keySet()) {
                if (availableLocations.get(biome).size() > 0) {
                    player.sendMessage(ChatColor.GOLD + biome.toString() + ChatColor.GREEN +  " has " + ChatColor.GOLD +  availableLocations.get(biome).size() + ChatColor.GREEN +  " island variations available.");
                }
            }

            return;
        }

        Biome targetBiome = getTargetBiome(args[1]);

        if (targetBiome == null) {
            player.sendMessage(Messages.Error.NO_BIOME);
            return;
        }

        if (!availableLocations.containsKey(targetBiome)) {
            player.sendMessage(Messages.Error.NO_LOCATIONS_FOR_BIOME);
            return;
        }

        try {
            String islandId = plugin.islands.createNewIsland(targetBiome, Islands.IslandSize.NORMAL, player.getUniqueId());

            player.sendMessage(Messages.Success.ISLAND_GEN);
            try {
                player.teleport(plugin.islands.grid.getIslandSpawn(islandId));

            } catch (IslandGrid.IslandNotFound e) {
                player.sendMessage(Messages.Error.ISLAND_GEN);
            }
        } catch (Islands.IslandsException e) {
            player.sendMessage(error(e.getMessage()));
        }

    }
    private void regenerateIsland(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(Messages.Help.REGENERATE);
            return;
        }

        Biome targetBiome = getTargetBiome(args[2]);

        if (targetBiome == null) {
            player.sendMessage(Messages.Error.NO_BIOME);
            return;
        }

        if (!plugin.islands.islandGeneration.biomes.availableLocations.containsKey(targetBiome)) {
            player.sendMessage(Messages.Error.NO_LOCATIONS_FOR_BIOME);
            return;
        }

        boolean success = plugin.islands.regenerateIsland(targetBiome, player.getUniqueId(), args[1]);

        if (success) {
            player.sendMessage(Messages.Success.ISLAND_GEN);
            try {
                player.teleport(plugin.islands.grid.getIslandSpawn(plugin.islands.grid.getPrivateIsland(player.getUniqueId(), args[1])));
            } catch (IslandGrid.IslandNotFound e) {
                player.sendMessage(Messages.Error.TELEPORT);
            }
        } else {
            player.sendMessage(Messages.Error.ISLAND_GEN);
        }
    }

    private static class Messages {
        public static class Error {
            public static final String UNAUTHORIZED = error("The island must be public and yours.");
            public static String ISLAND_GEN = error("Island regeneration failed.");
            public static String TELEPORT = error("Could not teleport.");
            public static String NO_BIOME = error("Biome not found.");
            public static String NO_LOCATIONS_FOR_BIOME = error("No available locations for specified biome.");
        }

        public static class Success {
            public static String ISLAND_GEN = success("Island regenerated successfully.");
        }

        public static class Help {
            public static String CREATE = ChatColor.GRAY + "/island create <biome>";
            public static String REGENERATE = ChatColor.GRAY + "/island regenerate <id/name> <biome>";
            public static String NAME = ChatColor.GRAY + "/island name <name> (You have to be on target island)";
            public static String UNNAME = ChatColor.GRAY + "/island unname (You have to be on target island)";
            public static String GIVE = ChatColor.GRAY + "/island give <name> (You have to be on target island)";
        }
    }
}
