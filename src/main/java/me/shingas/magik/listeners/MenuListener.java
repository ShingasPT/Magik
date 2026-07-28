package me.shingas.magik.listeners;

import me.shingas.magik.gui.CategoryMenu;
import me.shingas.magik.gui.MagicMenu;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.managers.MagicManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener implements Listener {

    private final MagicManager manager;

    public MenuListener(MagicManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (!(event.getInventory().getHolder() instanceof InventoryHolder holder))
            return;

        if (holder instanceof CategoryMenu) {

            handleCategoryClick(player, event);
            event.setCancelled(true);

        } else if (holder instanceof MagicMenu magicMenu) {

            handleMagicClick(player, event, magicMenu);
            event.setCancelled(true);

        }
    }

    private void handleCategoryClick(Player player, InventoryClickEvent event) {
        switch (event.getRawSlot()) {
            case 11 ->
                    new MagicMenu(manager, MagicCategory.ATTACK).open(player);
            case 13 ->
                    new MagicMenu(manager, MagicCategory.SUPPORT).open(player);
            case 15 ->
                    new MagicMenu(manager, MagicCategory.UTILITY).open(player);
        }
    }

    private void handleMagicClick(Player player,
                                  InventoryClickEvent event,
                                  MagicMenu menu) {

        if (event.getRawSlot() == MagicMenu.BACK_SLOT) {
            new CategoryMenu(manager).open(player);
            return;
        }

        Magic magic = menu.getMagic(event.getRawSlot());

        if (magic == null) return;

        manager.applyMagic(player, magic);
    }
}
