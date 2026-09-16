package pluginsfix.glowcrafts.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Messages {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, String> messages = new HashMap<>();
    private final Map<String, List<String>> messageLists = new HashMap<>();
    private String prefix = "";

    public static Messages load(JavaPlugin plugin) {
        Messages instance = new Messages();

        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            try (Reader reader = new InputStreamReader(defaultStream, StandardCharsets.UTF_8)) {
                YamlConfiguration defaultYaml = YamlConfiguration.loadConfiguration(reader);
                instance.loadFromYaml(defaultYaml);
            } catch (Exception ignored) {
            }
        }

        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (file.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                YamlConfiguration diskYaml = YamlConfiguration.loadConfiguration(reader);
                instance.loadFromYaml(diskYaml);
            } catch (Exception ignored) {
            }
        }

        instance.prefix = instance.messages.getOrDefault("prefix", "");
        return instance;
    }

    private void loadFromYaml(YamlConfiguration yaml) {
        for (String key : yaml.getKeys(true)) {
            if (yaml.isString(key)) {
                messages.put(key, yaml.getString(key));
            } else if (yaml.isList(key)) {
                messageLists.put(key, yaml.getStringList(key));
            }
        }
    }

    public String raw(String key) {
        return messages.getOrDefault(key, key);
    }

    public Component component(String key, TagResolver... resolvers) {
        String template = messages.getOrDefault(key, key);
        return miniMessage.deserialize(prefix + template, resolvers);
    }

    public Component componentWithoutPrefix(String key, TagResolver... resolvers) {
        String template = messages.getOrDefault(key, key);
        return miniMessage.deserialize(template, resolvers);
    }

    public List<Component> componentList(String key, TagResolver... resolvers) {
        List<String> list = messageLists.get(key);
        if (list == null) {
            return Collections.emptyList();
        }

        List<Component> components = new ArrayList<>(list.size());
        for (String line : list) {
            components.add(miniMessage.deserialize(line, resolvers));
        }
        return components;
    }

    public void send(CommandSender sender, String key, TagResolver... resolvers) {
        if (sender == null) {
            return;
        }
        sender.sendMessage(component(key, resolvers));
    }

    public void sendWithoutPrefix(CommandSender sender, String key, TagResolver... resolvers) {
        if (sender == null) {
            return;
        }
        sender.sendMessage(componentWithoutPrefix(key, resolvers));
    }
}
