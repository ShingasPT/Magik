package me.shingas.magik.spells.attack.FireballMagic;

import me.shingas.magik.Magik;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.persistence.PersistentDataType;

public class FireballListener implements Listener {

    private final Magik magik;

    public FireballListener(Magik magik) {
        this.magik = magik;
    }

    @EventHandler
    public void onFireballExplode(EntityExplodeEvent event) {

        if (!(event.getEntity() instanceof Fireball fireball))
            return;

        NamespacedKey key =
                new NamespacedKey(magik, "magic_fireball");

        if (!fireball.getPersistentDataContainer().has(
                key,
                PersistentDataType.BYTE
        )) {
            return;
        }

        event.blockList().clear();
    }

}
