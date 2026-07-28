package me.shingas.magik.magic;

import org.bukkit.Material;

import java.util.List;

public abstract class Magic {

    private final String id;
    private final String name;
    private final Material icon;
    private final MagicCategory category;
    private final List<String> description;
    private final CastType castType;
    private final long cooldownMillis;

    protected Magic(String id,
                    String name,
                    Material icon,
                    MagicCategory category,
                    List<String> description,
                    CastType castType,
                    long cooldownMillis
    ) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.category = category;
        this.description = description;
        this.castType = castType;
        this.cooldownMillis = cooldownMillis;
    }

    public abstract void cast(MagicContext context);

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Material getIcon() {
        return icon;
    }

    public MagicCategory getCategory() {
        return category;
    }

    public List<String> getDescription() { return description; }

    public CastType getCastType() { return castType; }

    /**
     * @return the cooldown for this magic, in milliseconds. 0 (or less) means no cooldown.
     */
    public long getCooldownMillis() { return cooldownMillis; }
}