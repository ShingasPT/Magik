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

    private static final String WAND_NEXO_ID = "wizard_wand";
    private static final int WAND_CUSTOM_MODEL_DATA = 1438;

    private static final Set<String> WIZARD_DEFAULT_MAGICS = Set.of(
            "fireball",
            "icespear"
    );

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
            player.closeInventory();
            player.sendMessage(Mini.message(
                    "<red>You must be holding your magic wand."
            ));
            return;
        }

        if (!isWizardWand(item)) {
            player.closeInventory();
            player.sendMessage(Mini.message(
                    "<red>You can only apply magic to a Wizard Wand."
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

        long cooldownSeconds =
                magic.getCooldownMillis() / 1000;

        lore.add(
                Mini.message(
                        "<red>Cooldown: <yellow>"
                                + cooldownSeconds
                                + " Seconds"
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

    public boolean canUseMagic(Player player, Magic magic) {

        // Admins can use everything.
        if (player.isOp()) {
            return true;
        }

        // Player must be a Wizard to use the Magik system.
        if (!player.hasPermission("magik.wizard")) {
            return false;
        }

        // Every Wizard automatically knows these.
        if (WIZARD_DEFAULT_MAGICS.contains(magic.getId())) {
            return true;
        }

        // Everything else has to be learned/unlocked.
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

    public void castHeldMagic(Player player, CastTrigger trigger) {

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir())
            return;

        if (!item.hasItemMeta())
            return;

        if (!player.isOp() && !player.hasPermission("magik.wizard")) {
            return;
        }

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