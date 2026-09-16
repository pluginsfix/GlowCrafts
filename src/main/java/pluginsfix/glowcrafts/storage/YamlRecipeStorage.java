package pluginsfix.glowcrafts.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import pluginsfix.glowcrafts.domain.CraftRecipe;
import pluginsfix.glowcrafts.domain.RecipeCondition;
import pluginsfix.glowcrafts.domain.RecipeIngredient;
import pluginsfix.glowcrafts.domain.RecipeType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class YamlRecipeStorage implements RecipeStorage {

    private final File file;
    private final Logger logger;
    private final Map<String, CraftRecipe> cache = new HashMap<>();

    public YamlRecipeStorage(File dataFolder, Logger logger) {
        this.file = new File(dataFolder, "recipes.yml");
        this.logger = logger;
    }

    @Override
    public Map<String, CraftRecipe> loadAll() {
        cache.clear();
        if (!file.exists()) {
            return cache;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = config.getConfigurationSection("recipes");
        if (root == null) {
            return cache;
        }

        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }

            try {
                CraftRecipe recipe = deserializeRecipe(id, section);
                if (recipe != null && recipe.isValid()) {
                    cache.put(recipe.id(), recipe);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to load recipe id=" + id, e);
            }
        }

        return new HashMap<>(cache);
    }

    @Override
    public Optional<CraftRecipe> findById(String id) {
        return Optional.ofNullable(cache.get(id.toLowerCase().trim()));
    }

    @Override
    public void save(CraftRecipe recipe) {
        cache.put(recipe.id(), recipe);

        YamlConfiguration config = file.exists()
                ? YamlConfiguration.loadConfiguration(file)
                : new YamlConfiguration();

        ConfigurationSection section = config.createSection("recipes." + recipe.id());
        serializeRecipe(recipe, section);

        try {
            config.save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save recipe id=" + recipe.id(), e);
        }
    }

    @Override
    public void delete(String id) {
        String key = id.toLowerCase().trim();
        cache.remove(key);

        if (!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("recipes." + key, null);

        try {
            config.save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to remove recipe from file id=" + key, e);
        }
    }

    private void serializeRecipe(CraftRecipe recipe, ConfigurationSection section) {
        section.set("type", recipe.type().name());
        section.set("exact-meta", recipe.exactMeta());
        section.set("result", recipe.result());

        RecipeCondition condition = recipe.condition();
        section.set("condition.permission", condition.permission());
        section.set("condition.level-cost", condition.levelCost());
        section.set("condition.money-cost", condition.moneyCost());
        section.set("condition.furnace-time", condition.furnaceCookingTimeTicks());
        section.set("condition.furnace-exp", (double) condition.furnaceExperience());

        switch (recipe.type()) {
            case SHAPED -> {
                RecipeIngredient[] grid = recipe.shapedGrid();
                for (int i = 0; i < grid.length; i++) {
                    RecipeIngredient ingredient = grid[i];
                    if (ingredient != null && !ingredient.isEmpty()) {
                        section.set("shaped." + i, ingredient.item());
                    }
                }
            }
            case SHAPELESS -> {
                List<ItemStack> items = new ArrayList<>();
                for (RecipeIngredient ingredient : recipe.shapelessIngredients()) {
                    if (ingredient != null && !ingredient.isEmpty()) {
                        items.add(ingredient.item());
                    }
                }
                section.set("shapeless", items);
            }
            case ANVIL -> {
                if (!recipe.anvilBase().isEmpty()) {
                    section.set("anvil.base", recipe.anvilBase().item());
                }
                if (!recipe.anvilSacrifice().isEmpty()) {
                    section.set("anvil.sacrifice", recipe.anvilSacrifice().item());
                }
            }
            case FURNACE, BLAST_FURNACE, SMOKER -> {
                if (!recipe.furnaceInput().isEmpty()) {
                    section.set("furnace.input", recipe.furnaceInput().item());
                }
            }
        }
    }

    private CraftRecipe deserializeRecipe(String id, ConfigurationSection section) {
        RecipeType type = RecipeType.fromString(section.getString("type"));
        boolean exactMeta = section.getBoolean("exact-meta", true);
        ItemStack result = section.getItemStack("result");

        String permission = section.getString("condition.permission");
        int levelCost = section.getInt("condition.level-cost", 0);
        double moneyCost = section.getDouble("condition.money-cost", 0.0);
        int furnaceTime = section.getInt("condition.furnace-time", 200);
        float furnaceExp = (float) section.getDouble("condition.furnace-exp", 0.0);

        RecipeCondition condition = new RecipeCondition(permission, levelCost, moneyCost, furnaceTime, furnaceExp);

        switch (type) {
            case SHAPED -> {
                RecipeIngredient[] grid = new RecipeIngredient[9];
                ConfigurationSection shapedSection = section.getConfigurationSection("shaped");
                if (shapedSection != null) {
                    for (int i = 0; i < 9; i++) {
                        ItemStack item = shapedSection.getItemStack(String.valueOf(i));
                        grid[i] = item != null ? RecipeIngredient.of(item, exactMeta) : RecipeIngredient.empty();
                    }
                }
                return CraftRecipe.createShaped(id, result, grid, condition, exactMeta);
            }
            case SHAPELESS -> {
                List<RecipeIngredient> list = new ArrayList<>();
                List<?> rawList = section.getList("shapeless");
                if (rawList != null) {
                    for (Object obj : rawList) {
                        if (obj instanceof ItemStack item) {
                            list.add(RecipeIngredient.of(item, exactMeta));
                        }
                    }
                }
                return CraftRecipe.createShapeless(id, result, list, condition, exactMeta);
            }
            case ANVIL -> {
                ItemStack baseItem = section.getItemStack("anvil.base");
                ItemStack sacrificeItem = section.getItemStack("anvil.sacrifice");
                RecipeIngredient base = RecipeIngredient.of(baseItem, exactMeta);
                RecipeIngredient sacrifice = RecipeIngredient.of(sacrificeItem, exactMeta);
                return CraftRecipe.createAnvil(id, result, base, sacrifice, condition, exactMeta);
            }
            case FURNACE, BLAST_FURNACE, SMOKER -> {
                ItemStack inputItem = section.getItemStack("furnace.input");
                RecipeIngredient input = RecipeIngredient.of(inputItem, exactMeta);
                return CraftRecipe.createFurnace(id, type, result, input, condition, exactMeta);
            }
        }
        return null;
    }
}
