package me.shingas.magik.listeners;

import me.shingas.magik.managers.StormManager;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

public class StormListener implements Listener {

    private final StormManager stormManager;

    public StormListener(StormManager stormManager) {
        this.stormManager = stormManager;
    }

    @EventHandler
    public void onLightningDamage(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof LightningStrike strike))
            return;

        if (!(event.getEntity() instanceof Player player))
            return;

        UUID caster = stormManager.getCaster(strike);

        if (caster == null)
            return;

        if (caster.equals(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
