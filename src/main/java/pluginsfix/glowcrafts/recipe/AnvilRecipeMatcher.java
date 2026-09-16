package pluginsfix.glowcrafts.recipe;

import org.bukkit.inventory.ItemStack;
import pluginsfix.glowcrafts.domain.CraftRecipe;
import pluginsfix.glowcrafts.domain.RecipeType;

import java.util.Collection;
import java.util.Optional;

public final class AnvilRecipeMatcher {

    public Optional<CraftRecipe> findMatch(Collection<CraftRecipe> recipes, ItemStack base, ItemStack sacrifice) {
        for (CraftRecipe recipe : recipes) {
            if (recipe.type() != RecipeType.ANVIL) {
                continue;
            }

            if (!recipe.anvilBase().matches(base)) {
                continue;
            }

            if (!recipe.anvilSacrifice().isEmpty() && !recipe.anvilSacrifice().matches(sacrifice)) {
                continue;
            }

            if (recipe.anvilSacrifice().isEmpty() && sacrifice != null && !sacrifice.getType().isAir()) {
                continue;
            }

            return Optional.of(recipe);
        }
        return Optional.empty();
    }
}
