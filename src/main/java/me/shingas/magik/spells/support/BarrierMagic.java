package me.shingas.magik.spells.support;

import me.shingas.magik.magic.CastType;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class BarrierMagic extends Magic {

    private static final double RADIUS = 4.0;
    private static final int DURATION_TICKS = 6 * 20;

    public BarrierMagic() {
        super(
                "barrier",
                "Barrier",
                Material.SHIELD,
                MagicCategory.SUPPORT,
                List.of(
                        "<gray>Creates a blue barrier that",
                        "<gray>pushes nearby entities away."
                ),
                CastType.RIGHT_CLICK,
                6000L,
                60000L
        );
    }

    @Override
    public void cast(MagicContext context) {
        Player player = context.getPlayer();
        Location barrierCenter = player.getLocation().clone().add(0, 1, 0);
        java.util.UUID casterId = player.getUniqueId();
        final int[] elapsed = {0};

        Bukkit.getRegionScheduler().runAtFixedRate(
                context.getPlugin(),
                barrierCenter,
                task -> {
                    if (elapsed[0] >= DURATION_TICKS) {
                        task.cancel();
                        return;
                    }

                    spawnParticles(barrierCenter);
                    pushNearbyEntities(context, barrierCenter, casterId);
                    elapsed[0]++;
                },
                1L,
                1L
        );
    }

    private void spawnParticles(Location center) {
        Particle.DustOptions lightBlue = new Particle.DustOptions(
                Color.fromRGB(120, 210, 255),
                0.6F
        );

        for (int latitude = 0; latitude <= 16; latitude++) {
            double phi = Math.PI * latitude / 16;
            double y = Math.cos(phi) * RADIUS;
            double horizontalRadius = Math.sin(phi) * RADIUS;

            for (int longitude = 0; longitude < 32; longitude++) {
                double theta = Math.PI * 2 * longitude / 32;
                Location particle = center.clone().add(
                        Math.cos(theta) * horizontalRadius,
                        y,
                        Math.sin(theta) * horizontalRadius
                );
                center.getWorld().spawnParticle(Particle.DUST, particle, 1, lightBlue);
            }
        }
    }

    private void pushNearbyEntities(
            MagicContext context,
            Location center,
            java.util.UUID casterId
    ) {
        Vector origin = center.toVector();
        for (Entity entity : center.getWorld().getNearbyEntities(
                center,
                RADIUS,
                RADIUS,
                RADIUS
        )) {
            if (!(entity instanceof LivingEntity living)
                    || living.getUniqueId().equals(casterId)) {
                continue;
            }

            living.getScheduler().run(
                    context.getPlugin(),
                    task -> {
                        if (!living.isValid()) {
                            return;
                        }

                        Location targetLocation = living.getLocation();
                        if (targetLocation.distanceSquared(origin.toLocation(targetLocation.getWorld()))
                                > RADIUS * RADIUS) {
                            return;
                        }

                        Vector away = targetLocation.toVector().subtract(origin);
                        away.setY(0);
                        if (away.lengthSquared() < 0.001) {
                            away = new Vector(0, 0, 1);
                        } else {
                            away.normalize();
                        }

                        living.setVelocity(
                                living.getVelocity()
                                        .multiply(0.5)
                                        .add(away.multiply(0.6))
                                        .setY(0.25)
                        );
                    },
                    () -> {
                    }
            );
        }
    }
}
