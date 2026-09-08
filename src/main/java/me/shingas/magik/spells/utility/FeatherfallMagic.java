package me.shingas.magik.spells.utility;

import me.shingas.magik.magic.CastType;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class FeatherfallMagic extends Magic {

    public FeatherfallMagic() {
        super(
                "featherfall",
                "Featherfall",
                Material.FEATHER,
                MagicCategory.UTILITY,
                List.of(
                        "<gray>Grants Slow Falling",
                        "<gray>for 15 seconds."
                ),
                CastType.RIGHT_CLICK,
                15000L,
                30000L
        );
    }

    @Override
    public void cast(MagicContext context) {
        Player player = context.getPlayer();
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOW_FALLING,
                15 * 20,
                0,
                false,
                true,
                true
        ));
    }
}
