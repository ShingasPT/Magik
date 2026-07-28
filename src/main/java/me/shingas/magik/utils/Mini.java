package me.shingas.magik.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Mini {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private Mini() {
    }

    public static Component message(String text) {
        return MM.deserialize("<i:false>" + text);
    }
}
