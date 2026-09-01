package me.t4xe.engram.reward;

import me.t4xe.engram.EngramItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class EngramRewardHandler {
    private final Map<EngramItem.EngramType, List<RewardHelper>> rewardMap = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    public EngramRewardHandler(JavaPlugin plugin) {
        reload(plugin);
    }

    private Map<EngramItem.EngramType, List<RewardHelper>> loadRewards(JavaPlugin plugin) {
        Map<EngramItem.EngramType, List<RewardHelper>> map = new HashMap<>();

        for (EngramItem.EngramType type : EngramItem.EngramType.values()) {

            String path = "engram-rewards." + type.name().toLowerCase();
            var section = plugin.getConfig().getConfigurationSection(path);

            if (section == null) {
                plugin.getLogger().warning("Geçersiz ödüllendirme kısmı: " + path);
                continue;
            }

            List<RewardHelper> rewards = new ArrayList<>();

            for (String key : section.getKeys(false)) {
                double chance = section.getDouble(key + ".chance");
                String command = section.getString(key + ".command");

                if (command == null) continue;

                String message = section.getString(key + ".message");

                rewards.add(new RewardHelper(chance, command, message));
            }

            map.put(type, rewards);
        }

        return map;
    }

    public void handleReward(EngramItem.EngramType type, Player player) {
        List<RewardHelper> rewards = rewardMap.get(type);

        if (rewards == null || rewards.isEmpty()) return;

        double totalWeight = rewards.stream()
                .mapToDouble(RewardHelper::getChance)
                .sum();

        double roll = Math.random() * totalWeight;
        double current = 0;

        for (RewardHelper reward : rewards) {
            current += reward.getChance();
            if (roll <= current) {
                executeReward(reward, player);
                return;
            }
        }
    }

    private void executeReward(RewardHelper reward, Player player) {
        String cmd = reward.getCommand().replace("%player%", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);

        if (reward.getMessage() != null && !reward.getMessage().isBlank()) {
            player.sendMessage(mm.deserialize(reward.getMessage()));
        }
    }

    public void reload(JavaPlugin plugin) {
        rewardMap.clear();
        rewardMap.putAll(loadRewards(plugin));
    }
}