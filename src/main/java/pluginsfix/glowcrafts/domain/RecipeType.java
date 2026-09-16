package pluginsfix.glowcrafts.domain;

public enum RecipeType {
    SHAPED,
    SHAPELESS,
    ANVIL,
    FURNACE,
    BLAST_FURNACE,
    SMOKER;

    public static RecipeType fromString(String name) {
        if (name == null) {
            return SHAPED;
        }
        for (RecipeType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return SHAPED;
    }
}
