package me.shingas.magik.spells.attack.FireballMagic;

import me.shingas.magik.Magik;
import me.shingas.magik.magic.CastType;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.List;

public class FireballMagic extends Magic {

    private Magik magik;

    public FireballMagic() {
        super(
                "fireball",
                "Fireball",
                Material.FIRE_CHARGE,
                MagicCategory.ATTACK,
                List.of(
                        "<gray>Launch a blazing fireball",
                        "<gray>that explodes on impact",
                        "<gray>and engulfs foes in flames."
                ),
                CastType.RIGHT_CLICK,
                5000L
        );
    }

    @Override
    public void cast(MagicContext context) {
        Player player = context.getPlayer();
        Location eye = player.getEyeLocation();

        // Spawn fireball
        Fireball fireball = player.getWorld().spawn(eye, Fireball.class);

        magik = context.getPlugin();
        fireball.getPersistentDataContainer().set(
                new NamespacedKey(magik, "magic_fireball"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        // Set direction (forward)
        Vector direction = eye.getDirection().normalize();
        fireball.setDirection(direction);

        // Optional tweaks
        fireball.setYield(2); // explosion power
        fireball.setIsIncendiary(false); // fire on impact
        fireball.setShooter(player);

        // Optional: make it faster
        fireball.setVelocity(direction.multiply(2));
    }

}
