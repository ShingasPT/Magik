package me.shingas.magik.magic;

public enum CastType {
    LEFT_CLICK,
    RIGHT_CLICK,
    BOTH;

    public boolean allows(CastTrigger trigger) {
        return switch (this) {
            case LEFT_CLICK -> trigger == CastTrigger.LEFT;
            case RIGHT_CLICK -> trigger == CastTrigger.RIGHT;
            case BOTH -> true;
        };
    }
}
