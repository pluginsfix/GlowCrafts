package pluginsfix.glowcrafts;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import pluginsfix.glowcrafts.command.GlowCraftsCommand;
import pluginsfix.glowcrafts.command.GlowCraftsTabCompleter;
import pluginsfix.glowcrafts.config.PluginConfig;
import pluginsfix.glowcrafts.gui.prompt.ChatInputHandler;
import pluginsfix.glowcrafts.hook.VaultEconomyHook;
import pluginsfix.glowcrafts.listener.AnvilListener;
import pluginsfix.glowcrafts.listener.CraftListener;
import pluginsfix.glowcrafts.listener.GuiListener;
import pluginsfix.glowcrafts.recipe.BukkitRecipeRegistrar;
import pluginsfix.glowcrafts.recipe.RecipeEngine;
import pluginsfix.glowcrafts.storage.YamlRecipeStorage;
import pluginsfix.glowcrafts.text.Messages;

import java.io.File;

public final class GlowCrafts extends JavaPlugin {

    private RecipeEngine recipeEngine;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfNotExists("messages.yml");

        PluginConfig config = PluginConfig.fromYaml(getConfig());
        Messages messages = Messages.load(new File(getDataFolder(), "messages.yml"));

        VaultEconomyHook economy = VaultEconomyHook.create();
        BukkitRecipeRegistrar registrar = new BukkitRecipeRegistrar(this, getLogger());
        YamlRecipeStorage storage = new YamlRecipeStorage(getDataFolder(), getLogger());

        this.recipeEngine = new RecipeEngine(this, storage, registrar);
        this.recipeEngine.loadAndRegisterAll();

        ChatInputHandler chatInput = new ChatInputHandler(this, messages);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new GuiListener(), this);
        pm.registerEvents(chatInput, this);
        pm.registerEvents(new CraftListener(recipeEngine, economy, messages), this);
        pm.registerEvents(new AnvilListener(recipeEngine, economy, messages), this);

        GlowCraftsCommand executor = new GlowCraftsCommand(messages, recipeEngine, chatInput, config);
        GlowCraftsTabCompleter completer = new GlowCraftsTabCompleter();

        PluginCommand command = getCommand("glowcrafts");
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(completer);
        }
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    private void saveResourceIfNotExists(String resourcePath) {
        File file = new File(getDataFolder(), resourcePath);
        if (!file.exists()) {
            saveResource(resourcePath, false);
        }
    }
}
