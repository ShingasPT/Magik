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
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ChainLightningMagic extends Magic {

    private static final int MAX_DISTANCE = 50;
    // Total number of entities the bolt can hit (first target + chained jumps)
    private static final int MAX_JUMPS = 5;
    private static final double CHAIN_RADIUS = 12.0;
    private static final double START_DAMAGE = 16.0;
    // Damage retained per jump - higher means the chain stays dangerous for longer
    private static final double CHAIN_DAMAGE_FALLOFF = 0.85;
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
                30000L
        );
    }

    @Override
    public void cast(MagicContext context) {
        Player player = context.getPlayer();
        this.plugin = context.getPlugin();

        LivingEntity first = getTarget(player);

        if (first == null || !first.isValid() || first.isDead()) {
            return;
        }

        World world = player.getWorld();

        // The initial strike + damage must run on the region thread that
        // owns "first", otherwise entity-add side effects of damage()
        // (e.g. XP orbs on death) crash with
        // "Cannot add entity off-main thread" on regionized servers
        // (Folia / ShreddedPaper).
        first.getScheduler().run(plugin, scheduledTask -> {

            LightningStrike lightningStrike =
                    world.strikeLightning(first.getLocation());

            context.getStormManager().registerLightning(
                    lightningStrike,
                    player
            );

            strike(first.getLocation());
            first.damage(START_DAMAGE, player);

            startChain(player, first);

        }, null);
    }

    private void startChain(Player player, LivingEntity first) {

        new BukkitRunnable() {

            private LivingEntity current = first;
            private Location currentLocation = first.getEyeLocation().clone();
            private final Set<UUID> hit = new HashSet<>();

            private double damage = START_DAMAGE * CHAIN_DAMAGE_FALLOFF;
            private int jumps = 1;
            private boolean animating = false;

            @Override
            public void run() {
                // Wait until the current lightning animation finishes
                if (animating) {
                    return;
                }

                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (current == null) {

                    cancel();
                    return;
                }

                // The current target has already been struck
                hit.add(current.getUniqueId());

                if (jumps >= MAX_JUMPS) {
                    cancel();
                    return;
                }

                LivingEntity next =
                        findNextTarget(currentLocation, hit, player);

                if (next == null) {
                    cancel();
                    return;
                }

                // Reserve the target immediately so it cannot be selected twice
                hit.add(next.getUniqueId());
                animating = true;

                Location from = currentLocation;
                Location to = next.getEyeLocation();

                drawLightning(from, to, () -> {

                    LivingEntity target = next;
                    double dmg = damage;

                    // Same reasoning as the initial strike: damage must be
                    // applied on the region thread that owns the target
                    // entity, not on this timer's (global) thread.
                    target.getScheduler().run(plugin, scheduledTask -> {
                        if (target.isValid() && !target.isDead()) {
                            target.damage(dmg, player);
                            strike(target.getLocation());
                        }
                    }, null);

                    current = next;
                    currentLocation = next.getEyeLocation().clone();
                    damage *= CHAIN_DAMAGE_FALLOFF;
                    jumps++;
                    animating = false;
                });
            }

        }.runTaskTimer(plugin, 5L, 1L);
    }

    private LivingEntity getTarget(Player player) {
        Location eye = player.getEyeLocation();

        RayTraceResult result = player.getWorld().rayTrace(
                eye,
                eye.getDirection(),
                MAX_DISTANCE,
                FluidCollisionMode.NEVER,
                true,
                0.75,
                entity -> entity instanceof LivingEntity && entity != player
        );

        if (result == null) {
            return null;
        }

        if (result.getHitEntity() instanceof LivingEntity living) {
            return living;
        }

        return null;
    }

    /**
     * Finds the closest entity that hasn't already been hit.
     */
    private LivingEntity findNextTarget(
            Location currentLocation,
            Set<UUID> hit,
            Player caster
    ) {

        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        World world = currentLocation.getWorld();
        if (world == null) {
            return null;
        }

        for (Entity entity : world.getNearbyEntities(
                currentLocation,
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
                    .distanceSquared(currentLocation);

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