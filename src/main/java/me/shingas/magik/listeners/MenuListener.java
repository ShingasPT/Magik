package me.shingas.magik.listeners;

import me.shingas.magik.gui.SpellBookMenu;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.managers.MagicManager;
import me.shingas.magik.utils.Mini;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.entity.Player;

public class MenuListener implements Listener {

    private final MagicManager manager;

    public MenuListener(MagicManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (!(event.getInventory().getHolder() instanceof SpellBookMenu menu))
            return;

        event.setCancelled(true);
        handleMagicClick(player, event, menu);
    }

    private void handleMagicClick(Player player,
                                  InventoryClickEvent event,
                                  SpellBookMenu menu) {

        Magic magic =
                menu.getMagic(event.getRawSlot());

        if (magic == null)
            return;

        if (!manager.canUseMagic(player, magic)) {

            player.sendMessage(
                    Mini.message(
                            "<red>You have not unlocked this magic."
                    )
            );

            player.closeInventory();
            return;
        }

        manager.applyMagic(player, magic);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof SpellBookMenu) {
            event.setCancelled(true);
        }
    }
}
