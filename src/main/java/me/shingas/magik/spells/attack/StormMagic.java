package me.shingas.magik.spells.attack;

import me.shingas.magik.Magik;
import me.shingas.magik.magic.CastType;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import me.shingas.magik.managers.StormManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class StormMagic extends Magic {

    public StormMagic() {
        super(
                "storm",
                "Storm",
                Material.TRIDENT,
                MagicCategory.ATTACK,
                List.of(
                        "<gray>Summon a raging storm",
                        "<gray>that shocks and batters",
                        "<gray>everything around you."
                ),
                CastType.RIGHT_CLICK,
                30000L
        );
    }

    @Override
    public void cast(MagicContext context) {

        Player player = context.getPlayer();
        Magik plugin = context.getPlugin();
        StormManager storm = context.getStormManager();
        World world = player.getWorld();

        double radius = 15;
        int duration = 200;
        Location center = player.getLocation().clone();
        UUID casterId = player.getUniqueId();

        ThreadLocalRandom random = ThreadLocalRandom.current();

        final int[] ticks = {0};
        Bukkit.getRegionScheduler().runAtFixedRate(plugin, center, task -> {
            if (ticks[0] >= duration) {
                task.cancel();
                return;
            }

            Location strikeLoc = center.clone();
            strikeLoc.add(random.nextDouble(-radius, radius), 0, random.nextDouble(-radius, radius));
            Bukkit.getRegionScheduler().run(plugin, strikeLoc, strikeTask -> {
                strikeLoc.setY(world.getHighestBlockYAt(strikeLoc));
                LightningStrike strike = world.strikeLightning(strikeLoc);
                storm.registerLightning(strike, casterId);
            });
            ticks[0] += 10;
        }, 1L, 10L);
    }

}
