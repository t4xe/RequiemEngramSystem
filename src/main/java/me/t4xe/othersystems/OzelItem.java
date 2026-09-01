package me.t4xe.othersystems;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class OzelItem {

    public enum ItemType {
        REQUIEM_PARSOMENI("requiem_parsomeni", "arcane_scroll", "<#04BCBC>Requiem Parşömeni"),
        DILEK_TASI("dilek_tasi", "eye_of_the_future", "<#F4D03F>Dilek Taşı");

        private final String id;
        private final String oraxenId;
        private final String displayName;

        ItemType(String id, String oraxenId, String displayName) {
            this.id = id;
            this.oraxenId = oraxenId;
            this.displayName = displayName;
        }

        public String getId() { return id; }
        public String getOraxenId() { return oraxenId; }
        public String getDisplayName() { return displayName; }

        public static ItemType fromString(String input) {
            for (ItemType type : ItemType.values()) {
                if (type.name().equalsIgnoreCase(input) || type.id.equalsIgnoreCase(input)) {
                    return type;
                }
            }
            return null;
        }
    }

    public static ItemStack createItem(JavaPlugin plugin, ItemType type) {
        var oraxenItem = OraxenItems.getItemById(type.getOraxenId());

        if (oraxenItem == null) {
            plugin.getLogger().warning("Oraxen itemi bulunamadı: " + type.getOraxenId());
            return new ItemStack(Material.PAPER);
        }

        ItemStack item = oraxenItem.build();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        NamespacedKey key = new NamespacedKey(plugin, "ozel_item_type");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, type.getId());
        item.setItemMeta(meta);

        return item;
    }
}