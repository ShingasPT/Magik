package me.shingas.magik.spells.attack;

import me.shingas.magik.Magik;
import me.shingas.magik.magic.CastType;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class BlackHoleMagic extends Magic {

    public BlackHoleMagic() {
        super(
                "blackhole",
                "Black Hole",
                Material.OBSIDIAN,
                MagicCategory.ATTACK,
                List.of(
                        "<gray>Summon a powerful black hole.",
                        "<gray>Pulls nearby enemies inward.",
                        "<gray>Consumes blocks and deals damage."
                ),
                CastType.RIGHT_CLICK,
                30000L
        );
    }

    private static class OrbitingBlock {
        BlockDisplay display;
        double angle; // current angle around the black hole
        double radius; // distance from center
        double heightOffset; // vertical offset
        Location center;

        OrbitingBlock(BlockDisplay display, double radius, double heightOffset, Location center) {
            this.display = display;
            this.radius = radius;
            this.heightOffset = heightOffset;
            this.center = center.clone();
            this.angle = Math.random() * 2 * Math.PI;
        }

        void tick() {
            angle += 0.1; // rotation speed
            radius = Math.max(0, radius - 0.05); // spiral inward
            Location loc = center.clone();
            loc.add(Math.cos(angle) * radius, heightOffset, Math.sin(angle) * radius);
            if (!display.isValid()) {
                return;
            }

            display.teleport(loc);

            if (radius < 0.1) {
                display.remove();
            }
        }

        boolean isDead() {
            return !display.isValid();
        }
    }

    @Override
    public void cast(MagicContext context) {

        Player player = context.getPlayer();
        Magik plugin = context.getPlugin();
        World world = player.getWorld();

        Block targetBlock = player.getTargetBlockExact(50);
        if (targetBlock == null) return;
        Location center = targetBlock.getLocation().add(0.5, 0.5, 0.5);

        int durationTicks = 10 * 20;
        final double[] radius = {2.0};
        double growthRate = 0.025;
        final int[] ticks = {0};

        List<OrbitingBlock> orbitingBlocks = new ArrayList<>();

        Bukkit.getRegionScheduler().runAtFixedRate(plugin, center, task -> {
            if (ticks[0] >= durationTicks) {
                orbitingBlocks.forEach(b -> {
                    if (b.display.isValid()) {
                        b.display.remove();
                    }
                });
                orbitingBlocks.clear();
                task.cancel();
                return;
            }

                // Spawn particles on outer shell only
                double particleRadius = radius[0] + 0.5; // slightly outside orbiting blocks
                for (double theta = 0; theta < Math.PI; theta += Math.PI / 20) {
                    for (double phi = 0; phi < 2 * Math.PI; phi += Math.PI / 20) {
                        double x = particleRadius * Math.sin(theta) * Math.cos(phi);
                        double y = particleRadius * Math.cos(theta);
                        double z = particleRadius * Math.sin(theta) * Math.sin(phi);
                        Location loc = center.clone().add(x, y, z);
                        world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.BLACK, 1f));
                    }
                }

                // Destroy blocks and create BlockDisplays (max 25)
                for (int x = (int) -radius[0]; x <= radius[0]; x++) {
                    for (int y = (int) -radius[0]; y <= radius[0]; y++) {
                        for (int z = (int) -radius[0]; z <= radius[0]; z++) {
                            if (Math.sqrt(x*x + y*y + z*z) <= radius[0]) {
                                Block block = world.getBlockAt(center.clone().add(x, y, z));
                                if (!block.getType().isAir() && block.getType().isSolid() && block.getType() != Material.BEDROCK) {
                                    Material mat = block.getType();
                                    block.setType(Material.AIR);

                                    if (orbitingBlocks.size() < 30) { // increased limit
                                        BlockDisplay display = world.spawn(block.getLocation().add(0.5, 0.5, 0.5), BlockDisplay.class);
                                        display.setBlock(mat.createBlockData());

                                        OrbitingBlock ob = new OrbitingBlock(display, radius[0], y, center);
                                        orbitingBlocks.add(ob);
                                    }
                                }
                            }
                        }
                    }
                }

                // Pull entities toward center and damage
                for (LivingEntity entity : world.getNearbyEntities(center, radius[0] +1, radius[0] +1, radius[0] +1).stream()
                        .filter(e -> e instanceof LivingEntity)
                        .map(e -> (LivingEntity) e).toList()) {
                    if (!entity.equals(player) && !entity.isDead() && entity.getHealth() > 0) {
                        Vector pull = center.toVector().subtract(entity.getLocation().toVector()).multiply(0.1);
                        entity.setVelocity(entity.getVelocity().add(pull));
                        entity.damage(2, player);
                    }
                }

                // Tick orbiting blocks
                orbitingBlocks.removeIf(ob -> {
                    ob.tick();
                    return ob.isDead();
                });

                radius[0] += growthRate;
                ticks[0]++;
        }, 0L, 1L);

    }
}