package pluginsfix.glowcrafts.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import pluginsfix.glowcrafts.gui.holder.AnvilEditorHolder;
import pluginsfix.glowcrafts.gui.holder.FurnaceEditorHolder;
import pluginsfix.glowcrafts.gui.holder.GlowHolder;
import pluginsfix.glowcrafts.gui.holder.MainMenuHolder;
import pluginsfix.glowcrafts.gui.holder.RecipeListHolder;
import pluginsfix.glowcrafts.gui.holder.ShapedEditorHolder;
import pluginsfix.glowcrafts.gui.holder.ShapelessEditorHolder;

public final class GuiListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        Inventory topInv = event.getView().getTopInventory();
        if (!(topInv.getHolder() instanceof GlowHolder holder)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClick() == ClickType.DOUBLE_CLICK || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
            return;
        }

        if (holder instanceof MainMenuHolder || holder instanceof RecipeListHolder) {
            event.setCancelled(true);
            if (event.getRawSlot() < topInv.getSize()) {
                holder.handleClick(event, player);
            }
            return;
        }

        boolean isTopClicked = event.getClickedInventory() != null && event.getClickedInventory().equals(topInv);

        if (event.isShiftClick() && !isTopClicked) {
            event.setCancelled(true);
            return;
        }

        if (isTopClicked) {
            holder.handleClick(event, player);
        } else {
            if (event.getClick() == ClickType.NUMBER_KEY || event.getClick() == ClickType.SWAP_OFFHAND) {
                int hotbarSlot = event.getHotbarButton();
                if (hotbarSlot >= 0 && hotbarSlot < 9) {
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        Inventory topInv = event.getView().getTopInventory();
        if (!(topInv.getHolder() instanceof GlowHolder holder)) {
            return;
        }

        int topSize = topInv.getSize();

        if (holder instanceof MainMenuHolder || holder instanceof RecipeListHolder) {
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot < topSize) {
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }

        if (holder instanceof ShapedEditorHolder editor) {
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot < topSize && !ShapedEditorHolder.isAllowedSlot(rawSlot)) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (holder instanceof ShapelessEditorHolder editor) {
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot < topSize && !ShapelessEditorHolder.isAllowedSlot(rawSlot)) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (holder instanceof AnvilEditorHolder editor) {
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot < topSize && !AnvilEditorHolder.isAllowedSlot(rawSlot)) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (holder instanceof FurnaceEditorHolder editor) {
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot < topSize && !FurnaceEditorHolder.isAllowedSlot(rawSlot)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        returnHolderItems(inv.getHolder(), player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Inventory open = player.getOpenInventory().getTopInventory();
        returnHolderItems(open.getHolder(), player);
    }

    private void returnHolderItems(Object holder, Player player) {
        if (holder instanceof ShapedEditorHolder editor) {
            editor.returnPlacedItems(player);
        } else if (holder instanceof ShapelessEditorHolder editor) {
            editor.returnPlacedItems(player);
        } else if (holder instanceof AnvilEditorHolder editor) {
            editor.returnPlacedItems(player);
        } else if (holder instanceof FurnaceEditorHolder editor) {
            editor.returnPlacedItems(player);
        }
    }
}
