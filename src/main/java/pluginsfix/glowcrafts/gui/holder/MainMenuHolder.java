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
import pluginsfix.glowcrafts.gui.ItemBuilder;
import pluginsfix.glowcrafts.gui.prompt.ChatInputHandler;
import pluginsfix.glowcrafts.recipe.RecipeEngine;
import pluginsfix.glowcrafts.text.Messages;

public final class MainMenuHolder implements GlowHolder {

    private final Messages messages;
    private final RecipeEngine recipeEngine;
    private final ChatInputHandler chatInput;
    private final PluginConfig config;
    private Inventory inventory;

    public MainMenuHolder(Messages messages, RecipeEngine recipeEngine, ChatInputHandler chatInput, PluginConfig config) {
        this.messages = messages;
        this.recipeEngine = recipeEngine;
        this.chatInput = chatInput;
        this.config = config;
    }

    public static void open(Player player, Messages messages, RecipeEngine recipeEngine, ChatInputHandler chatInput, PluginConfig config) {
        MainMenuHolder holder = new MainMenuHolder(messages, recipeEngine, chatInput, config);
        Inventory inv = Bukkit.createInventory(holder, 45, messages.componentWithoutPrefix("gui.main.title"));
        holder.inventory = inv;
        holder.render();
        player.openInventory(inv);
    }

    private void render() {
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).build();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        inventory.setItem(11, new ItemBuilder(Material.BOOK)
                .name(messages.componentWithoutPrefix("gui.main.list-button"))
                .lore(messages.componentList("gui.main.list-lore"))
                .build());

        inventory.setItem(13, new ItemBuilder(Material.CRAFTING_TABLE)
                .name(messages.componentWithoutPrefix("gui.main.create-shaped-button"))
                .lore(messages.componentList("gui.main.create-shaped-lore"))
                .build());

        inventory.setItem(15, new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .name(messages.componentWithoutPrefix("gui.main.create-shapeless-button"))
                .lore(messages.componentList("gui.main.create-shapeless-lore"))
                .build());

        inventory.setItem(29, new ItemBuilder(Material.ANVIL)
                .name(messages.componentWithoutPrefix("gui.main.create-anvil-button"))
                .lore(messages.componentList("gui.main.create-anvil-lore"))
                .build());

        inventory.setItem(31, new ItemBuilder(Material.FURNACE)
                .name(messages.componentWithoutPrefix("gui.main.create-furnace-button"))
                .lore(messages.componentList("gui.main.create-furnace-lore"))
                .build());

        inventory.setItem(33, new ItemBuilder(Material.REDSTONE_TORCH)
                .name(messages.componentWithoutPrefix("gui.main.reload-button"))
                .lore(messages.componentList("gui.main.reload-lore"))
                .build());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void handleClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        switch (slot) {
            case 11 -> RecipeListHolder.open(player, messages, recipeEngine, chatInput, config, 1, null);
            case 13 -> ShapedEditorHolder.openNew(player, messages, recipeEngine, chatInput, config);
            case 15 -> ShapelessEditorHolder.openNew(player, messages, recipeEngine, chatInput, config);
            case 29 -> AnvilEditorHolder.openNew(player, messages, recipeEngine, chatInput, config);
            case 31 -> FurnaceEditorHolder.openNew(player, messages, recipeEngine, chatInput, config);
            case 33 -> {
                recipeEngine.loadAndRegisterAll();
                messages.send(player, "command.reloaded", Placeholder.parsed("count", String.valueOf(recipeEngine.getAll().size())));
                player.closeInventory();
            }
        }
    }
}
