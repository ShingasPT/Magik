package me.shingas.magik.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.shingas.magik.gui.CategoryMenu;
import me.shingas.magik.managers.MagicManager;
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

        new CategoryMenu(manager).open(player);
    }

    @Override
    public @Nullable String permission() {
        return "Magik.Admin";
    }

}
