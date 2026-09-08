package me.shingas.magik.spells.support;

import me.shingas.magik.magic.CastType;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import me.shingas.magik.utils.Mini;

import java.util.List;

public class BlindnessMagic extends Magic {

    public BlindnessMagic() {
        super(
                "blindness",
                "Blindness",
                Material.INK_SAC,
                MagicCategory.SUPPORT,
                List.of(
                        "<gray>Blinds the player you",
                        "<gray>are looking at."
                ),
                CastType.RIGHT_CLICK,
                45000L
        );
    }

    @Override
    public void cast(MagicContext context) {
        Player caster = context.getPlayer();
        var rayTrace = caster.getWorld().rayTraceEntities(
                caster.getEyeLocation(),
                caster.getEyeLocation().getDirection(),
                50,
                0.3,
                entity -> entity instanceof Player && entity != caster
        );
        Entity target = rayTrace == null ? null : rayTrace.getHitEntity();

        if (!(target instanceof Player player)) {
            context.failCast();
            caster.sendMessage(Mini.message(
                    "<red>You must be looking at a player to cast Blindness."
            ));
            return;
        }

        player.getScheduler().run(context.getPlugin(), task -> {
            if (player.isOnline()) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.BLINDNESS,
                        15 * 20,
                        0,
                        false,
                        true,
                        true
                ));
            }
        }, null);
    }
}
