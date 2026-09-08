package me.shingas.magik;

import me.shingas.magik.listeners.MenuListener;
import me.shingas.magik.listeners.PlayerInteractListener;
import me.shingas.magik.listeners.StormListener;
import me.shingas.magik.managers.MagicManager;
import me.shingas.magik.managers.StormManager;
import me.shingas.magik.spells.attack.*;
import me.shingas.magik.spells.attack.FireballMagic.FireballListener;
import me.shingas.magik.spells.attack.FireballMagic.FireballMagic;
import me.shingas.magik.spells.attack.OrbitalMagic.OrbitalMagic;
import me.shingas.magik.spells.support.*;
import me.shingas.magik.spells.utility.LightMagic;
import me.shingas.magik.spells.utility.TeleportMagic;
import me.shingas.magik.spells.utility.FeatherfallMagic;
import org.bukkit.plugin.java.JavaPlugin;

public final class Magik extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        StormManager stormManager = new StormManager(this);
        MagicManager magicManager = new MagicManager(this, stormManager);

        magicManager.register(new AODMagic());
        magicManager.register(new BeamMagic());
        magicManager.register(new BlackHoleMagic());
        magicManager.register(new ChainLightningMagic());
        magicManager.register(new FireballMagic());
        magicManager.register(new IceSpearMagic());
        magicManager.register(new MeteorMagic());
        magicManager.register(new OrbitalMagic());
        magicManager.register(new StormMagic());
        magicManager.register(new VoidRiftMagic());
        magicManager.register(new BlessMagic());
        magicManager.register(new AidMagic());
        magicManager.register(new BlindnessMagic());
        magicManager.register(new SwiftwindMagic());
        magicManager.register(new BarrierMagic());
        magicManager.register(new TeleportMagic());
        magicManager.register(new LightMagic());
        magicManager.register(new FeatherfallMagic());

        getServer().getPluginManager().registerEvents(
                new FireballListener(this),
                this
        );

        getServer().getPluginManager().registerEvents(
                new StormListener(stormManager),
                this
        );

        getServer().getPluginManager().registerEvents(
                new PlayerInteractListener(magicManager),
                this
        );

        getServer().getPluginManager().registerEvents(
                new MenuListener(magicManager),
                this
        );
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
