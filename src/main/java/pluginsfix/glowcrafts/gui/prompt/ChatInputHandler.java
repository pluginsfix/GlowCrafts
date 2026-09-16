package pluginsfix.glowcrafts.gui.prompt;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import pluginsfix.glowcrafts.text.Messages;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class ChatInputHandler implements Listener {

    private final Plugin plugin;
    private final Messages messages;
    private final Map<UUID, PendingPrompt> pending = new ConcurrentHashMap<>();

    public record PendingPrompt(
            Consumer<String> onInput,
            Runnable onCancel,
            long expireTimeMillis
    ) {}

    public ChatInputHandler(Plugin plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    public void requestInput(Player player, String promptKey, int timeoutSeconds, Consumer<String> onInput, Runnable onCancel) {
        UUID uuid = player.getUniqueId();
        long expire = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        pending.put(uuid, new PendingPrompt(onInput, onCancel, expire));

        player.closeInventory();
        messages.send(player, promptKey);
    }

    public void cancel(Player player) {
        PendingPrompt prompt = pending.remove(player.getUniqueId());
        if (prompt != null && prompt.onCancel() != null) {
            Bukkit.getScheduler().runTask(plugin, prompt.onCancel());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PendingPrompt prompt = pending.get(uuid);
        if (prompt == null) {
            return;
        }

        event.setCancelled(true);
        pending.remove(uuid);

        if (System.currentTimeMillis() > prompt.expireTimeMillis()) {
            messages.send(player, "prompt.timeout");
            if (prompt.onCancel() != null) {
                Bukkit.getScheduler().runTask(plugin, prompt.onCancel());
            }
            return;
        }

        String text = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        if (text.equalsIgnoreCase("cancel")) {
            messages.send(player, "prompt.cancelled");
            if (prompt.onCancel() != null) {
                Bukkit.getScheduler().runTask(plugin, prompt.onCancel());
            }
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> prompt.onInput().accept(text));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pending.remove(event.getPlayer().getUniqueId());
    }
}
