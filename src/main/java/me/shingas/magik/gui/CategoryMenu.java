package me.shingas.magik.gui;

import me.shingas.magik.managers.MagicManager;
import me.shingas.magik.utils.ItemBuilder;
import me.shingas.magik.utils.Mini;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public class CategoryMenu implements InventoryHolder {

    private final Inventory inventory;
    private final MagicManager magicManager;

    public CategoryMenu(MagicManager magicManager) {
        this.magicManager = magicManager;
        inventory = Bukkit.createInventory(
                this,
                27,
                Mini.message("<dark_purple>Magik")
        );
        initializeItems();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private void initializeItems() {

        inventory.setItem(11,
                new ItemBuilder(Material.IRON_SWORD)
                        .name("<red><bold>Attack Magic")
                        .lore(List.of(
                                "<gray>Offensive spells.",
                                "",
                                "<yellow>Click to view."
                        ))
                        .build());

        inventory.setItem(13,
                new ItemBuilder(Material.GOLDEN_APPLE)
                        .name("<green><bold>Support Magic")
                        .lore(List.of(
                                "<gray>Helpful spells.",
                                "",
                                "<yellow>Click to view."
                        ))
                        .build());

        inventory.setItem(15,
                new ItemBuilder(Material.ENDER_PEARL)
                        .name("<aqua><bold>Utility Magic")
                        .lore(List.of(
                                "<gray>Movement and utility.",
                                "",
                                "<yellow>Click to view."
                        ))
                        .build());
    }

}
