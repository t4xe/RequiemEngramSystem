package me.t4xe.engram;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class EngramItem {
    private static final Map<EngramType, String> DISPLAY_NAME_CACHE = new HashMap<>();
    public enum EngramType {
        PIADO("piado_engram", "viridyaitem15"),
        DELMAS("delmas_engram", "viridyaitem16"),
        ROGUE("rogue_engram", "viridyaitem10"),
        ULTIMATUM("ultimatum_engram", "viridyaitem13"),
        VIRIDYA("viridya_engram", "viridyaitem2"),
        REQUIEM("requiem_engram", "viridyaitem5"),
        DILEK_TASI("dilek_tasi", "eye_of_the_future");

        private final String id;
        private final String oraxenId;

        EngramType(String id, String oraxenId) {
            this.id = id;
            this.oraxenId = oraxenId;
        }

        public String getId() {
            return id;
        }
        public String getOraxenId() {
            return oraxenId;
        }

        public String getCompactName() {
            return switch (this) {
                case PIADO -> "<#6DA964>Piado Engramı";
                case DELMAS -> "<#46D5DB>Delmas Engramı";
                case ROGUE -> "<#9F5031>Rogue Engramı";
                case ULTIMATUM -> "<#99359F>Ultimatum Engramı";
                case VIRIDYA -> "<#aacb61>Viridya Engramı";
                case REQUIEM -> "<#04BCBC>Requiem Engramı";
                case DILEK_TASI -> "<#6CD65C>Dilek Taşı";
            };
        }

        public static EngramType fromString(String input) {
            for (EngramType type : EngramType.values()) {
                if (type.name().equalsIgnoreCase(input) ||
                        type.id.equalsIgnoreCase(input)) {
                    return type;
                }
            }
            return null;
        }
    }

    public static ItemStack createEngram(JavaPlugin plugin, EngramType type) {

        var oraxenItem = OraxenItems.getItemById(type.getOraxenId());

        if (oraxenItem == null) {
            plugin.getLogger().warning("Oraxen itemi bulunamadı: " + type.getOraxenId());
            return new ItemStack(org.bukkit.Material.STONE);
        }

        ItemStack item = oraxenItem.build();
        if (item == null) {
            return new ItemStack(org.bukkit.Material.STONE);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        NamespacedKey key = new NamespacedKey(plugin, "engram_type");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, type.getId());
        item.setItemMeta(meta);

        return item;
    }
}