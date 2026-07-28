package me.shingas.magik.spells.attack;

import me.shingas.magik.Magik;
import me.shingas.magik.magic.CastType;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class MeteorMagic extends Magic {

    public MeteorMagic() {
        super(
                "meteor",
                "Meteor",
                Material.FIRE_CHARGE,
                MagicCategory.ATTACK,
                List.of(
                        "<gray>Call a meteor from the sky.",
                        "<gray>Creates a massive explosion",
                        "<gray>on impact."
                ),
                CastType.RIGHT_CLICK,
                10000L
        );
    }

    @Override
    public void cast(MagicContext context) {

        Player player = context.getPlayer();
        Magik plugin = context.getPlugin();
        World world = player.getWorld();

        // Get the block the player is looking at (up to 100 blocks away)
        Block targetBlock = player.getTargetBlockExact(50);
        if (targetBlock == null) return;

        Location target = targetBlock.getLocation().add(0.5, 0.5, 0.5); // center of the block

        // Spawn position: 50 blocks above the target
        Location start = target.clone().add(0, 50, 0);

        // Spawn fireball
        Fireball meteor = world.spawn(start, Fireball.class);
        meteor.setYield(6); // explosion power
        meteor.setIsIncendiary(true);
        meteor.setShooter(player);

        // Calculate direction
        Vector velocity = target.toVector().subtract(start.toVector()).normalize().multiply(1.5);
        meteor.setVelocity(velocity);

        // Particle trail to make it look like a meteor
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (meteor.isDead() || ticks > 200) {
                    this.cancel();
                    return;
                }

                world.spawnParticle(Particle.FLAME, meteor.getLocation(), 5, 0.5, 0.5, 0.5, 0);
                world.spawnParticle(Particle.LAVA, meteor.getLocation(), 3, 0.5, 0.5, 0.5, 0);

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Explosion on impact
        new BukkitRunnable() {
            @Override
            public void run() {
                if (meteor.isDead()) {
                    world.createExplosion(meteor.getLocation(), 10F, true, true, null);
                    world.spawnParticle(Particle.EXPLOSION, meteor.getLocation(), 1);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}