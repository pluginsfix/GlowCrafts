package pluginsfix.glowcrafts.gui.holder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pluginsfix.glowcrafts.config.PluginConfig;
import pluginsfix.glowcrafts.domain.CraftRecipe;
import pluginsfix.glowcrafts.domain.RecipeCondition;
import pluginsfix.glowcrafts.domain.RecipeIngredient;
import pluginsfix.glowcrafts.gui.ItemBuilder;
import pluginsfix.glowcrafts.gui.prompt.ChatInputHandler;
import pluginsfix.glowcrafts.recipe.RecipeEngine;
import pluginsfix.glowcrafts.text.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ShapelessEditorHolder implements GlowHolder {

    private static final int[] INGREDIENT_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int RESULT_SLOT = 24;
    private static final Set<Integer> ALLOWED_SLOTS = Set.of(10, 11, 12, 19, 20, 21, 28, 29, 30, 24);

    private final Messages messages;
    private final RecipeEngine recipeEngine;
    private final ChatInputHandler chatInput;
    private final PluginConfig config;

    private String id;
    private String permission;
    private int levelCost;
    private double moneyCost;
    private boolean exactMeta = true;
    private Inventory inventory;

    public ShapelessEditorHolder(Messages messages, RecipeEngine recipeEngine, ChatInputHandler chatInput, PluginConfig config, String id) {
        this.messages = messages;
        this.recipeEngine = recipeEngine;
        this.chatInput = chatInput;
        this.config = config;
        this.id = id;
    }

    public static boolean isAllowedSlot(int slot) {
        return ALLOWED_SLOTS.contains(slot);
    }

    public static void openNew(Player player, Messages messages, RecipeEngine recipeEngine, ChatInputHandler chatInput, PluginConfig config) {
        String defaultId = "recipe_" + System.currentTimeMillis() % 10000;
        ShapelessEditorHolder holder = new ShapelessEditorHolder(messages, recipeEngine, chatInput, config, defaultId);
        Inventory inv = Bukkit.createInventory(holder, 54, messages.componentWithoutPrefix("gui.editor.title-shapeless"));
        holder.inventory = inv;
        holder.render();
        player.openInventory(inv);
    }

    public static void openEdit(Player player, Messages messages, RecipeEngine recipeEngine, ChatInputHandler chatInput, PluginConfig config, CraftRecipe recipe) {
        ShapelessEditorHolder holder = new ShapelessEditorHolder(messages, recipeEngine, chatInput, config, recipe.id());
        holder.permission = recipe.condition().permission();
        holder.levelCost = recipe.condition().levelCost();
        holder.moneyCost = recipe.condition().moneyCost();
        holder.exactMeta = recipe.exactMeta();

        Inventory inv = Bukkit.createInventory(holder, 54, messages.componentWithoutPrefix("gui.editor.title-shapeless"));
        holder.inventory = inv;
        holder.render();

        List<RecipeIngredient> list = recipe.shapelessIngredients();
        for (int i = 0; i < INGREDIENT_SLOTS.length && i < list.size(); i++) {
            RecipeIngredient ing = list.get(i);
            if (!ing.isEmpty()) {
                inv.setItem(INGREDIENT_SLOTS[i], ing.item().clone());
            }
        }
        inv.setItem(RESULT_SLOT, recipe.result().clone());

        player.openInventory(inv);
    }

    public void render() {
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).build();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (!ALLOWED_SLOTS.contains(i)) {
                inventory.setItem(i, filler);
            }
        }

        inventory.setItem(23, new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .name(messages.componentWithoutPrefix("gui.editor.grid-label"))
                .build());

        updateButtons();
    }

    public void updateButtons() {
        inventory.setItem(8, new ItemBuilder(Material.NAME_TAG)
                .name(messages.componentWithoutPrefix("gui.editor.id-button", Placeholder.parsed("id", id)))
                .lore(messages.componentList("gui.editor.id-lore"))
                .build());

        String permText = permission != null && !permission.isBlank() ? permission : messages.raw("common.none");
        inventory.setItem(17, new ItemBuilder(Material.TRIPWIRE_HOOK)
                .name(messages.componentWithoutPrefix("gui.editor.permission-button", Placeholder.parsed("permission", permText)))
                .lore(messages.componentList("gui.editor.permission-lore"))
                .build());

        inventory.setItem(26, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name(messages.componentWithoutPrefix("gui.editor.level-button", Placeholder.parsed("level", String.valueOf(levelCost))))
                .lore(messages.componentList("gui.editor.level-lore"))
                .build());

        inventory.setItem(35, new ItemBuilder(Material.GOLD_INGOT)
                .name(messages.componentWithoutPrefix("gui.editor.money-button", Placeholder.parsed("money", String.valueOf(moneyCost))))
                .lore(messages.componentList("gui.editor.money-lore"))
                .build());

        String exactText = exactMeta ? messages.raw("common.yes") : messages.raw("common.no");
        inventory.setItem(44, new ItemBuilder(exactMeta ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(messages.componentWithoutPrefix("gui.editor.exact-meta-button", Placeholder.parsed("exact_meta", exactText)))
                .lore(messages.componentList("gui.editor.exact-meta-lore"))
                .build());

        inventory.setItem(48, new ItemBuilder(Material.EMERALD_BLOCK)
                .name(messages.componentWithoutPrefix("gui.editor.save-button"))
                .lore(messages.componentList("gui.editor.save-lore"))
                .build());

        inventory.setItem(50, new ItemBuilder(Material.REDSTONE_BLOCK)
                .name(messages.componentWithoutPrefix("gui.editor.cancel-button"))
                .lore(messages.componentList("gui.editor.cancel-lore"))
                .build());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void handleClick(InventoryClickEvent event, Player player) {
        int slot = event.getRawSlot();

        if (slot >= inventory.getSize()) {
            return;
        }

        if (ALLOWED_SLOTS.contains(slot)) {
            return;
        }

        event.setCancelled(true);

        switch (slot) {
            case 8 -> chatInput.requestInput(player, "prompt.enter-id", config.chatInputTimeoutSeconds(),
                    input -> {
                        String cleaned = input.toLowerCase().replaceAll("[^a-z0-9_-]", "");
                        if (!cleaned.isBlank()) {
                            this.id = cleaned;
                        }
                        player.openInventory(inventory);
                        updateButtons();
                    },
                    () -> player.openInventory(inventory)
            );
            case 17 -> chatInput.requestInput(player, "prompt.enter-permission", config.chatInputTimeoutSeconds(),
                    input -> {
                        this.permission = input.equalsIgnoreCase("none") ? null : input.trim();
                        player.openInventory(inventory);
                        updateButtons();
                    },
                    () -> player.openInventory(inventory)
            );
            case 26 -> chatInput.requestInput(player, "prompt.enter-level", config.chatInputTimeoutSeconds(),
                    input -> {
                        try {
                            this.levelCost = Math.max(0, Integer.parseInt(input.trim()));
                        } catch (NumberFormatException ignored) {
                            messages.send(player, "prompt.invalid-number");
                        }
                        player.openInventory(inventory);
                        updateButtons();
                    },
                    () -> player.openInventory(inventory)
            );
            case 35 -> chatInput.requestInput(player, "prompt.enter-money", config.chatInputTimeoutSeconds(),
                    input -> {
                        try {
                            this.moneyCost = Math.max(0.0, Double.parseDouble(input.trim().replace(',', '.')));
                        } catch (NumberFormatException ignored) {
                            messages.send(player, "prompt.invalid-number");
                        }
                        player.openInventory(inventory);
                        updateButtons();
                    },
                    () -> player.openInventory(inventory)
            );
            case 44 -> {
                this.exactMeta = !this.exactMeta;
                updateButtons();
            }
            case 48 -> saveRecipe(player);
            case 50 -> {
                returnPlacedItems(player);
                MainMenuHolder.open(player, messages, recipeEngine, chatInput, config);
            }
        }
    }

    private void saveRecipe(Player player) {
        ItemStack result = inventory.getItem(RESULT_SLOT);
        if (result == null || result.getType().isAir()) {
            messages.send(player, "notification.error-missing-result");
            return;
        }

        List<RecipeIngredient> list = new ArrayList<>();
        for (int slot : INGREDIENT_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && !item.getType().isAir()) {
                list.add(RecipeIngredient.of(item.clone(), exactMeta));
            }
        }

        if (list.isEmpty()) {
            messages.send(player, "notification.error-missing-ingredients");
            return;
        }

        RecipeCondition condition = new RecipeCondition(permission, levelCost, moneyCost, 200, 0.0f);
        CraftRecipe recipe = CraftRecipe.createShapeless(id, result.clone(), list, condition, exactMeta);

        recipeEngine.registerRecipe(recipe);
        messages.send(player, "notification.recipe-saved", Placeholder.parsed("id", id));
        returnPlacedItems(player);
        player.closeInventory();
    }

    public void returnPlacedItems(Player player) {
        for (int slot : ALLOWED_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && !item.getType().isAir()) {
                inventory.setItem(slot, null);
                player.getInventory().addItem(item).values().forEach(remaining ->
                        player.getWorld().dropItemNaturally(player.getLocation(), remaining)
                );
            }
        }
    }
}
