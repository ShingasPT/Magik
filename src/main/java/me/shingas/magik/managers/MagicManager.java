package me.shingas.magik.managers;

import com.nexomc.nexo.api.NexoItems;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MagicManager {

    private final Magik plugin;
    private final StormManager stormManager;

    private final Map<String, Magic> magics = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    private final NamespacedKey magicKey;

    private static final String WAND_NEXO_ID = "wizard_wand";
    private static final String ATTACK_BOOK_NEXO_ID = "wizard_attack_book";
    private static final String SUPPORT_BOOK_NEXO_ID = "wizard_support_book";
    private static final String UTILITY_BOOK_NEXO_ID = "wizard_utility_book";

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

    public boolean isWizardBook(ItemStack item) {
        return getBookCategory(item) != null;
    }

    public MagicCategory getBookCategory(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }

        String nexoId = NexoItems.idFromItem(item);
        if (nexoId == null) {
            return null;
        }

        return switch (nexoId) {
            case ATTACK_BOOK_NEXO_ID -> MagicCategory.ATTACK;
            case SUPPORT_BOOK_NEXO_ID -> MagicCategory.SUPPORT;
            case UTILITY_BOOK_NEXO_ID -> MagicCategory.UTILITY;
            default -> null;
        };
    }

    public void applyMagic(Player player, Magic magic) {

        ItemStack item = findWizardWand(player);

        if (item == null) {
            player.closeInventory();
            player.sendMessage(Mini.message(
                    "<red>You must have a magic wand in your inventory."
            ));
            return;
        }

        ItemMeta meta = item.getItemMeta();

        List<Component> lore = new ArrayList<>();

        lore.add(
                Mini.message(
                        "<light_purple>Selected Magic: <gold>"
                                + magic.getName()
                )
        );

        lore.add(Component.empty());

        lore.addAll(
                magic.getDescription()
                        .stream()
                        .map(Mini::message)
                        .toList()
        );

        lore.add(Component.empty());

        switch (magic.getCastType()) {

            case RIGHT_CLICK ->
                    lore.add(
                            Mini.message(
                                    "<yellow>▶ Right Click <gray>to cast"
                            )
                    );

            case LEFT_CLICK ->
                    lore.add(
                            Mini.message(
                                    "<yellow>▶ Left Click <gray>to cast"
                            )
                    );

            case BOTH -> {
                lore.add(
                        Mini.message(
                                "<yellow>▶ Right Click <gray>to cast on yourself"
                        )
                );

                lore.add(
                        Mini.message(
                                "<yellow>▶ Left Click <gray>to cast on others"
                        )
                );
            }
        }

        lore.add(
                Mini.message(
                        "<aqua>Cast Time: <yellow>"
                                + formatTime(magic.getCastTimeMillis())
                )
        );

        lore.add(
                Mini.message(
                        "<red>Cooldown: <yellow>"
                                + formatTime(magic.getCooldownMillis())
                )
        );

        meta.lore(lore);

        meta.getPersistentDataContainer().set(
                magicKey,
                PersistentDataType.STRING,
                magic.getId()
        );

        item.setItemMeta(meta);

        player.closeInventory();

        player.sendMessage(
                Mini.message(
                        "<green>Selected <gold>"
                                + magic.getName()
                                + " Magic<green>!"
                )
        );
    }

    private ItemStack findWizardWand(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (isWizardWand(mainHand)) {
            return mainHand;
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (isWizardWand(offHand)) {
            return offHand;
        }

        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (isWizardWand(item)) {
                return item;
            }
        }

        return null;
    }

    private boolean isWizardWand(ItemStack item) {

        if (item == null || item.getType().isAir())
            return false;

        return WAND_NEXO_ID.equals(NexoItems.idFromItem(item)
        );
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

        if (remaining <= 0) {
            cooldowns.computeIfPresent(
                    player.getUniqueId(),
                    (id, activeCooldowns) -> {
                        activeCooldowns.remove(magic.getId(), expiresAt);
                        return activeCooldowns.isEmpty() ? null : activeCooldowns;
                    }
            );
            return 0;
        }

        return remaining;
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
                .computeIfAbsent(player.getUniqueId(), id -> new ConcurrentHashMap<>())
                .put(magic.getId(), System.currentTimeMillis() + magic.getCooldownMillis());
    }

    public boolean canUseMagic(Player player, Magic magic) {

        // Admins can use everything.
        if (player.isOp()) {
            return true;
        }

        // Player must be a Wizard to use the Magik system.
        if (!player.hasPermission("magik.wizard")) {
            return false;
        }

        return player.hasPermission(
                "magik.spell." + magic.getId()
        );
    }

    public List<Magic> getByCategory(Player player, MagicCategory category) {
        return magics.values().stream()
                .filter(magic -> magic.getCategory() == category)
                .filter(magic -> canUseMagic(player, magic))
                .toList();
    }

    public List<Magic> getAllByCategory(MagicCategory category) {
        return magics.values().stream()
                .filter(magic -> magic.getCategory() == category)
                .toList();
    }

    public void castHeldMagic(Player player, CastTrigger trigger) {

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir())
            return;

        if (!isWizardWand(item))
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

        if (!canUseMagic(player, magic))
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

        MagicContext context = new MagicContext(
                plugin,
                this,
                stormManager,
                player,
                trigger
        );

        magic.cast(context);

        if (context.isCastSuccessful()) {
            applyCooldown(player, magic);
        }
    }

    public Magik getPlugin() {
        return plugin;
    }

    private String formatTime(long milliseconds) {
        if (milliseconds <= 0) {
            return "Instant";
        }

        long seconds = milliseconds / 1000;
        if (seconds % 60 == 0) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute" : " minutes");
        }

        return seconds + (seconds == 1 ? " second" : " seconds");
    }
}