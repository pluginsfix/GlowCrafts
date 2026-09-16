package pluginsfix.glowcrafts.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.plugin.Plugin;
import pluginsfix.glowcrafts.domain.CraftRecipe;
import pluginsfix.glowcrafts.domain.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BukkitRecipeRegistrar {

    private final Plugin plugin;
    private final Logger logger;

    public BukkitRecipeRegistrar(Plugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public void register(CraftRecipe recipe) {
        NamespacedKey key = new NamespacedKey(plugin, recipe.id());
        unregister(key);

        try {
            switch (recipe.type()) {
                case SHAPED -> registerShaped(key, recipe);
                case SHAPELESS -> registerShapeless(key, recipe);
                case FURNACE -> registerFurnace(key, recipe);
                case BLAST_FURNACE -> registerBlasting(key, recipe);
                case SMOKER -> registerSmoking(key, recipe);
                case ANVIL -> {}
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to register Bukkit recipe id=" + recipe.id(), e);
        }
    }

    public void unregister(NamespacedKey key) {
        try {
            Bukkit.removeRecipe(key);
        } catch (Exception ignored) {
        }
    }

    private void registerShaped(NamespacedKey key, CraftRecipe recipe) {
        ShapedRecipe shaped = new ShapedRecipe(key, recipe.result());
        shaped.shape("ABC", "DEF", "GHI");

        char[] chars = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
        RecipeIngredient[] grid = recipe.shapedGrid();

        Map<Character, RecipeChoice> choices = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            RecipeIngredient ingredient = i < grid.length ? grid[i] : null;
            if (ingredient != null && !ingredient.isEmpty()) {
                choices.put(chars[i], toChoice(ingredient));
            }
        }

        for (Map.Entry<Character, RecipeChoice> entry : choices.entrySet()) {
            shaped.setIngredient(entry.getKey(), entry.getValue());
        }

        Bukkit.addRecipe(shaped);
    }

    private void registerShapeless(NamespacedKey key, CraftRecipe recipe) {
        ShapelessRecipe shapeless = new ShapelessRecipe(key, recipe.result());
        for (RecipeIngredient ingredient : recipe.shapelessIngredients()) {
            if (ingredient != null && !ingredient.isEmpty()) {
                shapeless.addIngredient(toChoice(ingredient));
            }
        }
        Bukkit.addRecipe(shapeless);
    }

    private void registerFurnace(NamespacedKey key, CraftRecipe recipe) {
        RecipeIngredient input = recipe.furnaceInput();
        if (input.isEmpty()) {
            return;
        }

        FurnaceRecipe furnace = new FurnaceRecipe(
                key,
                recipe.result(),
                toChoice(input),
                recipe.condition().furnaceExperience(),
                recipe.condition().furnaceCookingTimeTicks()
        );
        Bukkit.addRecipe(furnace);
    }

    private void registerBlasting(NamespacedKey key, CraftRecipe recipe) {
        RecipeIngredient input = recipe.furnaceInput();
        if (input.isEmpty()) {
            return;
        }

        BlastingRecipe blasting = new BlastingRecipe(
                key,
                recipe.result(),
                toChoice(input),
                recipe.condition().furnaceExperience(),
                recipe.condition().furnaceCookingTimeTicks()
        );
        Bukkit.addRecipe(blasting);
    }

    private void registerSmoking(NamespacedKey key, CraftRecipe recipe) {
        RecipeIngredient input = recipe.furnaceInput();
        if (input.isEmpty()) {
            return;
        }

        SmokingRecipe smoking = new SmokingRecipe(
                key,
                recipe.result(),
                toChoice(input),
                recipe.condition().furnaceExperience(),
                recipe.condition().furnaceCookingTimeTicks()
        );
        Bukkit.addRecipe(smoking);
    }

    private RecipeChoice toChoice(RecipeIngredient ingredient) {
        if (ingredient.matchExactMeta()) {
            return new RecipeChoice.ExactChoice(ingredient.item());
        }
        return new RecipeChoice.MaterialChoice(ingredient.item().getType());
    }
}
