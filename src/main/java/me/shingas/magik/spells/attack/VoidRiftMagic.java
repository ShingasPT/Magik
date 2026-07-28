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
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.List;

public class VoidRiftMagic extends Magic {

    private static final int LIFETIME = 100;
    private static final double MAX_RADIUS = 2.0;
    private static final double PULL_RADIUS = 7.0;

    public VoidRiftMagic() {
        super(
                "voidrift",
                "Void Rift",
                Material.CRYING_OBSIDIAN,
                MagicCategory.ATTACK,
                List.of(
                        "<gray>Tear open a rift into",
                        "<gray>the void, consuming and",
                        "<gray>destroying nearby enemies."
                ),
                CastType.RIGHT_CLICK,
                30000L
        );
    }

    @Override
    public void cast(MagicContext context) {

        Player player = context.getPlayer();
        Magik plugin = context.getPlugin();
        RayTraceResult result = player.rayTraceBlocks(30);

        Location center;

        if (result != null) {

            center = result.getHitPosition()
                    .toLocation(player.getWorld());
            center.setY(center.getY() + 2);

        } else {

            center = player.getEyeLocation().add(
                    player.getLocation().getDirection().multiply(20)
            );

        }

        World world = center.getWorld();

        Vector normal = center.toVector()
                .subtract(player.getEyeLocation().toVector())
                .normalize();

        Vector right = normal.clone().crossProduct(new Vector(0, 1, 0));

        if (right.lengthSquared() < 0.01) {
            right = new Vector(1, 0, 0);
        }

        right.normalize();

        Vector up = right.clone().crossProduct(normal).normalize();

        Vector finalRight = right;
        new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {

                ticks++;

                if (ticks >= LIFETIME) {
                    cancel();
                    explode(center, player);
                    return;
                }

                double radius;

                if (ticks < 80) {

                    radius = MAX_RADIUS + Math.sin(ticks * 0.18) * 0.25;

                } else {

                    double progress = (ticks - 80) / 20.0;

                    // Smooth collapse
                    radius = MAX_RADIUS * Math.pow(1.0 - progress, 2);

                }

                pullEntities(center, player);

                if (ticks % 12 == 0) {

                    world.playSound(
                            center,
                            Sound.BLOCK_PORTAL_AMBIENT,
                            0.35f,
                            0.7f + (float)Math.random() * 0.3f
                    );

                }

                if (ticks % 2 == 0) {
                    drawRift(center, radius, ticks, finalRight, up);
                }

            }

        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void pullEntities(Location center, Player caster) {

        for (Entity entity : center.getWorld().getNearbyEntities(center, PULL_RADIUS, PULL_RADIUS, PULL_RADIUS)) {

            if (!(entity instanceof LivingEntity living))
                continue;

            if (living == caster)
                continue;

            Vector pull = center.toVector().subtract(living.getLocation().toVector());

            double distance = pull.length();

            if (distance < 0.75)
                continue;

            double strength = Math.min(
                    0.4,
                    0.08 + (PULL_RADIUS - distance) * 0.04
            );

            living.setVelocity(
                    living.getVelocity().multiply(0.85)
                            .add(
                                    pull.normalize().multiply(strength)
                            )
            );

        }

    }

    private void drawRift(
            Location center,
            double radius,
            int ticks,
            Vector right,
            Vector up
    ) {

        World world = center.getWorld();
        if (world == null) return;

        double height = radius * 1.8;
        double width = radius * 0.7;

        drawCenter(world, center, radius, ticks);

        drawDiamond(world, center, right, up,
                width,
                height,
                ticks,
                0);

        drawDiamond(world, center, right, up,
                width * 0.65,
                height * 0.65,
                -ticks,
                Math.PI);

        drawVoidSparks(
                world,
                center,
                right,
                up,
                radius,
                ticks
        );

        drawOrbit(world, center, right, up,
                radius * 0.9,
                ticks);
    }

    private void drawCenter(
            World world,
            Location center,
            double radius,
            int ticks
    ) {

        int extra = ticks > 80 ? 2 : 0;

        world.spawnParticle(
                Particle.REVERSE_PORTAL,
                center,
                2 + extra,
                radius * 0.15,
                radius * 0.15,
                radius * 0.15,
                0.02
        );

        world.spawnParticle(
                Particle.DUST,
                center,
                2,
                0,
                0,
                0,
                0,
                new Particle.DustOptions(
                        Color.fromRGB(180, 0, 255),
                        2f
                )
        );
    }

    private void drawDiamond(
            World world,
            Location center,
            Vector right,
            Vector up,
            double width,
            double height,
            int ticks,
            double phase
    ) {

        double pulse = 1 + Math.sin(ticks * 0.18 + phase) * 0.08;

        width *= pulse;
        height *= pulse;

        double rotation = Math.toRadians(ticks * 4);

        Vector top = rotate(
                up.clone().multiply(height),
                right,
                up,
                rotation
        );

        Vector bottom = rotate(
                up.clone().multiply(-height),
                right,
                up,
                rotation
        );

        Vector left = rotate(
                right.clone().multiply(-width),
                right,
                up,
                rotation
        );

        Vector rightPoint = rotate(
                right.clone().multiply(width),
                right,
                up,
                rotation
        );

        drawEdge(world,
                center.clone().add(top),
                center.clone().add(rightPoint));

        drawEdge(world,
                center.clone().add(rightPoint),
                center.clone().add(bottom));

        drawEdge(world,
                center.clone().add(bottom),
                center.clone().add(left));

        drawEdge(world,
                center.clone().add(left),
                center.clone().add(top));
    }

    private void drawEdge(
            World world,
            Location from,
            Location to
    ) {

        Vector line = to.toVector().subtract(from.toVector());

        double length = line.length();

        Vector direction = line.normalize();

        for (double d = 0; d <= length; d += 0.30) {

            Location point = from.clone()
                    .add(direction.clone().multiply(d));

            world.spawnParticle(
                    Particle.DUST,
                    point,
                    1,
                    0,
                    0,
                    0,
                    0,
                    new Particle.DustOptions(
                            Color.fromRGB(170, 50, 255),
                            1.5f
                    )
            );

            if (Math.random() < 0.15) {

                world.spawnParticle(
                        Particle.PORTAL,
                        point,
                        1,
                        0,
                        0,
                        0,
                        0
                );
            }
        }
    }

    private Vector rotate(
            Vector point,
            Vector right,
            Vector up,
            double angle
    ) {

        double x = point.dot(right);
        double y = point.dot(up);

        double rx = x * Math.cos(angle) - y * Math.sin(angle);
        double ry = x * Math.sin(angle) + y * Math.cos(angle);

        return right.clone()
                .multiply(rx)
                .add(
                        up.clone().multiply(ry)
                );
    }

    private void drawOrbit(
            World world,
            Location center,
            Vector right,
            Vector up,
            double radius,
            int ticks
    ) {

        double rotation1 = Math.toRadians(ticks * 6);
        double rotation2 = Math.toRadians(-ticks * 8);
        int points = ticks > 80 ? 14 : 10;

        for (int i = 0; i < points; i++) {

            double angle1 = rotation1 + (Math.PI * 2 * i / 16);
            double angle2 = rotation2 + (Math.PI * 2 * i / 16);

            Vector offset1 =
                    right.clone().multiply(Math.cos(angle1) * radius)
                            .add(
                                    up.clone().multiply(Math.sin(angle1) * radius)
                            );

            Vector offset2 =
                    right.clone().multiply(Math.cos(angle2) * radius * 0.65)
                            .add(
                                    up.clone().multiply(Math.sin(angle2) * radius * 0.65)
                            );

            world.spawnParticle(
                    Particle.REVERSE_PORTAL,
                    center.clone().add(offset1),
                    1,
                    0,
                    0,
                    0,
                    0
            );

        }
    }

    private void drawVoidSparks(
            World world,
            Location center,
            Vector right,
            Vector up,
            double radius,
            int ticks
    ) {

        for (int i = 0; i < 3; i++) {

            double angle = Math.random() * Math.PI * 2;

            double distance = radius * (0.8 + Math.random());

            Vector offset =
                    right.clone().multiply(Math.cos(angle) * distance)
                            .add(
                                    up.clone().multiply(Math.sin(angle) * distance)
                            );

            Location point = center.clone().add(offset);

            world.spawnParticle(
                    Particle.SOUL_FIRE_FLAME,
                    point,
                    1,
                    0,
                    0,
                    0,
                    0
            );
        }
    }

    private void explode(Location center, Player caster) {

        World world = center.getWorld();
        if (world == null) return;

        world.playSound(
                center,
                Sound.ENTITY_GENERIC_EXPLODE,
                1.0f,
                0.8f
        );

        world.playSound(
                center,
                Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE,
                1.0f,
                0.5f
        );

        world.spawnParticle(
                Particle.FIREWORK,
                center,
                20,
                0.2,
                0.2,
                0.2,
                0.0
        );

        world.spawnParticle(
                Particle.EXPLOSION,
                center,
                1
        );

        world.spawnParticle(
                Particle.REVERSE_PORTAL,
                center,
                150,
                0.8,
                0.8,
                0.8,
                0.2
        );

        world.spawnParticle(
                Particle.DUST,
                center,
                120,
                0.6,
                0.6,
                0.6,
                0,
                new Particle.DustOptions(
                        Color.fromRGB(170, 50, 255),
                        2.4f
                )
        );

        for (Entity entity : world.getNearbyEntities(center, 7, 7, 7)) {

            if (!(entity instanceof LivingEntity living))
                continue;

            if (living == caster)
                continue;

            Location entityLoc = living.getLocation().clone();
            entityLoc.setY(center.getY());

            double distance = entityLoc.distance(center);

            if (distance > 7)
                continue;

            double damage = 16 - distance * 2;

            living.damage(Math.max(6, damage), caster);
        }

    }

}
