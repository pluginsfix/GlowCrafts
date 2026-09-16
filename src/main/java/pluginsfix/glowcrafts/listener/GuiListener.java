package pluginsfix.glowcrafts.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import pluginsfix.glowcrafts.gui.holder.AnvilEditorHolder;
import pluginsfix.glowcrafts.gui.holder.FurnaceEditorHolder;
import pluginsfix.glowcrafts.gui.holder.GlowHolder;
import pluginsfix.glowcrafts.gui.holder.ShapedEditorHolder;
import pluginsfix.glowcrafts.gui.holder.ShapelessEditorHolder;

public final class GuiListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof GlowHolder holder)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        holder.handleClick(event, player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof GlowHolder)) {
            return;
        }

        int topSize = inv.getSize();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topSize) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (inv.getHolder() instanceof ShapedEditorHolder holder) {
            holder.returnPlacedItems(player);
        } else if (inv.getHolder() instanceof ShapelessEditorHolder holder) {
            holder.returnPlacedItems(player);
        } else if (inv.getHolder() instanceof AnvilEditorHolder holder) {
            holder.returnPlacedItems(player);
        } else if (inv.getHolder() instanceof FurnaceEditorHolder holder) {
            holder.returnPlacedItems(player);
        }
    }
}
