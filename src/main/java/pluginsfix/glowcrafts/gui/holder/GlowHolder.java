package pluginsfix.glowcrafts.gui.holder;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface GlowHolder extends InventoryHolder {

    void handleClick(InventoryClickEvent event, Player player);
}
