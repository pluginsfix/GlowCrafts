package pluginsfix.glowcrafts.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class ItemBuilder {

    private final ItemStack item;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material != null ? material : Material.STONE);
    }

    public ItemBuilder(ItemStack item) {
        this.item = item != null ? item.clone() : new ItemStack(Material.STONE);
    }

    public ItemBuilder name(Component name) {
        if (name != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(name);
                item.setItemMeta(meta);
            }
        }
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        if (lore != null && !lore.isEmpty()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.lore(lore);
                item.setItemMeta(meta);
            }
        }
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(Math.max(1, amount));
        return this;
    }

    public ItemBuilder glow(boolean glow) {
        if (glow) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
            }
        }
        return this;
    }

    public ItemStack build() {
        return item.clone();
    }
}
