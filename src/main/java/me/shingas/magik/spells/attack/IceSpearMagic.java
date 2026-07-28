package me.shingas.magik.spells.attack;

import me.shingas.magik.Magik;
import me.shingas.magik.magic.CastType;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IceSpearMagic extends Magic {

    private static final double SPEED = 1.2;
    private static final double DAMAGE = 8;
    private static final int MAX_DISTANCE = 50;

    public IceSpearMagic() {
        super(
                "icespear",
                "Ice Spear",
                Material.PACKED_ICE,
                MagicCategory.ATTACK,
                List.of(
                        "<gray>Summon a piercing spear",
                        "<gray>of enchanted ice that",
                        "<gray>impales your target."
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

        Location spear = player.getEyeLocation().add(
                player.getLocation().getDirection().normalize().multiply(1.2)
        );

        Vector direction = player.getEyeLocation().getDirection().normalize();

        ItemDisplay display = world.spawn(spear, ItemDisplay.class);

        display.setItemStack(new ItemStack(Material.PACKED_ICE));

        display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);

        Transformation transform = new Transformation(
                new Vector3f(),
                new AxisAngle4f((float) Math.toRadians(90), 1, 0, 0),
                new Vector3f(0.7f, 1.8f, 0.7f),
                new AxisAngle4f()
        );

        display.setTransformation(transform);

        display.setInterpolationDuration(1);
        display.setInterpolationDelay(0);

        Set<UUID> hit = new HashSet<>();

        world.playSound(
                player.getLocation(),
                Sound.BLOCK_GLASS_BREAK,
                1f,
                0.6f
        );

        new BukkitRunnable() {

            double travelled = 0;

            @Override
            public void run() {

                if (travelled >= MAX_DISTANCE) {
                    shatter(spear);
                    display.remove();
                    cancel();
                    return;
                }

                spear.add(direction.clone().multiply(SPEED));
                travelled += SPEED;

                Location displayLoc = spear.clone().add(direction.clone().multiply(0.5));

                displayLoc.setDirection(direction);

                display.teleport(displayLoc);

                if (!spear.getBlock().isPassable()) {
                    shatter(spear);
                    display.remove();
                    cancel();
                    return;
                }

                spawnTrail(spear);

                for (Entity entity : world.getNearbyEntities(spear, 1, 1, 1)) {

                    if (!(entity instanceof LivingEntity living))
                        continue;

                    if (living == player)
                        continue;

                    if (!hit.add(living.getUniqueId()))
                        continue;

                    living.damage(DAMAGE, player);

                    living.addPotionEffect(
                            new PotionEffect(
                                    PotionEffectType.SLOWNESS,
                                    60,
                                    1
                            )
                    );

                    world.playSound(
                            living.getLocation(),
                            Sound.BLOCK_GLASS_HIT,
                            1f,
                            1.5f
                    );

                    world.spawnParticle(
                            Particle.BLOCK_CRUMBLE,
                            living.getLocation().add(0,1,0),
                            25,
                            .3,.4,.3,
                            Material.PACKED_ICE.createBlockData()
                    );
                }

            }

        }.runTaskTimer(plugin,0,1);
    }

    private void spawnTrail(Location loc) {

        World world = loc.getWorld();

        world.spawnParticle(
                Particle.SNOWFLAKE,
                loc,
                6,
                .08,.08,.08,
                .01
        );

        world.spawnParticle(
                Particle.END_ROD,
                loc,
                1,
                0,
                0,
                0,
                0
        );

        world.spawnParticle(
                Particle.BLOCK_CRUMBLE,
                loc,
                3,
                .05,.05,.05,
                Material.PACKED_ICE.createBlockData()
        );

        world.spawnParticle(
                Particle.DUST,
                loc,
                2,
                0,
                0,
                0,
                0,
                new Particle.DustOptions(Color.WHITE,1.3f)
        );

    }

    private void shatter(Location loc) {

        World world = loc.getWorld();

        world.playSound(
                loc,
                Sound.BLOCK_GLASS_BREAK,
                1f,
                0.8f
        );

        world.spawnParticle(
                Particle.BLOCK_CRUMBLE,
                loc,
                80,
                .6,.6,.6,
                Material.PACKED_ICE.createBlockData()
        );

        world.spawnParticle(
                Particle.SNOWFLAKE,
                loc,
                50,
                .5,.5,.5,
                .03
        );

        world.spawnParticle(
                Particle.END_ROD,
                loc,
                25,
                .4,.4,.4,
                .02
        );

    }

}
