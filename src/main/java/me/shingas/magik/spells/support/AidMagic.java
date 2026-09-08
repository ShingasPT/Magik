package me.shingas.magik.spells.support;

import me.shingas.magik.magic.CastType;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class AidMagic extends Magic {

    public AidMagic() {
        super(
                "aid",
                "Aid",
                Material.GLOWSTONE_DUST,
                MagicCategory.SUPPORT,
                List.of(
                        "<gray>Restores half of your",
                        "<gray>maximum health."
                ),
                CastType.RIGHT_CLICK,
                45000L
        );
    }

    @Override
    public void cast(MagicContext context) {
        Player player = context.getPlayer();
        AttributeInstance maxHealthAttribute =
                player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttribute == null) {
            context.failCast();
            return;
        }

        double maxHealth = maxHealthAttribute.getValue();
        double healedHealth = Math.min(
                maxHealth,
                player.getHealth() + maxHealth / 2.0
        );
        player.setHealth(healedHealth);
    }
}
