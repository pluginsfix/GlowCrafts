package pluginsfix.glowcrafts.config;

import org.bukkit.configuration.file.FileConfiguration;

public record PluginConfig(
        boolean vaultEnabled,
        boolean debug,
        int chatInputTimeoutSeconds,
        int defaultGuiRows
) {
    public static PluginConfig fromYaml(FileConfiguration config) {
        boolean vault = config.getBoolean("general.enable-vault", true);
        boolean debug = config.getBoolean("general.debug", false);
        int timeout = config.getInt("general.chat-input-timeout-seconds", 45);
        int rows = config.getInt("gui.default-rows", 6);
        return new PluginConfig(vault, debug, timeout, rows);
    }
}
