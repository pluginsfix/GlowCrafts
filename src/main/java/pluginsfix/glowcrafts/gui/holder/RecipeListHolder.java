package pluginsfix.glowcrafts.gui.holder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pluginsfix.glowcrafts.config.PluginConfig;
import pluginsfix.glowcrafts.domain.CraftRecipe;
import pluginsfix.glowcrafts.domain.RecipeType;
import pluginsfix.glowcrafts.gui.ItemBuilder;
import pluginsfix.glowcrafts.gui.prompt.ChatInputHandler;
import pluginsfix.glowcrafts.recipe.RecipeEngine;
import pluginsfix.glowcrafts.text.Messages;

import java.util.ArrayList;
import java.util.List;

public final class RecipeListHolder implements GlowHolder {

    private final Messages messages;
    private final RecipeEngine recipeEngine;
    private final ChatInputHandler chatInput;
    private final PluginConfig config;
    private final int page;
    private final RecipeType filter;
    private Inventory inventory;
    private final List<CraftRecipe> displayedRecipes = new ArrayList<>();

    public RecipeListHolder(Messages messages, RecipeEngine recipeEngine, ChatInputHandler chatInput, PluginConfig config, int page, RecipeType filter) {
        this.messages = messages;
        this.recipeEngine = recipeEngine;
        this.chatInput = chatInput;
        this.config = config;
        this.page = Math.max(1, page);
        this.filter = filter;
    }

    public static void open(Player player, Messages messages, RecipeEngine recipeEngine, ChatInputHandler chatInput, PluginConfig config, int page, RecipeType filter) {
        RecipeListHolder holder = new RecipeListHolder(messages, recipeEngine, chatInput, config, page, filter);

        List<CraftRecipe> all = new ArrayList<>();
        for (CraftRecipe recipe : recipeEngine.getAll()) {
            if (filter == null || recipe.type() == filter) {
                all.add(recipe);
            }
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) all.size() / 36.0));
        int safePage = Math.min(holder.page, totalPages);

        Component title = messages.componentWithoutPrefix("gui.list.title",
                Placeholder.parsed("page", String.valueOf(safePage)),
                Placeholder.parsed("total_pages", String.valueOf(totalPages))
        );

        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.inventory = inv;
        holder.render(all, safePage, totalPages);
        player.openInventory(inv);
    }

    private void render(List<CraftRecipe> all, int currentPage, int totalPages) {
        ItemStack bottomBarFiller = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(Component.empty()).build();
        for (int i = 36; i < 54; i++) {
            inventory.setItem(i, bottomBarFiller);
        }

        int startIndex = (currentPage - 1) * 36;
        int endIndex = Math.min(startIndex + 36, all.size());
        displayedRecipes.clear();

        for (int i = startIndex; i < endIndex; i++) {
            CraftRecipe recipe = all.get(i);
            displayedRecipes.add(recipe);

            ItemStack icon = recipe.result().clone();
            ItemMeta meta = icon.getItemMeta();
            List<Component> currentLore = meta != null && meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();

            String permText = recipe.condition().hasPermission() ? recipe.condition().permission() : messages.raw("common.none");
            String levelText = String.valueOf(recipe.condition().levelCost());
            String moneyText = String.valueOf(recipe.condition().moneyCost());
            String exactText = recipe.exactMeta() ? messages.raw("common.yes") : messages.raw("common.no");

            List<Component> extraLore = messages.componentList("gui.list.item-lore",
                    Placeholder.parsed("type", recipe.type().name()),
                    Placeholder.parsed("id", recipe.id()),
                    Placeholder.parsed("permission", permText),
                    Placeholder.parsed("level", levelText),
                    Placeholder.parsed("money", moneyText),
                    Placeholder.parsed("exact_meta", exactText)
            );

            currentLore.addAll(extraLore);
            if (meta != null) {
                meta.lore(currentLore);
                icon.setItemMeta(meta);
            }

            int slot = i - startIndex;
            inventory.setItem(slot, icon);
        }

        inventory.setItem(45, new ItemBuilder(Material.HOPPER)
                .name(messages.componentWithoutPrefix(getFilterMessageKey()))
                .build());

        if (currentPage > 1) {
            inventory.setItem(48, new ItemBuilder(Material.ARROW)
                    .name(messages.componentWithoutPrefix("gui.list.prev-page"))
                    .build());
        }

        inventory.setItem(49, new ItemBuilder(Material.BARRIER)
                .name(messages.componentWithoutPrefix("gui.list.back-button"))
                .build());

        if (currentPage < totalPages) {
            inventory.setItem(50, new ItemBuilder(Material.ARROW)
                    .name(messages.componentWithoutPrefix("gui.list.next-page"))
                    .build());
        }
    }

    private String getFilterMessageKey() {
        if (filter == null) {
            return "gui.list.filter-all";
        }
        return switch (filter) {
            case SHAPED, SHAPELESS -> "gui.list.filter-crafting";
            case ANVIL -> "gui.list.filter-anvil";
            case FURNACE, BLAST_FURNACE, SMOKER -> "gui.list.filter-furnace";
        };
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void handleClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot >= 0 && slot < 36) {
            if (slot >= displayedRecipes.size()) {
                return;
            }

            CraftRecipe recipe = displayedRecipes.get(slot);
            if (event.getClick() == ClickType.SHIFT_RIGHT) {
                recipeEngine.deleteRecipe(recipe.id());
                messages.send(player, "notification.recipe-deleted", Placeholder.parsed("id", recipe.id()));
                open(player, messages, recipeEngine, chatInput, config, page, filter);
                return;
            }

            if (event.isLeftClick()) {
                switch (recipe.type()) {
                    case SHAPED -> ShapedEditorHolder.openEdit(player, messages, recipeEngine, chatInput, config, recipe);
                    case SHAPELESS -> ShapelessEditorHolder.openEdit(player, messages, recipeEngine, chatInput, config, recipe);
                    case ANVIL -> AnvilEditorHolder.openEdit(player, messages, recipeEngine, chatInput, config, recipe);
                    case FURNACE, BLAST_FURNACE, SMOKER -> FurnaceEditorHolder.openEdit(player, messages, recipeEngine, chatInput, config, recipe);
                }
            }
            return;
        }

        switch (slot) {
            case 45 -> {
                RecipeType nextFilter = getNextFilter(filter);
                open(player, messages, recipeEngine, chatInput, config, 1, nextFilter);
            }
            case 48 -> {
                if (page > 1) {
                    open(player, messages, recipeEngine, chatInput, config, page - 1, filter);
                }
            }
            case 49 -> MainMenuHolder.open(player, messages, recipeEngine, chatInput, config);
            case 50 -> open(player, messages, recipeEngine, chatInput, config, page + 1, filter);
        }
    }

    private RecipeType getNextFilter(RecipeType current) {
        if (current == null) {
            return RecipeType.SHAPED;
        }
        return switch (current) {
            case SHAPED -> RecipeType.ANVIL;
            case ANVIL -> RecipeType.FURNACE;
            case FURNACE, BLAST_FURNACE, SMOKER -> null;
            case SHAPELESS -> RecipeType.ANVIL;
        };
    }
}
