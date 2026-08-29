package me.shingas.magik.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.shingas.magik.gui.CategoryMenu;
import me.shingas.magik.managers.MagicManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public class MagicCommand implements BasicCommand {

    private final MagicManager manager;

    public MagicCommand(MagicManager manager) {
        this.manager = manager;
    }

    @Override
    public void execute(CommandSourceStack ctx, String[] args) {
        if (!(ctx.getExecutor() instanceof Player player)) return;

        if (!player.isOp() && !player.hasPermission("magik.wizard")) {
            player.sendMessage(Component.text(
                    "You do not have access to magic.",
                    NamedTextColor.RED
            ));
            return;
        }

        new CategoryMenu(manager).open(player);
    }

}
