package me.shingas.magik.listeners;


import me.shingas.magik.magic.CastTrigger;
import me.shingas.magik.managers.MagicManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractListener implements Listener {

    private final MagicManager magicManager;

    public PlayerInteractListener(MagicManager magicManager) {
        this.magicManager = magicManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND)
            return;

        Player player = event.getPlayer();
        if (event.getAction().isRightClick()) {
            magicManager.castHeldMagic(player, CastTrigger.RIGHT);
        } else if (event.getAction().isLeftClick()) {
            magicManager.castHeldMagic(player, CastTrigger.LEFT);
        }
    }
}
