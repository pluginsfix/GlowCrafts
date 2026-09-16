package pluginsfix.glowcrafts.listener;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import pluginsfix.glowcrafts.domain.CraftRecipe;
import pluginsfix.glowcrafts.domain.RecipeCondition;
import pluginsfix.glowcrafts.hook.VaultEconomyHook;
import pluginsfix.glowcrafts.recipe.RecipeEngine;
import pluginsfix.glowcrafts.text.Messages;

import java.util.Optional;

public final class AnvilListener implements Listener {

    private final RecipeEngine recipeEngine;
    private final VaultEconomyHook economy;
    private final Messages messages;

    public AnvilListener(RecipeEngine recipeEngine, VaultEconomyHook economy, Messages messages) {
        this.recipeEngine = recipeEngine;
        this.economy = economy;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack base = inv.getItem(0);
        ItemStack sacrifice = inv.getItem(1);

        if (base == null || base.getType() == Material.AIR) {
            return;
        }

        Optional<CraftRecipe> matchOpt = recipeEngine.findMatchingAnvil(base, sacrifice);
        if (matchOpt.isEmpty()) {
            return;
        }

        CraftRecipe recipe = matchOpt.get();
        event.setResult(recipe.result().clone());
        inv.setRepairCost(Math.max(1, recipe.condition().levelCost()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvilClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL) {
            return;
        }

        if (event.getRawSlot() != 2) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        AnvilInventory inv = (AnvilInventory) event.getInventory();
        ItemStack base = inv.getItem(0);
        ItemStack sacrifice = inv.getItem(1);
        ItemStack result = inv.getItem(2);

        if (base == null || result == null || result.getType() == Material.AIR) {
            return;
        }

        Optional<CraftRecipe> matchOpt = recipeEngine.findMatchingAnvil(base, sacrifice);
        if (matchOpt.isEmpty()) {
            return;
        }

        CraftRecipe recipe = matchOpt.get();
        RecipeCondition condition = recipe.condition();

        if (condition.hasPermission() && !player.hasPermission(condition.permission())) {
            event.setCancelled(true);
            messages.send(player, "anvil.no-permission");
            return;
        }

        int requiredLevel = Math.max(1, condition.levelCost());
        if (player.getGameMode() != GameMode.CREATIVE && player.getLevel() < requiredLevel) {
            event.setCancelled(true);
            messages.send(player, "anvil.not-enough-level", Placeholder.parsed("level", String.valueOf(requiredLevel)));
            return;
        }

        if (condition.hasMoneyCost() && economy.isAvailable()) {
            if (!economy.has(player, condition.moneyCost())) {
                event.setCancelled(true);
                messages.send(player, "anvil.not-enough-money", Placeholder.parsed("amount", String.valueOf(condition.moneyCost())));
                return;
            }
        }

        event.setCancelled(true);

        if (player.getGameMode() != GameMode.CREATIVE) {
            player.setLevel(player.getLevel() - requiredLevel);
        }

        if (condition.hasMoneyCost() && economy.isAvailable()) {
            economy.withdraw(player, condition.moneyCost());
        }

        consumeItem(inv, 0, recipe.anvilBase().amount());
        if (!recipe.anvilSacrifice().isEmpty()) {
            consumeItem(inv, 1, recipe.anvilSacrifice().amount());
        }

        inv.setItem(2, null);
        player.getInventory().addItem(recipe.result().clone()).values().forEach(remaining ->
                player.getWorld().dropItemNaturally(player.getLocation(), remaining)
        );

        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        messages.send(player, "anvil.processed");
    }

    private void consumeItem(AnvilInventory inv, int slot, int requiredAmount) {
        ItemStack item = inv.getItem(slot);
        if (item == null) {
            return;
        }

        int consume = Math.max(1, requiredAmount);
        if (item.getAmount() <= consume) {
            inv.setItem(slot, null);
        } else {
            item.setAmount(item.getAmount() - consume);
            inv.setItem(slot, item);
        }
    }
}
