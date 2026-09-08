package me.shingas.magik.magic;

import me.shingas.magik.Magik;
import me.shingas.magik.managers.MagicManager;
import me.shingas.magik.managers.StormManager;
import org.bukkit.entity.Player;

public class MagicContext {

    private final Magik plugin;
    private final MagicManager magicManager;
    private final StormManager stormManager;
    private final Player player;
    private final CastTrigger trigger;
    private boolean castSuccessful = true;

    public MagicContext(Magik plugin,
                        MagicManager magicManager,
                        StormManager stormManager,
                        Player player,
                        CastTrigger trigger) {

        this.plugin = plugin;
        this.magicManager = magicManager;
        this.stormManager = stormManager;
        this.player = player;
        this.trigger = trigger;
    }

    public Magik getPlugin() {
        return plugin;
    }

    public MagicManager getMagicManager() {
        return magicManager;
    }

    public StormManager getStormManager() { return stormManager; }

    public Player getPlayer() {
        return player;
    }

    public CastTrigger getTrigger() { return trigger; }

    public void failCast() {
        castSuccessful = false;
    }

    public boolean isCastSuccessful() {
        return castSuccessful;
    }
}
