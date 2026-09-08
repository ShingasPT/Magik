package me.shingas.magik.spells.support;

import me.shingas.magik.magic.CastType;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class SwiftwindMagic extends Magic {

    public SwiftwindMagic() {
        super(
                "swiftwind",
                "Swiftwind",
                Material.FEATHER,
                MagicCategory.SUPPORT,
                List.of(
                        "<gray>Grants Speed II and",
                        "<gray>Jump Boost I for 10 seconds."
                ),
                CastType.RIGHT_CLICK,
                10000L,
                45000L
        );
    }

    @Override
    public void cast(MagicContext context) {
        Player player = context.getPlayer();
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                10 * 20,
                1,
                false,
                true,
                true
        ));
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.JUMP_BOOST,
                10 * 20,
                0,
                false,
                true,
                true
        ));
    }
}
