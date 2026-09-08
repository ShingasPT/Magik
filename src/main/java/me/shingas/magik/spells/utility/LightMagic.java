package me.shingas.magik.spells.utility;

import me.shingas.magik.magic.CastType;
import me.shingas.magik.magic.Magic;
import me.shingas.magik.magic.MagicCategory;
import me.shingas.magik.magic.MagicContext;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;

import java.util.List;

public class LightMagic extends Magic {

    public LightMagic() {
        super(
                "light",
                "Light",
                Material.LANTERN,
                MagicCategory.UTILITY,
                List.of(
                        "<gray>Creates a moving light source",
                        "<gray>around you for one minute."
                ),
                CastType.RIGHT_CLICK,
                120000L
        );
    }

    @Override
    public void cast(MagicContext context) {
        Player player = context.getPlayer();
        Block[] litBlock = {null};
        BlockData[] originalData = {null};
        int[] elapsed = {0};

        scheduleTick(
                context,
                player,
                litBlock,
                originalData,
                elapsed
        );
    }

    private void scheduleTick(
            MagicContext context,
            Player player,
            Block[] litBlock,
            BlockData[] originalData,
            int[] elapsed
    ) {
        player.getScheduler().runDelayed(
                context.getPlugin(),
                task -> {
                    if (!player.isOnline() || elapsed[0] >= 60 * 20) {
                        restore(litBlock, originalData);
                        return;
                    }

                    Location orbLocation = player.getLocation()
                            .add(player.getLocation().getDirection().normalize().multiply(-0.45))
                            .add(0, 1.8, 0);

                    player.getWorld().spawnParticle(
                            Particle.DUST,
                            orbLocation,
                            3,
                            0.04,
                            0.04,
                            0.04,
                            0,
                            new Particle.DustOptions(Color.YELLOW, 1.5f)
                    );

                    Block current = player.getLocation().getBlock();
                    restore(litBlock, originalData);

                    if (current.isPassable() && current.getType() != Material.LIGHT) {
                        litBlock[0] = current;
                        originalData[0] = current.getBlockData().clone();
                        current.setType(Material.LIGHT, false);
                        Light light = (Light) current.getBlockData();
                        light.setLevel(15);
                        current.setBlockData(light, false);
                    }
                    elapsed[0]++;

                    scheduleTick(
                            context,
                            player,
                            litBlock,
                            originalData,
                            elapsed
                    );
                },
                () -> restore(litBlock, originalData),
                1L
        );
    }

    private void restore(Block[] litBlock, BlockData[] originalData) {
        if (litBlock[0] != null
                && originalData[0] != null
                && litBlock[0].getType() == Material.LIGHT) {
            litBlock[0].setBlockData(originalData[0], false);
        }
        litBlock[0] = null;
        originalData[0] = null;
    }
}
