package me.shingas.magik.gui;

import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.managers.MagicManager;
import me.shingas.magik.utils.ItemBuilder;
import me.shingas.magik.utils.Mini;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellBookMenu implements InventoryHolder {

    private final Inventory inventory;
    private final MagicManager manager;
    private final Map<Integer, Magic> magicSlots = new HashMap<>();

    public SpellBookMenu(
            MagicManager manager,
            Player player,
            MagicCategory category
    ) {
        this.manager = manager;
        this.inventory = Bukkit.createInventory(
                this,
                27,
                Mini.message(titleFor(category))
        );

        initializeItems(player, category);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private void initializeItems(Player player, MagicCategory category) {
        fillBackground();

        int slot = 0;
        for (Magic magic : manager.getAllByCategory(category)) {
            if (slot >= inventory.getSize()) {
                break;
            }

            boolean unlocked = manager.canUseMagic(player, magic);
            List<String> lore = new ArrayList<>(magic.getDescription());
            lore.add("");
            lore.add("<aqua>Cast Time: <yellow>" + formatTime(magic.getCastTimeMillis()));
            lore.add("<red>Cooldown: <yellow>" + formatTime(magic.getCooldownMillis()));
            lore.add("");
            lore.add(unlocked
                    ? "<yellow>Click to select."
                    : "<red>You have not unlocked this magic.");

            inventory.setItem(
                    slot,
                    new ItemBuilder(magic.getIcon())
                            .name((unlocked ? "<gold>" : "<dark_red>") + magic.getName())
                            .lore(lore)
                            .build()
            );
            magicSlots.put(slot, magic);
            slot++;
        }
    }

    private void fillBackground() {
        ItemBuilder filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .name("<dark_gray>");

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler.build());
        }
    }

    private String formatTime(long milliseconds) {
        if (milliseconds <= 0) {
            return "Instant";
        }

        long seconds = milliseconds / 1000;
        if (seconds % 60 == 0) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute" : " minutes");
        }

        return seconds + (seconds == 1 ? " second" : " seconds");
    }

    private String titleFor(MagicCategory category) {
        return switch (category) {
            case ATTACK -> "<red>Attack Magic";
            case SUPPORT -> "<green>Support Magic";
            case UTILITY -> "<aqua>Utility Magic";
        };
    }

    public Magic getMagic(int slot) {
        return magicSlots.get(slot);
    }
}
