package pluginsfix.glowcrafts.domain;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public record RecipeIngredient(
        ItemStack item,
        boolean matchExactMeta,
        int amount
) {
    public static RecipeIngredient of(ItemStack item, boolean matchExactMeta) {
        if (item == null || item.getType() == Material.AIR) {
            return empty();
        }
        return new RecipeIngredient(item.clone(), matchExactMeta, Math.max(1, item.getAmount()));
    }

    public static RecipeIngredient empty() {
        return new RecipeIngredient(null, false, 0);
    }

    public boolean isEmpty() {
        return item == null || item.getType() == Material.AIR || amount <= 0;
    }

    public boolean matches(ItemStack target) {
        if (isEmpty()) {
            return target == null || target.getType() == Material.AIR;
        }
        if (target == null || target.getType() == Material.AIR) {
            return false;
        }
        if (target.getAmount() < amount) {
            return false;
        }
        if (matchExactMeta) {
            return item.isSimilar(target);
        }
        return item.getType() == target.getType();
    }
}
