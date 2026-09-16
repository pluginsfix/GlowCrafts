package pluginsfix.glowcrafts.recipe;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import pluginsfix.glowcrafts.domain.CraftRecipe;
import pluginsfix.glowcrafts.storage.RecipeStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class RecipeEngine {

    private final Plugin plugin;
    private final RecipeStorage storage;
    private final BukkitRecipeRegistrar registrar;
    private final AnvilRecipeMatcher anvilMatcher;
    private final Map<String, CraftRecipe> recipes = new ConcurrentHashMap<>();

    public RecipeEngine(Plugin plugin, RecipeStorage storage, BukkitRecipeRegistrar registrar) {
        this.plugin = plugin;
        this.storage = storage;
        this.registrar = registrar;
        this.anvilMatcher = new AnvilRecipeMatcher();
    }

    public void loadAndRegisterAll() {
        recipes.clear();
        Map<String, CraftRecipe> loaded = storage.loadAll();
        for (CraftRecipe recipe : loaded.values()) {
            recipes.put(recipe.id(), recipe);
            registrar.register(recipe);
        }
    }

    public void registerRecipe(CraftRecipe recipe) {
        recipes.put(recipe.id(), recipe);
        storage.save(recipe);
        registrar.register(recipe);
    }

    public void deleteRecipe(String id) {
        String key = id.toLowerCase().trim();
        recipes.remove(key);
        storage.delete(key);
        registrar.unregister(new NamespacedKey(plugin, key));
    }

    public Optional<CraftRecipe> getById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(recipes.get(id.toLowerCase().trim()));
    }

    public Collection<CraftRecipe> getAll() {
        return Collections.unmodifiableCollection(recipes.values());
    }

    public Optional<CraftRecipe> findByBukkitKey(NamespacedKey key) {
        if (key == null || !key.getNamespace().equalsIgnoreCase(plugin.getName())) {
            return Optional.empty();
        }
        return getById(key.getKey());
    }

    public Optional<CraftRecipe> findMatchingAnvil(ItemStack base, ItemStack sacrifice) {
        return anvilMatcher.findMatch(recipes.values(), base, sacrifice);
    }
}
