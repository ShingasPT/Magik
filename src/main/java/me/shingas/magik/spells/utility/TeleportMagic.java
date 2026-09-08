package me.shingas.magik.spells.utility;

import me.shingas.magik.magic.CastType;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class TeleportMagic extends Magic {

    public TeleportMagic() {
        super(
                "teleport",
                "Teleport",
                Material.ENDER_PEARL,
                MagicCategory.UTILITY,
                List.of(
                        "<gray>Throw an ender pearl",
                        "<gray>where your cursor points."
                ),
                CastType.RIGHT_CLICK,
                15000L
        );
    }

    @Override
    public void cast(MagicContext context) {
        Player player = context.getPlayer();
        Vector direction = player.getEyeLocation().getDirection().normalize();
        EnderPearl pearl = player.getWorld().spawn(
                player.getEyeLocation(),
                EnderPearl.class
        );
        pearl.setShooter(player);
        pearl.setVelocity(direction);
    }
}
