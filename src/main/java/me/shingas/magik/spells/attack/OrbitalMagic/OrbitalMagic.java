package me.shingas.magik.spells.attack.OrbitalMagic;

import me.shingas.magik.Magik;
import me.shingas.magik.magic.CastType;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrbitalMagic extends Magic {

    private static final Particle.DustOptions TARGET_LASER =
            new Particle.DustOptions(Color.fromRGB(255, 40, 40), 1.2F);

    // Timeline (20 ticks = 1 second)
    private static final int LASER_DURATION = 100;
    private static final int CHARGING_DURATION = 100;
    private static final int DESCENT_DURATION = 10;
    private static final int BEAM_DURATION = 100;
    private static final int COLLAPSE_DURATION = 10;
    private static final int SILENCE_DURATION = 5;

    private static final int TOTAL_DURATION =
            LASER_DURATION +
                    CHARGING_DURATION +
                    DESCENT_DURATION +
                    BEAM_DURATION +
                    COLLAPSE_DURATION +
                    SILENCE_DURATION + 1;

    private final Map<UUID, Integer> drillDepth = new HashMap<>();

    public OrbitalMagic() {
        super(
                "orbital",
                "Orbital Strike",
                Material.BEACON,
                MagicCategory.ATTACK,
                List.of(
                        "<gray>Call down an immense",
                        "<gray>orbital blast from above",
                        "<gray>to obliterate the area."
                ),
                CastType.RIGHT_CLICK,
                60000L
        );
    }

    @Override
    public void cast(MagicContext context) {

        Player player = context.getPlayer();
        Magik plugin = context.getPlugin();
        World world = player.getWorld();

        Block targetBlock = player.getTargetBlockExact(50);
        if (targetBlock == null) return;
        Location target = targetBlock.getLocation().add(0.5, 0.5, 0.5); // center of the block

        new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {

                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                drillDepth.put(player.getUniqueId(), 0);

                OrbitalPhase phase = getPhase(ticks);
                int phaseTicks = getPhaseTicks(ticks);

                switch (phase) {

                    case LASER -> {
                        drawTargetLaser(world, target, phaseTicks);
                    }

                    case CHARGING -> {
                        drawChargingSpirals(world, target, phaseTicks);
                    }

                    case BEAM_DESCENT -> {
                        drawOrbitalBeam(world, target, phaseTicks);
                        drawGroundImpact(world, target, phaseTicks);
                    }

                    case BEAM -> {
                        drawOrbitalBeam(world, target, phaseTicks);
                        drawGroundImpact(world, target, phaseTicks);
                        destroyTerrain(world, target, player);
                        launchDebris(world, target,
                                drillDepth.get(player.getUniqueId()), plugin);
                        pullEntities(world, target, player);
                        igniteGround(world, target);
                        shakePlayers(world, target);
                        vaporizeEntities(world, target, player, phaseTicks);

                        // Random lightning around the crater
                        if (phaseTicks % 8 == 0) {

                            Location strike = target.clone().add(
                                    (Math.random() - 0.5) * 10,
                                    0,
                                    (Math.random() - 0.5) * 10
                            );

                            world.strikeLightningEffect(strike);

                        }

                    }

                    case COLLAPSE -> {

                        beamCollapse(world, target, phaseTicks);

                        // Final crater widening
                        if (phaseTicks == 0) {
                            destroyFinalCrater(world, target);
                        }
                    }

                    case SILENCE -> {
                        // Intentionally empty
                    }

                    case DETONATION -> {
                        // TODO
                        beamDetonation(world, target, player);
                    }

                    case FINISHED -> {
                        drillDepth.remove(player.getUniqueId());
                        cancel();
                        return;
                    }

                }

                ticks++;

            }

        }.runTaskTimer(plugin, 0L, 1L);

    }

    private void drawChargingSpirals(World world,
                                     Location center,
                                     int phaseTicks) {


        double progress =
                Math.min(phaseTicks / (double) CHARGING_DURATION, 1);


        double maxRadius =
                14 * progress;


        double rotation =
                phaseTicks * 0.15;


        Particle.DustOptions red =
                new Particle.DustOptions(
                        Color.fromRGB(255,40,40),
                        1.3F
                );


        Particle.DustOptions white =
                new Particle.DustOptions(
                        Color.WHITE,
                        1F
                );


        // Two opposite spirals
        drawSpiral(
                world,
                center,
                maxRadius,
                rotation,
                1,
                red
        );


        drawSpiral(
                world,
                center,
                maxRadius,
                -rotation,
                -1,
                white
        );


        // Ground charging circle
        world.spawnParticle(
                Particle.ELECTRIC_SPARK,
                center,
                20,
                maxRadius,
                0.1,
                maxRadius,
                0.02
        );
    }

    private void drawSpiral(World world,
                            Location center,
                            double radius,
                            double rotation,
                            int direction,
                            Particle.DustOptions color) {


        int points = 80;


        for (int i = 0; i < points; i++) {


            double progress =
                    i / (double) points;


            double r =
                    progress * radius;


            double angle =
                    (progress * Math.PI * 6)
                            + rotation * direction;


            double x =
                    Math.cos(angle) * r;


            double z =
                    Math.sin(angle) * r;


            Location particle =
                    center.clone()
                            .add(x, 0.15, z);


            world.spawnParticle(
                    Particle.DUST,
                    particle,
                    1,
                    0,
                    0,
                    0,
                    0,
                    color
            );


            // Add sparks near the end
            if (Math.random() < 0.08) {

                world.spawnParticle(
                        Particle.ELECTRIC_SPARK,
                        particle,
                        1,
                        0.1,
                        0.05,
                        0.1,
                        0
                );

            }
        }
    }

    private void drawTargetLaser(World world,
                                 Location target,
                                 int phaseTicks) {

        double height = 320;

        Location start = target.clone().add(0, height, 0);

        double pulse =
                0.35 + Math.sin(phaseTicks * 0.25) * 0.15;


        Particle.DustOptions red =
                new Particle.DustOptions(
                        Color.fromRGB(255, 0, 0),
                        1.5F
                );


        // Main laser line
        for (double y = start.getY();
             y >= target.getY();
             y -= 1.5) {


            Location point = new Location(
                    world,
                    target.getX(),
                    y,
                    target.getZ()
            );


            world.spawnParticle(
                    Particle.DUST,
                    point,
                    1,
                    0,
                    0,
                    0,
                    0,
                    red
            );


            // Energy flicker
            if (Math.random() < 0.25) {

                world.spawnParticle(
                        Particle.ELECTRIC_SPARK,
                        point,
                        1,
                        pulse,
                        pulse,
                        pulse,
                        0
                );

            }
        }


        // Impact point
        world.spawnParticle(
                Particle.DUST,
                target,
                10,
                1,
                0.1,
                1,
                0,
                red
        );


        world.spawnParticle(
                Particle.END_ROD,
                target,
                5,
                0.4,
                0.1,
                0.4,
                0.02
        );
    }

    private void beamDetonation(World world, Location target, Player player) {
        world.spawnParticle(
                Particle.EXPLOSION_EMITTER,
                target,
                1
        );

        world.playSound(
                target,
                Sound.ENTITY_GENERIC_EXPLODE,
                5F,
                0.3F
        );

        for (Entity entity : world.getNearbyEntities(target, 20, 20, 20)) {

            if (entity instanceof LivingEntity living
                    && !living.equals(player)) {

                living.damage(500, player);
            }
        }
    }

    private double getCurrentBeamRadius(int phaseTicks) {

        // First 60 ticks expand, then stay max size
        double progress = Math.min(phaseTicks / 60.0, 1.0);

        // Smooth acceleration
        progress = progress * progress;

        // Start narrow, grow into orbital cannon size
        return 2.0 + (progress * 6.0);
    }

    private void destroyFinalCrater(World world, Location center) {

        int radius = 12;

        for (int x = -radius; x <= radius; x++) {

            for (int z = -radius; z <= radius; z++) {

                if (x * x + z * z > radius * radius)
                    continue;

                int depth = (int)
                        (Math.random() * 6 + 3);

                Block surface = getGroundBlock(
                        world,
                        center.getBlockX() + x,
                        center.getBlockZ() + z
                );

                for (int y = 0; y < depth; y++) {

                    Block block = surface.getRelative(0, -y, 0);

                    if (block.getType() == Material.BEDROCK)
                        break;

                    block.setType(Material.AIR);
                }

                if (Math.random() < 0.2) {

                    Block lava =
                            surface.getRelative(0, -depth, 0);

                    lava.setType(Material.LAVA);
                }
            }
        }
    }

    private Block getGroundBlock(World world, int x, int z) {

        int y = world.getHighestBlockYAt(x, z);

        Block block = world.getBlockAt(x, y, z);

        while (!block.getType().isSolid() && y > world.getMinHeight()) {
            y--;
            block = world.getBlockAt(x, y, z);
        }

        return block;
    }

    private void vaporizeEntities(World world,
                                  Location center,
                                  Player caster,
                                  int phaseTicks) {

        double beamRadius = getCurrentBeamRadius(phaseTicks);

        for (Entity entity : world.getNearbyEntities(center, beamRadius, 256, beamRadius)) {

            if (!(entity instanceof LivingEntity living))
                continue;

            // Don't kill the caster
            if (living.equals(caster))
                continue;

            if (living.isDead() || living.getHealth() <= 0) continue;

            Location loc = living.getLocation();

            world.spawnParticle(
                    Particle.FLASH,
                    loc,
                    1,
                    Color.WHITE
            );

            world.spawnParticle(
                    Particle.LAVA,
                    loc,
                    30,
                    0.4,
                    0.8,
                    0.4,
                    0
            );

            world.spawnParticle(
                    Particle.SMOKE,
                    loc,
                    25,
                    0.3,
                    0.8,
                    0.3,
                    0.02
            );

            world.playSound(
                    loc,
                    Sound.ENTITY_GENERIC_BURN,
                    1.5F,
                    0.6F
            );

            // Absolutely annihilate them
            living.damage(1000.0, caster);

        }

    }

    private void beamCollapse(World world, Location target, int phaseTicks) {
        double progress = phaseTicks / (double) COLLAPSE_DURATION;

        double radius = 12 - (progress * 12);

        drawShockwave(world, target, radius);

        world.spawnParticle(
                Particle.EXPLOSION,
                target,
                2
        );

        world.spawnParticle(
                Particle.LAVA,
                target,
                50,
                2,
                0.5,
                2,
                0.1
        );

        world.playSound(
                target,
                Sound.ENTITY_GENERIC_EXPLODE,
                4F,
                0.5F
        );
    }

    private void pullEntities(World world, Location center, Player caster) {

        for (Entity entity : world.getNearbyEntities(center, 15, 15, 15)) {

            if (entity.equals(caster))
                continue;

            if (!(entity instanceof LivingEntity))
                continue;

            Vector direction = center.toVector()
                    .subtract(entity.getLocation().toVector());

            double distance = direction.length();

            if (distance < 1)
                continue;

            direction.normalize();

            double strength = Math.min(0.8, 2.5 / distance);

            entity.setVelocity(
                    entity.getVelocity().add(
                            direction.multiply(strength)
                    )
            );

        }

    }

    private void igniteGround(World world, Location center) {

        for (int i = 0; i < 3; i++) {

            double angle = Math.random() * Math.PI * 2;

            double radius = 5 + Math.random() * 3;

            int x = center.getBlockX() + (int) Math.round(Math.cos(angle) * radius);
            int z = center.getBlockZ() + (int) Math.round(Math.sin(angle) * radius);

            Block block = world.getHighestBlockAt(x, z);

            Block above = block.getRelative(BlockFace.UP);

            if (above.getType() == Material.AIR) {
                above.setType(Material.FIRE);
            }

        }

    }

    private void shakePlayers(World world, Location center) {

        for (Player player : world.getPlayers()) {

            double distance = player.getLocation().distance(center);

            if (distance > 30)
                continue;

            player.setVelocity(

                    player.getVelocity().add(

                            new Vector(

                                    (Math.random() - 0.5) * 0.08,

                                    0.03,

                                    (Math.random() - 0.5) * 0.08

                            )

                    )

            );

        }

    }

    private void drawOrbitalBeam(World world, Location target, int phaseTicks) {

        double topY = target.getY() + 320;

        double progress = Math.min(phaseTicks / 60.0, 1.0);
        progress = progress * progress;

        double radius = 0.25 + (progress * 3.0);

        Particle.DustOptions red =
                new Particle.DustOptions(Color.fromRGB(255, 40, 40), 2.0F);

        Particle.DustOptions white =
                new Particle.DustOptions(Color.WHITE, 1.2F);

        for (double y = topY; y >= target.getY(); y -= 0.75) {

            double pulse = Math.sin((phaseTicks * 0.35) + (y * 0.05)) * 0.15;

            drawBeamRing(
                    world,
                    target.getX(),
                    y,
                    target.getZ(),
                    radius + pulse,
                    red,
                    white
            );
        }

    }

    private void drawBeamRing(World world,
                              double x,
                              double y,
                              double z,
                              double radius,
                              Particle.DustOptions shell,
                              Particle.DustOptions core) {

        Location center = new Location(world, x, y, z);

        // Outer plasma shell
        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 12) {

            double px = Math.cos(angle) * radius;
            double pz = Math.sin(angle) * radius;

            world.spawnParticle(
                    Particle.DUST,
                    center.clone().add(px, 0, pz),
                    1,
                    0,
                    0,
                    0,
                    0,
                    shell
            );
        }

        // White-hot centre
        world.spawnParticle(
                Particle.DUST,
                center,
                2,
                0.05,
                0,
                0.05,
                0,
                core
        );

        // Plasma sparks
        if (Math.random() < 0.35) {

            world.spawnParticle(
                    Particle.ELECTRIC_SPARK,
                    center,
                    2,
                    radius * 0.5,
                    0,
                    radius * 0.5,
                    0
            );

        }

        // Heat shimmer
        if (Math.random() < 0.15) {

            world.spawnParticle(
                    Particle.WHITE_SMOKE,
                    center,
                    1,
                    radius,
                    0,
                    radius,
                    0.01
            );

        }
    }

    private void drawGroundImpact(World world, Location target, int phaseTicks) {

        double progress = Math.min(phaseTicks / 30.0, 1.0);

        progress = progress * progress;

        double radius = progress * 12;

        drawShockwave(world, target, radius);

        drawDustRing(world, target, radius);

        drawDebris(world, target, radius);

        drawMoltenCore(world, target);

    }

    private void drawShockwave(World world,
                               Location center,
                               double radius) {

        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 36) {

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            world.spawnParticle(
                    Particle.CLOUD,
                    center.clone().add(x, 0.15, z),
                    2,
                    0.05,
                    0.02,
                    0.05,
                    0
            );
        }

    }

    private void drawDustRing(World world,
                              Location center,
                              double radius) {

        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 40) {

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            world.spawnParticle(
                    Particle.WHITE_SMOKE,
                    center.clone().add(x, 0.2, z),
                    1,
                    0.1,
                    0.1,
                    0.1,
                    0.01
            );
        }

    }

    private void drawDebris(World world,
                            Location center,
                            double radius) {

        for (int i = 0; i < 20; i++) {

            double angle = Math.random() * Math.PI * 2;
            double r = Math.random() * radius;

            double x = Math.cos(angle) * r;
            double z = Math.sin(angle) * r;

            world.spawnParticle(
                    Particle.BLOCK,
                    center.clone().add(x, 0.3, z),
                    3,
                    0.15,
                    0.25,
                    0.15,
                    Material.STONE.createBlockData()
            );

        }

    }

    private void launchDebris(World world,
                              Location center,
                              int craterRadius,
                              Magik plugin) {

        for (int i = 0; i < 4; i++) {

            double angle = Math.random() * Math.PI * 2;

            double radius = craterRadius - 1 + Math.random() * 2;

            int x = center.getBlockX() + (int) Math.round(Math.cos(angle) * radius);
            int z = center.getBlockZ() + (int) Math.round(Math.sin(angle) * radius);

            Block block = world.getHighestBlockAt(x, z);

            if (block.isEmpty())
                continue;

            if (block.getType() == Material.BEDROCK)
                continue;

            FallingBlock falling = world.spawn(
                    block.getLocation().add(0.5, 0.5, 0.5),
                    FallingBlock.class
            );
            falling.setBlockData(block.getBlockData());

            block.setType(Material.AIR);

            falling.setDropItem(false);
            falling.setHurtEntities(false);
            falling.setCancelDrop(true);

            falling.setVelocity(new Vector(
                    (Math.random() - 0.5) * 1.4,
                    1.2 + Math.random() * 0.8,
                    (Math.random() - 0.5) * 1.4
            ));

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!falling.isValid()) {
                        cancel();
                        return;
                    }
                    if (falling.isOnGround()) {
                        falling.remove();
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 1L, 1L);

        }

    }

    private void drawMoltenCore(World world,
                                Location center) {

        world.spawnParticle(
                Particle.LAVA,
                center,
                6,
                0.6,
                0.1,
                0.6,
                0
        );

        world.spawnParticle(
                Particle.FLAME,
                center,
                8,
                0.8,
                0.2,
                0.8,
                0.02
        );

        world.spawnParticle(
                Particle.ASH,
                center,
                8,
                1,
                0.2,
                1,
                0
        );

    }

    private void destroyTerrain(World world,
                                Location center,
                                Player caster) {

        int depth = drillDepth.getOrDefault(
                caster.getUniqueId(),
                0
        );

        double radius = Math.min(2 + depth * 0.18, 8);

        for (double x = -radius; x <= radius; x++) {

            for (double z = -radius; z <= radius; z++) {

                if (x * x + z * z > radius * radius)
                    continue;

                int bx = center.getBlockX() + (int) Math.round(x);
                int bz = center.getBlockZ() + (int) Math.round(z);

                Block block = world.getHighestBlockAt(bx, bz);

                for (int y = 0; y <= depth; y++) {

                    Block current = block.getRelative(0, -y, 0);

                    if (current.getType() == Material.BEDROCK)
                        break;

                    if (current.isEmpty())
                        continue;

                    current.setType(Material.AIR);

                }

            }

        }

        drillDepth.put(
                caster.getUniqueId(),
                Math.min(depth + 1, 30)
        );

        Block lava = world.getBlockAt(
                center.getBlockX(),
                world.getHighestBlockYAt(center),
                center.getBlockZ()
        );

        if (lava.getType() != Material.BEDROCK) {
            lava.setType(Material.LAVA);
        }

    }

    private int getPhaseTicks(int ticks) {

        OrbitalPhase phase = getPhase(ticks);

        return switch (phase) {
            case LASER -> ticks;
            case CHARGING -> ticks - LASER_DURATION;
            case BEAM_DESCENT -> ticks - LASER_DURATION - CHARGING_DURATION;
            case BEAM -> ticks
                    - LASER_DURATION
                    - CHARGING_DURATION
                    - DESCENT_DURATION;
            case COLLAPSE -> ticks
                    - LASER_DURATION
                    - CHARGING_DURATION
                    - DESCENT_DURATION
                    - BEAM_DURATION;
            case SILENCE -> ticks
                    - LASER_DURATION
                    - CHARGING_DURATION
                    - DESCENT_DURATION
                    - BEAM_DURATION
                    - COLLAPSE_DURATION;
            default -> 0;
        };
    }

    private OrbitalPhase getPhase(int ticks) {

        if (ticks < LASER_DURATION)
            return OrbitalPhase.LASER;

        ticks -= LASER_DURATION;

        if (ticks < CHARGING_DURATION)
            return OrbitalPhase.CHARGING;

        ticks -= CHARGING_DURATION;

        if (ticks < DESCENT_DURATION)
            return OrbitalPhase.BEAM_DESCENT;

        ticks -= DESCENT_DURATION;

        if (ticks < BEAM_DURATION)
            return OrbitalPhase.BEAM;

        ticks -= BEAM_DURATION;

        if (ticks < COLLAPSE_DURATION)
            return OrbitalPhase.COLLAPSE;

        ticks -= COLLAPSE_DURATION;

        if (ticks < SILENCE_DURATION)
            return OrbitalPhase.SILENCE;

        ticks -= SILENCE_DURATION;

        if (ticks == 0)
            return OrbitalPhase.DETONATION;

        return OrbitalPhase.FINISHED;
    }

}