package pluginsfix.glowcrafts.domain;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CraftRecipe {

    private final String id;
    private final RecipeType type;
    private final ItemStack result;
    private final RecipeIngredient[] shapedGrid;
    private final List<RecipeIngredient> shapelessIngredients;
    private final RecipeIngredient anvilBase;
    private final RecipeIngredient anvilSacrifice;
    private final RecipeIngredient furnaceInput;
    private final RecipeCondition condition;
    private final boolean exactMeta;

    public CraftRecipe(
            String id,
            RecipeType type,
            ItemStack result,
            RecipeIngredient[] shapedGrid,
            List<RecipeIngredient> shapelessIngredients,
            RecipeIngredient anvilBase,
            RecipeIngredient anvilSacrifice,
            RecipeIngredient furnaceInput,
            RecipeCondition condition,
            boolean exactMeta
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null").toLowerCase().trim();
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.result = result != null ? result.clone() : new ItemStack(Material.AIR);
        this.shapedGrid = shapedGrid != null ? shapedGrid.clone() : new RecipeIngredient[9];
        this.shapelessIngredients = shapelessIngredients != null
                ? new ArrayList<>(shapelessIngredients)
                : new ArrayList<>();
        this.anvilBase = anvilBase != null ? anvilBase : RecipeIngredient.empty();
        this.anvilSacrifice = anvilSacrifice != null ? anvilSacrifice : RecipeIngredient.empty();
        this.furnaceInput = furnaceInput != null ? furnaceInput : RecipeIngredient.empty();
        this.condition = condition != null ? condition : RecipeCondition.empty();
        this.exactMeta = exactMeta;
    }

    public static CraftRecipe createShaped(
            String id,
            ItemStack result,
            RecipeIngredient[] grid,
            RecipeCondition condition,
            boolean exactMeta
    ) {
        return new CraftRecipe(
                id,
                RecipeType.SHAPED,
                result,
                grid,
                Collections.emptyList(),
                RecipeIngredient.empty(),
                RecipeIngredient.empty(),
                RecipeIngredient.empty(),
                condition,
                exactMeta
        );
    }

    public static CraftRecipe createShapeless(
            String id,
            ItemStack result,
            List<RecipeIngredient> ingredients,
            RecipeCondition condition,
            boolean exactMeta
    ) {
        return new CraftRecipe(
                id,
                RecipeType.SHAPELESS,
                result,
                new RecipeIngredient[9],
                ingredients,
                RecipeIngredient.empty(),
                RecipeIngredient.empty(),
                RecipeIngredient.empty(),
                condition,
                exactMeta
        );
    }

    public static CraftRecipe createAnvil(
            String id,
            ItemStack result,
            RecipeIngredient base,
            RecipeIngredient sacrifice,
            RecipeCondition condition,
            boolean exactMeta
    ) {
        return new CraftRecipe(
                id,
                RecipeType.ANVIL,
                result,
                new RecipeIngredient[9],
                Collections.emptyList(),
                base,
                sacrifice,
                RecipeIngredient.empty(),
                condition,
                exactMeta
        );
    }

    public static CraftRecipe createFurnace(
            String id,
            RecipeType type,
            ItemStack result,
            RecipeIngredient input,
            RecipeCondition condition,
            boolean exactMeta
    ) {
        return new CraftRecipe(
                id,
                type,
                result,
                new RecipeIngredient[9],
                Collections.emptyList(),
                RecipeIngredient.empty(),
                RecipeIngredient.empty(),
                input,
                condition,
                exactMeta
        );
    }

    public String id() {
        return id;
    }

    public RecipeType type() {
        return type;
    }

    public ItemStack result() {
        return result.clone();
    }

    public RecipeIngredient[] shapedGrid() {
        return shapedGrid.clone();
    }

    public List<RecipeIngredient> shapelessIngredients() {
        return Collections.unmodifiableList(shapelessIngredients);
    }

    public RecipeIngredient anvilBase() {
        return anvilBase;
    }

    public RecipeIngredient anvilSacrifice() {
        return anvilSacrifice;
    }

    public RecipeIngredient furnaceInput() {
        return furnaceInput;
    }

    public RecipeCondition condition() {
        return condition;
    }

    public boolean exactMeta() {
        return exactMeta;
    }

    public boolean isValid() {
        if (id.isBlank()) {
            return false;
        }
        if (result == null || result.getType() == Material.AIR) {
            return false;
        }

        return switch (type) {
            case SHAPED -> hasAnyShapedIngredient();
            case SHAPELESS -> !shapelessIngredients.isEmpty() && shapelessIngredients.stream().anyMatch(i -> !i.isEmpty());
            case ANVIL -> !anvilBase.isEmpty();
            case FURNACE, BLAST_FURNACE, SMOKER -> !furnaceInput.isEmpty();
        };
    }

    private boolean hasAnyShapedIngredient() {
        for (RecipeIngredient ingredient : shapedGrid) {
            if (ingredient != null && !ingredient.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
