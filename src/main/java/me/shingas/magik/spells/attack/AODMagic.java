package me.shingas.magik.spells.attack;

import me.shingas.magik.Magik;
import me.shingas.magik.magic.CastType;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class AODMagic extends Magic {

    public AODMagic() {
        super(
                "aod",
                "AOD",
                Material.NETHER_STAR,
                MagicCategory.ATTACK,
                List.of(
                        "<gray>Unleash a devastating cloud",
                        "<gray>of arcane destruction upon",
                        "<gray>everything before you."
                ),
                CastType.RIGHT_CLICK,
                5000L
        );
    }

    @Override
    public void cast(MagicContext context) {
        Player player = context.getPlayer();
        Magik plugin = context.getPlugin();
        World world = player.getWorld();
        double radius = 5;
        int durationTicks = 10 * 20; // 10 seconds
        int interval = 20; // 1 second per tick interval

        new BukkitRunnable() {
            int elapsed = 0;

            @Override
            public void run() {
                if (elapsed >= durationTicks) {
                    this.cancel();
                    return;
                }

                // Spawn red particles in a sphere
                for (int i = 0; i < 50; i++) {
                    double x = (Math.random() - 0.5) * 2 * radius;
                    double y = Math.random() * 2;
                    double z = (Math.random() - 0.5) * 2 * radius;
                    Location particleLoc = player.getLocation().add(x, y, z);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, new Particle.DustOptions(Color.RED, 1f));
                }

                // Damage all nearby entities except the caster
                for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                    if (entity instanceof LivingEntity living && living != player) {
                        double newHealth = living.getHealth() - 2; // 1 heart = 2 HP
                        living.setHealth(Math.max(newHealth, 0));
                    }
                }

                elapsed += interval;
            }
        }.runTaskTimer(plugin, 0L, interval);
    }

}
