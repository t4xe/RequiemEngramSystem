package me.t4xe.command;

import me.t4xe.engram.EngramItem;
import me.t4xe.engram.reward.EngramRewardHandler;
import me.t4xe.othersystems.OzelItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class Komutlar implements CommandExecutor {

    private final JavaPlugin plugin;
    private final EngramRewardHandler rewardHandler;
    private final NamespacedKey engramKey;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public Komutlar(JavaPlugin plugin, EngramRewardHandler rewardHandler) {
        this.plugin = plugin;
        this.rewardHandler = rewardHandler;
        this.engramKey = new NamespacedKey(plugin, "engram_type");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String cmd = command.getName().toLowerCase();

        return switch (cmd) {
            case "vsreload" -> handleVsreload(sender);
            case "egkır" -> handleEgkir(sender, args);
            case "egver" -> handleEgver(sender, args);
            case "besyaver" -> handleBesyaver(sender, args);
            case "dilektut" -> handleDilektut(sender, args);
            default -> true;
        };
    }

    private boolean handleVsreload(CommandSender sender) {
        if (!sender.hasPermission("v.reload")) {
            sender.sendMessage("Bilinmeyen komut.");
            return true;
        }

        plugin.reloadConfig();
        rewardHandler.reload(plugin);

        sender.sendMessage("§aViridya config yenilendi!");
        return true;
    }

    private boolean handleEgkir(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(mm.deserialize(
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Yanlış kullanım: /egkır <oyuncu> <engram>"
            ));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        EngramItem.EngramType targetType = EngramItem.EngramType.fromString(args[1]);
        ItemStack[] contents = target.getInventory().getContents();

        if (!sender.isOp()) {
            target.sendMessage(mm.deserialize(
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Bilinmeyen komut."
            ));
            return true;
        }

        if (!target.hasPermission("v.egkir")) {
            target.sendMessage(mm.deserialize(
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Yetkin yetersiz!"
            ));
            return true;
        }

        if (targetType == null) {
            sender.sendMessage(mm.deserialize(
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Geçersiz engram türü!"
            ));
            return true;
        }

        for (int i = 0; i < contents.length; i++) {

            ItemStack item = contents[i];
            if (item == null || !item.hasItemMeta()) continue;

            EngramItem.EngramType type = getTypeFromItem(item);
            if (type == null || type != targetType) continue;

            // firstEmpty() returns -1 if there are no empty slots (0-35)
            if (target.getInventory().firstEmpty() == -1) {
                target.sendMessage(mm.deserialize(
                        "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Envanterin dolu! Engramı kırmak için yer açmalısın."
                ));
                return true;
            }

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                target.getInventory().setItem(i, null);
            }

            rewardHandler.handleReward(type, target);

            String name = type.getCompactName();

            target.sendMessage(mm.deserialize(
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ "
                            + name +
                            " <#3ACBAE>başarıyla kırıldı!"
            ));

            return true;
        }

        target.sendMessage(mm.deserialize(
                "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Bu engram envanterinde yok."
        ));

        return true;
    }

    private boolean handleEgver(CommandSender sender, String[] args) {
        if (!sender.hasPermission("v.egver")) {
            sender.sendMessage(mm.deserialize(
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Bilinmeyen komut."
            ));
            return true;
        }

        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(mm.deserialize(
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Kullanım: /egver <oyuncu> <engram> [miktar]"
            ));
            return true;
        }

        Player target = plugin.getServer().getPlayerExact(args[0]);

        if (target == null || !target.isOnline()) {
            sender.sendMessage(mm.deserialize(
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Oyuncu bulunamadı."
            ));
            return true;
        }

        EngramItem.EngramType type = EngramItem.EngramType.fromString(args[1]);

        if (type == null) {
            sender.sendMessage(mm.deserialize(
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Geçersiz engram türü."
            ));
            return true;
        }

        int amount = 1; // default
        if (args.length == 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sender.sendMessage(mm.deserialize(
                        "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Geçersiz miktar."
                ));
                return true;
            }
        }

        ItemStack engram = EngramItem.createEngram(plugin, type);
        engram.setAmount(amount);

        var leftovers = target.getInventory().addItem(engram);

        if (!leftovers.isEmpty()) {
            leftovers.values().forEach(item ->
                    target.getWorld().dropItemNaturally(target.getLocation(), item)
            );
        }

        String name = type.getCompactName();

        sender.sendMessage(mm.deserialize(
                "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Eşya verildi: "
                        + name + " x" + amount + " → " + target.getName()
        ));

        target.sendMessage(mm.deserialize(
                "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#248673>"
                        + name +
                        " x" + amount +
                        "<#3ACBAE> envanterine eklendi!"
        ));

        return true;
    }

    private boolean handleBesyaver(CommandSender sender, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(mm.deserialize("<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Bilinmeyen komut."));
            return true;
        }

        // Argument Check
        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(mm.deserialize("<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Kullanım: /besyaver <oyuncu> <eşya> [miktar]"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(mm.deserialize("<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Oyuncu bulunamadı."));
            return true;
        }

        OzelItem.ItemType type = OzelItem.ItemType.fromString(args[1]);
        if (type == null) {
            sender.sendMessage(mm.deserialize("<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Geçersiz eşya adı! (requiem_parsomeni, dilek_tasi)"));
            return true;
        }

        int amount = 1; // default
        if (args.length == 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sender.sendMessage(mm.deserialize("<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Geçersiz miktar."));
                return true;
            }
        }

        ItemStack item = OzelItem.createItem(plugin, type);
        item.setAmount(amount);

        var leftovers = target.getInventory().addItem(item);

        if (!leftovers.isEmpty()) {
            leftovers.values().forEach(i ->
                    target.getWorld().dropItemNaturally(target.getLocation(), i)
            );
        }

        sender.sendMessage(mm.deserialize(
                "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Eşya verildi: "
                        + type.getDisplayName() + " x" + amount
        ));

        target.sendMessage(mm.deserialize(
                "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#248673>"
                        + type.getDisplayName() + " x" + amount +
                        "<#3ACBAE> envanterine eklendi!"
        ));

        return true;
    }

    private boolean handleDilektut(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(mm.deserialize(
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Yanlış kullanım: /dilektut <oyuncu>"
            ));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        ItemStack[] contents = target.getInventory().getContents();

        if (!sender.isOp()) {
            target.sendMessage(mm.deserialize(
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Bilinmeyen komut."
            ));
            return true;
        }

        if (!target.hasPermission("v.dilektut")) {
            target.sendMessage(mm.deserialize(
                    "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Yetkin yetersiz!"
            ));
            return true;
        }

        for (int i = 0; i < contents.length; i++) {

            ItemStack item = contents[i];
            if (item == null || !item.hasItemMeta()) continue;

            OzelItem.ItemType type = getOzelItemType(item);
            if (type != OzelItem.ItemType.DILEK_TASI) continue;

            if (target.getInventory().firstEmpty() == -1) {
                target.sendMessage(mm.deserialize(
                        "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Envanterin dolu! Dilek tutmak için yer açmalısın."
                ));
                return true;
            }

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                target.getInventory().setItem(i, null);
            }

            rewardHandler.handleReward(EngramItem.EngramType.DILEK_TASI, target);

            target.sendMessage(mm.deserialize("<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Dilek tuttun!"));

            return true;
        }

        target.sendMessage(mm.deserialize(
                "<gradient:#aacb61:#a4e1a1>Viridya</gradient> <#1D691C>➙ <#3ACBAE>Envanterinde dilek taşı yok."
        ));

        return true;
    }

    private EngramItem.EngramType getTypeFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();

        String id = meta.getPersistentDataContainer()
                .get(engramKey, PersistentDataType.STRING);

        if (id == null) return null;

        return EngramItem.EngramType.fromString(id);
    }

    private OzelItem.ItemType getOzelItemType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "ozel_item_type");

        String id = meta.getPersistentDataContainer()
                .get(key, PersistentDataType.STRING);

        if (id == null) return null;

        return OzelItem.ItemType.fromString(id);
    }
}