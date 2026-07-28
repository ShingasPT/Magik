package me.shingas.magik.managers;

import me.shingas.magik.Magik;
import org.bukkit.Bukkit;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StormManager {

    private final Magik plugin;
    private final Map<UUID, UUID> lightningCasters = new HashMap<>();

    public StormManager(Magik plugin) {
        this.plugin = plugin;
    }

    public void registerLightning(LightningStrike strike, Player caster) {

        lightningCasters.put(
                strike.getUniqueId(),
                caster.getUniqueId()
        );

        // Clean up after 2 seconds
        Bukkit.getScheduler().runTaskLater(plugin,
                () -> lightningCasters.remove(strike.getUniqueId()),
                40L);
    }

    public UUID getCaster(LightningStrike strike) {
        return lightningCasters.get(strike.getUniqueId());
    }
}
