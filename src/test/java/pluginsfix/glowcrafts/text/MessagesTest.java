package pluginsfix.glowcrafts.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MessagesTest {

    @Test
    void loadAndFormatMessages() throws Exception {
        File messagesFile = new File("src/main/resources/messages.yml");
        assertThat(messagesFile.exists()).isTrue();

        YamlConfiguration yaml = new YamlConfiguration();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(messagesFile), StandardCharsets.UTF_8)) {
            yaml.load(reader);
        }

        assertThat(yaml.getString("prefix")).isNotEmpty();
        assertThat(yaml.getString("gui.main.title")).isNotEmpty();
        assertThat(yaml.getString("gui.main.list-button")).isNotEmpty();
        assertThat(yaml.getString("command.no-permission")).isNotEmpty();
    }
}
