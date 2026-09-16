package pluginsfix.glowcrafts.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class MessagesTest {

    @Test
    void loadAndFormatMessages() {
        File messagesFile = new File("src/main/resources/messages.yml");
        Messages messages = Messages.load(messagesFile);

        assertThat(messages.raw("prefix")).isNotEmpty();
        assertThat(messages.raw("command.no-permission")).isNotEmpty();

        Component component = messages.component("command.reloaded", Placeholder.parsed("count", "5"));
        assertThat(component).isNotNull();
    }
}
