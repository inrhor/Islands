package me.aleksilassila.islands.GUIs;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.aleksilassila.islands.IslandLayout;
import me.aleksilassila.islands.Islands;
import me.aleksilassila.islands.utils.BiomeMaterials;
import me.aleksilassila.islands.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class VisitGUI extends PageGUI {
    private final Islands plugin;
    private final Player player;

    private int sort = 0;

    private final int PAGE_HEIGHT = 4; // < 1

    public VisitGUI(Islands plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    Gui getGui() {
        Gui gui = createPaginatedGUI(PAGE_HEIGHT, Messages.get("gui.visit.TITLE", sort), getPanes());

        StaticPane sort = new StaticPane(4, PAGE_HEIGHT - 1, 1, 1);

        sort.addItem(new GuiItem(createGuiItem(Material.REDSTONE, Messages.get("gui.visit.SORT", this.sort == 1 ? 0 : 1), false), event -> {
            toggleSort();
            event.getWhoClicked().closeInventory();
            getGui().show(event.getWhoClicked());
        }), 0, 0);

        gui.addPane(sort);

        return gui;
    }

    @Override
    Player getPlayer() {
        return player;
    }

    private List<StaticPane> getPanes() {
        List<StaticPane> panes = new ArrayList<>();

        Map<String, Map<String, String>> publicIslands = plugin.layout.getIslandsInfo(true);

        List<String> sortedSet = new ArrayList<>(publicIslands.keySet());

        // Sort islands
        if (sort == 0) { // Sort by date, oldest first
            sortedSet.sort(Comparator.comparingInt(a ->
                    IslandLayout.placement.getIslandIndex(new int[]{Integer.parseInt(a.split("x")[0]), Integer.parseInt(a.split("x")[1])})));
        } else { // Sort by name
            sortedSet.sort(Comparator.comparingInt(a -> publicIslands.get(a).get("name").charAt(0)));
        }

        StaticPane pane = new StaticPane(0, 0, 9, PAGE_HEIGHT - 1);

        int itemCount = 0;
        for (String islandId : sortedSet) {
            if (pane.getItems().size() >= (PAGE_HEIGHT - 1) * 9) {
                panes.add(pane);
                pane = new StaticPane(0, 0, 9, PAGE_HEIGHT - 1);
            }

            String displayName;

            try {
                displayName = Bukkit.getOfflinePlayer(UUID.fromString(publicIslands.get(islandId).get("owner"))).getName();
            } catch (Exception e) {
                displayName = "Server";
            }

            pane.addItem(new GuiItem(createGuiItem(BiomeMaterials.valueOf(publicIslands.get(islandId).get("material")).getMaterial(),
                        Messages.get("gui.visit.ISLAND_NAME", publicIslands.get(islandId).get("name")),
                        displayName.equals("Server"),
                        Messages.get("gui.visit.ISLAND_OWNER", displayName)),
                        event -> {
                            if (!(event.getWhoClicked() instanceof Player)) return; // Dunno if this is necessary in practice, cows don't click inventories

                            ((Player) event.getWhoClicked()).performCommand("visit " + publicIslands.get(islandId).get("name"));
                        }), (itemCount % (9 * (PAGE_HEIGHT - 1))) % 9, (itemCount % (9 * (PAGE_HEIGHT - 1))) / 9);
            itemCount++;
        }

        if (pane.getItems().size() > 0) panes.add(pane);

        return panes;
    }

    public void toggleSort() {
        sort = sort == 0 ? 1 : 0;
    }
}
