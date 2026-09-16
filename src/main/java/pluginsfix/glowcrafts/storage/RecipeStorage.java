package pluginsfix.glowcrafts.storage;

import pluginsfix.glowcrafts.domain.CraftRecipe;

import java.util.Map;
import java.util.Optional;

public interface RecipeStorage {

    Map<String, CraftRecipe> loadAll();

    Optional<CraftRecipe> findById(String id);

    void save(CraftRecipe recipe);

    void delete(String id);
}
