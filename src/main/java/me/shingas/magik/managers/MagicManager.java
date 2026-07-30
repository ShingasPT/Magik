package me.shingas.magik.managers;

import me.shingas.magik.Magik;
import me.shingas.magik.magic.CastTrigger;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import me.shingas.magik.utils.Mini;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class MagicManager {

    private final Magik plugin;
    private final StormManager stormManager;

    private final Map<String, Magic> magics = new HashMap<>();
    private final Map<UUID, Magic> selectedMagic = new HashMap<>();
    // playerId -> (magicId -> timestamp the cooldown expires, in millis)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final NamespacedKey magicKey;

    public MagicManager(Magik plugin, StormManager stormManager) {
        this.plugin = plugin;
        this.stormManager = stormManager;
        this.magicKey = new NamespacedKey(plugin, "magic");
    }

    public void register(Magic magic) {
        magics.put(magic.getId(), magic);
    }

    public Magic getMagic(String id) {
        return magics.get(id);
    }

    public Collection<Magic> getMagics() {
        return magics.values();
    }

    public List<Magic> getByCategory(MagicCategory category) {
        return magics.values().stream()
                .filter(m -> m.getCategory() == category)
                .toList();
    }

    public NamespacedKey getMagicKey() {
        return magicKey;
    }

    public void selectMagic(Player player, Magic magic) {
        selectedMagic.put(player.getUniqueId(), magic);
    }

    public Magic getSelectedMagic(Player player) {
        return selectedMagic.get(player.getUniqueId());
    }

    public void applyMagic(Player player, Magic magic) {

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            player.getOpenInventory().close();
            player.sendMessage(Mini.message("<red>You must be holding an item."));
            return;
        }

        ItemMeta meta = item.getItemMeta();

        meta.displayName(Mini.message("<gold>" + magic.getName() + " Magic"));
        List<Component> lore = magic.getDescription()
                .stream()
                .map(Mini::message)
                .collect(Collectors.toList());

        lore.add(Component.empty());

        switch (magic.getCastType()) {

            case RIGHT_CLICK ->
                    lore.add(Mini.message("<yellow>▶ Right Click <gray>to cast"));

            case LEFT_CLICK ->
                    lore.add(Mini.message("<yellow>▶ Left Click <gray>to cast"));

            case BOTH -> {
                lore.add(Mini.message("<yellow>▶ Right Click <gray>to cast on yourself"));
                lore.add(Mini.message("<yellow>▶ Left Click <gray>to cast on others"));
            }
        }

        long cooldownSeconds = magic.getCooldownMillis() / 1000;
        lore.add(Mini.message(
                "<red>Cooldown: <yellow>" + cooldownSeconds + " Seconds"
        ));

        meta.lore(lore);

        meta.getPersistentDataContainer().set(
                magicKey,
                PersistentDataType.STRING,
                magic.getId()
        );

        item.setItemMeta(meta);

        player.closeInventory();

        player.sendMessage(Mini.message(
                "<green>Applied <gold>" + magic.getName() + " Magic<green> to your item!"
        ));
    }

    /**
     * @return the number of milliseconds remaining before {@code player} can cast
     *         {@code magic} again, or 0 if it is not on cooldown.
     */
    public long getRemainingCooldown(Player player, Magic magic) {

        if (magic.getCooldownMillis() <= 0)
            return 0;

        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());

        if (playerCooldowns == null)
            return 0;

        Long expiresAt = playerCooldowns.get(magic.getId());

        if (expiresAt == null)
            return 0;

        long remaining = expiresAt - System.currentTimeMillis();

        return Math.max(remaining, 0);
    }

    public boolean isOnCooldown(Player player, Magic magic) {
        return getRemainingCooldown(player, magic) > 0;
    }

    /**
     * Starts the cooldown for {@code magic} for {@code player}, based on the magic's
     * configured cooldown duration.
     */
    public void applyCooldown(Player player, Magic magic) {

        if (magic.getCooldownMillis() <= 0)
            return;

        cooldowns
                .computeIfAbsent(player.getUniqueId(), id -> new HashMap<>())
                .put(magic.getId(), System.currentTimeMillis() + magic.getCooldownMillis());
    }

    /**
     * Clears any active cooldown for {@code magic} for {@code player}.
     */
    public void clearCooldown(Player player, Magic magic) {

        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());

        if (playerCooldowns != null)
            playerCooldowns.remove(magic.getId());
    }

    public void castHeldMagic(Player player, CastTrigger trigger) {

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir())
            return;

        if (!item.hasItemMeta())
            return;

        ItemMeta meta = item.getItemMeta();

        String id = meta.getPersistentDataContainer().get(
                magicKey,
                PersistentDataType.STRING
        );

        if (id == null)
            return;

        Magic magic = getMagic(id);

        if (magic == null)
            return;

        if (!magic.getCastType().allows(trigger)) {
            return;
        }

        if (isOnCooldown(player, magic)) {
            double secondsLeft = getRemainingCooldown(player, magic) / 1000.0;
            player.sendMessage(Mini.message(
                    "<red>" + magic.getName() + " is on cooldown for <gold>"
                            + String.format("%.1f", secondsLeft) + "s"
            ));
            return;
        }

        magic.cast(new MagicContext(
                plugin,
                this,
                stormManager,
                player,
                trigger
        ));

        applyCooldown(player, magic);
    }

    public Magik getPlugin() {
        return plugin;
    }
}