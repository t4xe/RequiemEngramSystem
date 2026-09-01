package me.t4xe.engram;

import me.t4xe.ViridyaSistemleri;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Random;

public class EngramVerme implements Listener {

    private final JavaPlugin plugin;
    private final Random random = new Random();
    private final MiniMessage mm = MiniMessage.miniMessage();

    private static final String WORLD_NAME = "Viridya";

    public EngramVerme(ViridyaSistemleri plugin) {
        this.plugin = plugin;
    }

    // -------------------- GÜVENLİ ÖDÜL SİSTEMİ --------------------

    private void tryGive(Player player, EngramItem.EngramType type, double chance, String message)
    {
        if (player == null) return;
        if (!isViridya(player)) return;

        chance = clampChance(chance);
        if (chance <= 0.0) return;

        chance = applyPermissionBoost(player, chance);

        if (random.nextDouble() > chance) return;

        ItemStack item = EngramItem.createEngram(plugin, type);
        if (item == null || item.getType() == Material.AIR) return;

        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);

        if (!leftover.isEmpty()) {
            leftover.values().forEach(i ->
                    player.getWorld().dropItemNaturally(player.getLocation(), i)
            );
        }

        if (message != null && !message.isEmpty()) {
            player.sendMessage(mm.deserialize(message));
        }
    }

    private void tryGiveEssence(Player player) {
        if (player == null || !isViridya(player)) return;

        double chance = plugin.getConfig().getDouble("essence.base-chance", 0.0);
        int baseAmount = plugin.getConfig().getInt("essence.amount", 0);

        chance = clampChance(chance);

        double chanceMultiplier = getHighestPermissionMultiplier(player, "essence-boosts");
        chance = clampChance(chance * chanceMultiplier);

        if (random.nextDouble() > chance) return;

        //int finalAmount = (int) Math.round(baseAmount * chanceMultiplier);

        String command = "esans give " + player.getName() + " " + baseAmount;
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
    }

    private double getHighestPermissionMultiplier(Player player, String path) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        if (section == null) return 1.0;

        double highest = 1.0;

        for (String perm : section.getKeys(false)) {
            if (player.hasPermission(perm)) {
                double value = section.getDouble(perm);

                if (value > highest) {
                    highest = value;
                }
            }
        }

        return highest;
    }

    private double applyPermissionBoost(Player player, double baseChance) {
        double multiplier = getHighestPermissionMultiplier(player, "engram-boosts");
        return clampChance(baseChance * multiplier);
    }

    // -------------------- EVENTS --------------------

    @EventHandler
    public void onCropBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!isViridya(player)) return;

        if (!(block.getBlockData() instanceof Ageable ageable)) return;
        if (ageable.getAge() != ageable.getMaximumAge()) return;
        if (!isCrop(block.getType())) return;

        tryGive(
                player,
                EngramItem.EngramType.PIADO,
                getChance("engram-chance.piado"),
                "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Ekinlerden <#6DA964>Piado Engramı <#3ACBAE>buldun!"
        );
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null || !isViridya(player)) return;

        EntityType type = event.getEntity().getType();

        if (isHostileMob(type)) {
            tryGive(
                    player,
                    EngramItem.EngramType.ROGUE,
                    getChance("engram-chance.rogue"),
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Canavardan <#9F5031>Rogue Engramı <#3ACBAE>buldun!"
            );

            tryGiveEssence(player);
        }

        if (isFarmAnimal(type)) {
            tryGive(
                    player,
                    EngramItem.EngramType.DELMAS,
                    getChance("engram-chance.delmas"),
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Hayvandan <#46D5DB>Delmas Engramı <#3ACBAE>buldun!"
            );

            tryGiveEssence(player);
        }
    }

    @EventHandler
    public void onMine(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!isViridya(player)) return;
        if (!isMiningBlock(event.getBlock().getType())) return;

        tryGive(
                player,
                EngramItem.EngramType.REQUIEM,
                getChance("engram-chance.requiem"),
                "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Maden kazarken <#04BCBC>Requiem Engramı <#3ACBAE>buldun!"
        );
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Player player = event.getPlayer();
        if (!isViridya(player)) return;

        tryGive(
                player,
                EngramItem.EngramType.ULTIMATUM,
                getChance("engram-chance.ultimatum"),
                "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Balık tutarken <#99359F>Ultimatum Engramı <#3ACBAE>buldun!"
        );
    }

    // -------------------- GÜVENLİK HELPERLARI --------------------

    private boolean isViridya(Player player) {
        return player != null && WORLD_NAME.equalsIgnoreCase(player.getWorld().getName());
    }

    private double getChance(String path) {
        return plugin.getConfig().getDouble(path, 0.0);
    }

    private double clampChance(double chance) {
        if (Double.isNaN(chance)) return 0.0;
        return Math.max(0.0, Math.min(1.0, chance));
    }

    // -------------------- TYPE CHECKLERİ --------------------

    private boolean isCrop(Material material) {
        return material == Material.WHEAT ||
                material == Material.CARROTS ||
                material == Material.POTATOES ||
                material == Material.BEETROOTS ||
                material == Material.NETHER_WART ||
                material == Material.COCOA;
    }

    private boolean isHostileMob(EntityType type) {
        return type == EntityType.ZOMBIE
                || type == EntityType.SKELETON
                || type == EntityType.SPIDER
                || type == EntityType.CAVE_SPIDER
                || type == EntityType.CREEPER
                || type == EntityType.ENDERMAN
                || type == EntityType.BLAZE
                || type == EntityType.PILLAGER
                || type == EntityType.EVOKER
                || type == EntityType.DROWNED
                || type == EntityType.MAGMA_CUBE
                || type == EntityType.ILLUSIONER
                || type == EntityType.RAVAGER
                || type == EntityType.SLIME;
    }

    private boolean isFarmAnimal(EntityType type) {
        return type == EntityType.PIG
                || type == EntityType.SHEEP
                || type == EntityType.CHICKEN
                || type == EntityType.COW
                || type == EntityType.RABBIT;
    }

    private boolean isMiningBlock(Material material) {
        return material == Material.IRON_ORE
                || material == Material.DEEPSLATE_IRON_ORE
                || material == Material.GOLD_ORE
                || material == Material.DEEPSLATE_GOLD_ORE
                || material == Material.DIAMOND_ORE
                || material == Material.DEEPSLATE_DIAMOND_ORE
                || material == Material.EMERALD_ORE
                || material == Material.DEEPSLATE_EMERALD_ORE
                || material == Material.STONE
                || material == Material.DEEPSLATE
                || material == Material.ANDESITE
                || material == Material.DIORITE
                || material == Material.GRANITE;
    }
}