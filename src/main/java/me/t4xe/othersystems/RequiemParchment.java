package me.t4xe.othersystems;

import io.th0rgal.oraxen.api.OraxenItems;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RequiemParchment implements Listener {

    private static final String ITEM_ID = "arcane_scroll";
    private static final long COOLDOWN = 5 * 60 * 1000;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @EventHandler
    public void onUse(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        if (!ITEM_ID.equals(OraxenItems.getIdByItem(item))) return;

        Player player = event.getPlayer();
        event.setCancelled(true);

        if (player.hasPotionEffect(PotionEffectType.HASTE)) {
            player.sendMessage(mm.deserialize(
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Zaten üzerinde efekt mevcut!"
            ));
            return;
        }

        long now = System.currentTimeMillis();
        long lastUse = cooldowns.getOrDefault(player.getUniqueId(), 0L);

        long remaining = COOLDOWN - (now - lastUse);
        if (remaining > 0) {
            long seconds = remaining / 1000;
            long minutes = seconds / 60;
            seconds %= 60;

            player.sendMessage(mm.deserialize(
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Yeniden kullanmak için <#248673>" + minutes
                            + " <#3ACBAE>dakika <#248673>" + seconds + " <#3ACBAE>saniye beklemelisin."
            ));
            return;
        }

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.HASTE,
                5 * 60 * 20,
                0
        ));

        item.setAmount(item.getAmount() - 1);

        cooldowns.put(player.getUniqueId(), now);

        player.sendMessage(mm.deserialize(
                "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Parşömenin sana verdiği gücü hissediyorsun..."
        ));
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
    }
}