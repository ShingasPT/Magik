package me.shingas.magik.gui;

import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.managers.MagicManager;
import me.shingas.magik.utils.ItemBuilder;
import me.shingas.magik.utils.Mini;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagicMenu implements InventoryHolder {

    private final Inventory inventory;
    private final MagicManager manager;
    private final MagicCategory category;
    private final Map<Integer, Magic> magicSlots = new HashMap<>();
    public static final int BACK_SLOT = 49;

    public MagicMenu(
            MagicManager manager,
            MagicCategory category,
            Player player
    ) {
        this.manager = manager;
        this.category = category;

        inventory = Bukkit.createInventory(
                this,
                54,
                Mini.message(
                        "<dark_purple>"
                                + category.name()
                                + " Magic"
                )
        );

        initializeItems(player);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private void initializeItems(Player player) {

        int slot = 0;

        for (Magic magic :
                manager.getByCategory(player, category)) {

            inventory.setItem(
                    slot,
                    new ItemBuilder(magic.getIcon())
                            .name("<gold>" + magic.getName())
                            .lore(magic.getDescription())
                            .build()
            );

            magicSlots.put(slot, magic);

            slot++;
        }

        inventory.setItem(
                BACK_SLOT,
                new ItemBuilder(Material.ARROW)
                        .name("<red>Return")
                        .lore(List.of(
                                "<gray>Return to the categories menu"
                        ))
                        .build()
        );
    }

    public Magic getMagic(int slot) {
        return magicSlots.get(slot);
    }
}
