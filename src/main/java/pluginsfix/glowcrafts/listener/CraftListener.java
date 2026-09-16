package pluginsfix.glowcrafts.listener;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import pluginsfix.glowcrafts.domain.CraftRecipe;
import pluginsfix.glowcrafts.domain.RecipeCondition;
import pluginsfix.glowcrafts.domain.RecipeIngredient;
import pluginsfix.glowcrafts.hook.VaultEconomyHook;
import pluginsfix.glowcrafts.recipe.RecipeEngine;
import pluginsfix.glowcrafts.text.Messages;

import java.util.Optional;

public final class CraftListener implements Listener {

    private final RecipeEngine recipeEngine;
    private final VaultEconomyHook economy;
    private final Messages messages;

    public CraftListener(RecipeEngine recipeEngine, VaultEconomyHook economy, Messages messages) {
        this.recipeEngine = recipeEngine;
        this.economy = economy;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (!(recipe instanceof Keyed keyed)) {
            return;
        }

        NamespacedKey key = keyed.getKey();
        Optional<CraftRecipe> customRecipeOpt = recipeEngine.findByBukkitKey(key);
        if (customRecipeOpt.isEmpty()) {
            return;
        }

        CraftRecipe custom = customRecipeOpt.get();
        CraftingInventory inv = event.getInventory();

        for (HumanEntity viewer : event.getViewers()) {
            if (viewer instanceof Player player) {
                RecipeCondition condition = custom.condition();
                if (condition.hasPermission() && !player.hasPermission(condition.permission())) {
                    inv.setResult(null);
                    return;
                }
            }
        }

        if (custom.exactMeta()) {
            ItemStack[] matrix = inv.getMatrix();
            if (!verifyMatrix(custom, matrix)) {
                inv.setResult(null);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        Recipe recipe = event.getRecipe();
        if (!(recipe instanceof Keyed keyed)) {
            return;
        }

        NamespacedKey key = keyed.getKey();
        Optional<CraftRecipe> customRecipeOpt = recipeEngine.findByBukkitKey(key);
        if (customRecipeOpt.isEmpty()) {
            return;
        }

        CraftRecipe custom = customRecipeOpt.get();
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        RecipeCondition condition = custom.condition();

        if (condition.hasPermission() && !player.hasPermission(condition.permission())) {
            event.setCancelled(true);
            messages.send(player, "craft.no-permission");
            return;
        }

        int crafts = 1;
        if (event.isShiftClick()) {
            int maxCrafts = Integer.MAX_VALUE;
            for (ItemStack item : event.getInventory().getMatrix()) {
                if (item != null && !item.getType().isAir()) {
                    maxCrafts = Math.min(maxCrafts, item.getAmount());
                }
            }
            if (maxCrafts != Integer.MAX_VALUE && maxCrafts > 0) {
                crafts = maxCrafts;
            }
        }

        int totalLevelCost = condition.levelCost() * crafts;
        double totalMoneyCost = condition.moneyCost() * crafts;

        if (totalLevelCost > 0) {
            if (player.getLevel() < totalLevelCost) {
                event.setCancelled(true);
                messages.send(player, "craft.not-enough-level", Placeholder.parsed("level", String.valueOf(totalLevelCost)));
                return;
            }
        }

        if (totalMoneyCost > 0.0 && economy.isAvailable()) {
            if (!economy.has(player, totalMoneyCost)) {
                event.setCancelled(true);
                messages.send(player, "craft.not-enough-money", Placeholder.parsed("amount", String.valueOf(totalMoneyCost)));
                return;
            }
        }

        if (totalLevelCost > 0) {
            player.setLevel(player.getLevel() - totalLevelCost);
        }

        if (totalMoneyCost > 0.0 && economy.isAvailable()) {
            economy.withdraw(player, totalMoneyCost);
        }
    }

    private boolean verifyMatrix(CraftRecipe recipe, ItemStack[] matrix) {
        switch (recipe.type()) {
            case SHAPED -> {
                RecipeIngredient[] grid = recipe.shapedGrid();
                for (int i = 0; i < 9 && i < matrix.length; i++) {
                    RecipeIngredient ing = grid[i];
                    ItemStack slotItem = matrix[i];
                    if (ing == null || ing.isEmpty()) {
                        if (slotItem != null && slotItem.getType() != Material.AIR) {
                            return false;
                        }
                    } else {
                        if (!ing.matches(slotItem)) {
                            return false;
                        }
                    }
                }
                return true;
            }
            case SHAPELESS -> {
                return true;
            }
            default -> {
                return true;
            }
        }
    }
}
