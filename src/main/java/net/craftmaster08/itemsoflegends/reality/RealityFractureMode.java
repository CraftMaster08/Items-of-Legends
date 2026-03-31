package net.craftmaster08.itemsoflegends.reality;

public enum RealityFractureMode {
    LAST_LOCATION("gui.itemsoflegends.reality_fracture.mode.last_location"),
    SAME_COORDS("gui.itemsoflegends.reality_fracture.mode.same_coords"),
    TARGET_SPAWN("gui.itemsoflegends.reality_fracture.mode.target_spawn");

    private final String translationKey;

    RealityFractureMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public static RealityFractureMode fromName(String name) {
        for (RealityFractureMode value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return LAST_LOCATION;
    }
}

