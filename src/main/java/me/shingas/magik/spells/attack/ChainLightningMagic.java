package me.shingas.magik.spells.attack;

import me.shingas.magik.Magik;
import me.shingas.magik.magic.CastType;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ChainLightningMagic extends Magic {

    private static final int MAX_DISTANCE = 50;
    private static final int MAX_JUMPS = 6;
    private static final double CHAIN_RADIUS = 8.0;
    private static final double START_DAMAGE = 10.0;
    private Magik plugin;

    public ChainLightningMagic() {
        super(
                "chainlightning",
                "Chain Lightning",
                Material.LIGHTNING_ROD,
                MagicCategory.ATTACK,
                List.of(
                        "<gray>Strike your target with",
                        "<gray>lightning that jumps",
                        "<gray>between nearby enemies."
                ),
                CastType.RIGHT_CLICK,
                20000L
        );
    }

    @Override
    public void cast(MagicContext context) {
        Player player = context.getPlayer();
        this.plugin = context.getPlugin();
        LivingEntity first = getTarget(player);

        if (first == null) {
            return;
        }

        World world = player.getWorld();

        // Real lightning only on the first target
        LightningStrike strike = world.strikeLightning(first.getLocation());

        context.getStormManager().registerLightning(strike, player);

        strike(first.getLocation());

        // Damage first target
        first.damage(START_DAMAGE, player);

        // Begin chaining after a short delay
        new BukkitRunnable() {

            LivingEntity current = first;
            final Set<UUID> hit = new HashSet<>();
            double damage = START_DAMAGE * 0.8;
            int jumps = 1;

            @Override
            public void run() {

                hit.add(current.getUniqueId());

                LivingEntity next = findNextTarget(current, hit, player);

                if (next == null || jumps >= MAX_JUMPS) {
                    cancel();
                    return;
                }

                drawLightning(
                        current.getEyeLocation(),
                        next.getEyeLocation(),
                        () -> {
                            next.damage(damage, player);
                            strike(next.getLocation());

                            current = next;
                            damage *= 0.8;
                            jumps++;
                        });

            }

        }.runTaskTimer(plugin, 5L, 5L);
    }

    private LivingEntity getTarget(Player player) {

        List<Entity> nearby = player.getNearbyEntities(
                MAX_DISTANCE,
                MAX_DISTANCE,
                MAX_DISTANCE
        );

        LivingEntity closest = null;
        double bestDot = 0.98;

        Location eye = player.getEyeLocation();

        for (Entity entity : nearby) {

            if (!(entity instanceof LivingEntity living))
                continue;

            if (living == player)
                continue;

            Location target = living.getEyeLocation();

            var direction = target.toVector()
                    .subtract(eye.toVector())
                    .normalize();

            double dot = direction.dot(eye.getDirection());

            if (dot > bestDot) {

                if (eye.distanceSquared(target)
                        <= MAX_DISTANCE * MAX_DISTANCE) {

                    bestDot = dot;
                    closest = living;
                }
            }
        }

        return closest;
    }

    /**
     * Finds the closest entity that hasn't already been hit.
     */
    private LivingEntity findNextTarget(
            LivingEntity current,
            Set<UUID> hit,
            Player caster
    ) {

        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : current.getNearbyEntities(
                CHAIN_RADIUS,
                CHAIN_RADIUS,
                CHAIN_RADIUS
        )) {

            if (!(entity instanceof LivingEntity living))
                continue;

            if (hit.contains(living.getUniqueId()))
                continue;

            if (living.getUniqueId().equals(caster.getUniqueId()))
                continue;

            double distance = living.getLocation()
                    .distanceSquared(current.getLocation());

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = living;
            }
        }

        return nearest;
    }

    private void drawLightning(
            Location from,
            Location to,
            Runnable onHit
    ) {

        new BukkitRunnable() {

            double progress = 0;
            double speed = 0.35;

            final double distance = from.distance(to);

            final Vector direction = to.toVector()
                    .subtract(from.toVector())
                    .normalize();

            Vector perpendicular = direction.clone()
                    .crossProduct(new Vector(0, 1, 0));

            {
                if (perpendicular.lengthSquared() < 0.01) {
                    perpendicular = new Vector(1, 0, 0);
                }
                perpendicular.normalize();
            }

            @Override
            public void run() {

                progress += speed;
                speed += 0.12;

                if (progress >= distance) {
                    onHit.run();
                    cancel();
                    return;
                }

                Location point = from.clone().add(direction.clone().multiply(progress));

                // Zig-zag offset
                point.add(
                        perpendicular.clone().multiply((Math.random() - 0.5) * 0.45)
                );

                // Tiny vertical movement
                point.setY(point.getY() + (Math.random() - 0.5) * 0.12);

                World world = point.getWorld();
                if (world == null) return;

                Particle.DustOptions dust = new Particle.DustOptions(
                        Math.random() < 0.2 ? Color.WHITE : Color.AQUA,
                        2f
                );

                // Main bolt
                world.spawnParticle(
                        Particle.ELECTRIC_SPARK,
                        point,
                        6,
                        0.03,
                        0.03,
                        0.03,
                        0
                );

                world.spawnParticle(
                        Particle.END_ROD,
                        point,
                        2,
                        0,
                        0,
                        0,
                        0
                );

                world.spawnParticle(
                        Particle.DUST,
                        point,
                        1,
                        0,
                        0,
                        0,
                        0,
                        dust
                );

                // Lightning branch
                if (Math.random() < 0.30) {

                    Vector branchDir = direction.clone()
                            .add(new Vector(
                                    (Math.random() - 0.5) * 2,
                                    (Math.random() - 0.5),
                                    (Math.random() - 0.5) * 2
                            ))
                            .normalize();

                    Location branch = point.clone();

                    for (int i = 0; i < 3; i++) {

                        branch.add(branchDir.clone().multiply(0.18));

                        world.spawnParticle(
                                Particle.ELECTRIC_SPARK,
                                branch,
                                1,
                                0,
                                0,
                                0,
                                0
                        );

                        world.spawnParticle(
                                Particle.END_ROD,
                                branch,
                                1,
                                0,
                                0,
                                0,
                                0
                        );
                    }
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void strike(Location location) {

        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(
                Particle.FLASH,
                location,
                2,
                Color.WHITE
        );

        world.spawnParticle(
                Particle.ELECTRIC_SPARK,
                location,
                40,
                0.5,
                0.5,
                0.5,
                0.15
        );

        world.spawnParticle(
                Particle.END_ROD,
                location,
                30,
                0.4,
                0.6,
                0.4,
                0
        );

        world.spawnParticle(
                Particle.DUST,
                location,
                25,
                0.4,
                0.4,
                0.4,
                0,
                new Particle.DustOptions(Color.AQUA, 2f)
        );

        world.playSound(
                location,
                Sound.ENTITY_LIGHTNING_BOLT_IMPACT,
                1f,
                1f
        );

        world.playSound(
                location,
                Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                0.5f,
                1.6f
        );
    }

}
