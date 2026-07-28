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
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeamMagic extends Magic {

    public BeamMagic() {
        super(
                "beam",
                "Beam",
                Material.END_ROD,
                MagicCategory.ATTACK,
                List.of(
                        "<gray>Fire a concentrated beam",
                        "<gray>of magical energy that",
                        "<gray>pierces through enemies."
                ),
                CastType.RIGHT_CLICK,
                10000L
        );
    }

    @Override
    public void cast(MagicContext context) {
        Player player = context.getPlayer();
        Location loc = player.getEyeLocation();
        Vector dir = loc.getDirection().normalize();

        World world = player.getWorld();

        int maxDistance = 50;
        for (int i = 0; i < maxDistance; i++) {

            loc = loc.add(dir);

            // Sonic boom particle
            world.spawnParticle(Particle.SONIC_BOOM, loc, 1);
            if (!loc.getBlock().getType().isAir()) break;

            // Kill entities near beam
            Set<Entity> alreadyHit = new HashSet<>();
            Collection<Entity> entities = loc.getNearbyEntities(1, 1, 1);
            for (Entity ent : entities) {
                if (ent instanceof LivingEntity le && ent != player && !alreadyHit.contains(ent)) {
                    le.setHealth(0);
                    alreadyHit.add(ent);
                }
            }
        }

        // Sound
        world.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 1f);
    }

}
