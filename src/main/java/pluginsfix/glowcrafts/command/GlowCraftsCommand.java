package pluginsfix.glowcrafts.command;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pluginsfix.glowcrafts.config.PluginConfig;
import pluginsfix.glowcrafts.gui.holder.AnvilEditorHolder;
import pluginsfix.glowcrafts.gui.holder.FurnaceEditorHolder;
import pluginsfix.glowcrafts.gui.holder.MainMenuHolder;
import pluginsfix.glowcrafts.gui.holder.RecipeListHolder;
import pluginsfix.glowcrafts.gui.holder.ShapedEditorHolder;
import pluginsfix.glowcrafts.gui.holder.ShapelessEditorHolder;
import pluginsfix.glowcrafts.gui.prompt.ChatInputHandler;
import pluginsfix.glowcrafts.recipe.RecipeEngine;
import pluginsfix.glowcrafts.text.Messages;

public final class GlowCraftsCommand implements CommandExecutor {

    private final Messages messages;
    private final RecipeEngine recipeEngine;
    private final ChatInputHandler chatInput;
    private final PluginConfig config;

    public GlowCraftsCommand(Messages messages, RecipeEngine recipeEngine, ChatInputHandler chatInput, PluginConfig config) {
        this.messages = messages;
        this.recipeEngine = recipeEngine;
        this.chatInput = chatInput;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("glowcrafts.admin")) {
            messages.send(sender, "command.no-permission");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "command.players-only");
                return true;
            }
            MainMenuHolder.open(player, messages, recipeEngine, chatInput, config);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create" -> handleCreate(sender, args);
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            default -> messages.send(sender, "command.usage");
        }

        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "command.players-only");
            return;
        }

        if (!player.hasPermission("glowcrafts.admin.create")) {
            messages.send(player, "command.no-permission");
            return;
        }

        if (args.length < 2) {
            messages.send(player, "command.create-usage");
            return;
        }

        String type = args[1].toLowerCase();
        switch (type) {
            case "shaped" -> ShapedEditorHolder.openNew(player, messages, recipeEngine, chatInput, config);
            case "shapeless" -> ShapelessEditorHolder.openNew(player, messages, recipeEngine, chatInput, config);
            case "anvil" -> AnvilEditorHolder.openNew(player, messages, recipeEngine, chatInput, config);
            case "furnace" -> FurnaceEditorHolder.openNew(player, messages, recipeEngine, chatInput, config);
            default -> messages.send(player, "command.unknown-type");
        }
    }

    private void handleList(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "command.players-only");
            return;
        }

        if (!player.hasPermission("glowcrafts.admin.list")) {
            messages.send(player, "command.no-permission");
            return;
        }

        RecipeListHolder.open(player, messages, recipeEngine, chatInput, config, 1, null);
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("glowcrafts.admin.reload")) {
            messages.send(sender, "command.no-permission");
            return;
        }

        recipeEngine.loadAndRegisterAll();
        messages.send(sender, "command.reloaded", Placeholder.parsed("count", String.valueOf(recipeEngine.getAll().size())));
    }
}
