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

public class BlessMagic extends Magic {

    public BlessMagic() {
        super(
                "bless",
                "Bless",
                Material.ENCHANTED_GOLDEN_APPLE,
                MagicCategory.SUPPORT,
                List.of(
                        "<gray>Temporarily increases your",
                        "<gray>damage with Strength."
                ),
                CastType.RIGHT_CLICK,
                45000L
        );
    }

    @Override
    public void cast(MagicContext context) {
        Player player = context.getPlayer();
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                25 * 20,
                0,
                false,
                true,
                true
        ));
    }
}
