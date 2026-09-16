package pluginsfix.glowcrafts.domain;

public record RecipeCondition(
        String permission,
        int levelCost,
        double moneyCost,
        int furnaceCookingTimeTicks,
        float furnaceExperience
) {
    public static RecipeCondition empty() {
        return new RecipeCondition(null, 0, 0.0, 200, 0.0f);
    }

    public boolean hasPermission() {
        return permission != null && !permission.isBlank();
    }

    public boolean hasLevelCost() {
        return levelCost > 0;
    }

    public boolean hasMoneyCost() {
        return moneyCost > 0.0;
    }
}
