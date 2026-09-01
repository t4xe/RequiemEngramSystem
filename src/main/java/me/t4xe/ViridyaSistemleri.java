package me.t4xe;

import me.t4xe.command.Komutlar;
import me.t4xe.engram.EngramVerme;
import me.t4xe.engram.reward.EngramRewardHandler;
import me.t4xe.othersystems.RequiemParchment;
import org.bukkit.plugin.java.JavaPlugin;

public final class ViridyaSistemleri extends JavaPlugin {

    private EngramRewardHandler rewardHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("ViridyaSistemleri aktifleştirildi!");
        rewardHandler = new EngramRewardHandler(this);

        getServer().getPluginManager().registerEvents(new EngramVerme(this), this);
        getServer().getPluginManager().registerEvents(new RequiemParchment(), this);

        Komutlar komutlar = new Komutlar(this, rewardHandler);

        if (getCommand("vsreload") != null) { getCommand("vsreload").setExecutor(komutlar); }
        if (getCommand("egkır") != null) { getCommand("egkır").setExecutor(komutlar); }
        if (getCommand("egver") != null) { getCommand("egver").setExecutor(komutlar); }
        if (getCommand("besyaver") != null) { getCommand("besyaver").setExecutor(komutlar); }
        if (getCommand("dilektut") != null) { getCommand("dilektut").setExecutor(komutlar); }
    }

    @Override
    public void onDisable() {
        getLogger().info("ViridyaSistemleri devredışı bırakıldı!");
    }
}