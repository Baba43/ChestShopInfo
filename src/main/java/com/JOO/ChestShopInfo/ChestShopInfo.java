package com.JOO.ChestShopInfo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ChestShopInfo extends JavaPlugin {

    private MyCommandExecutor myExecutor;
    public static Plugin plugin;
    public static ChestShopInfo instance;

    public MaterialTranslations translations;
    public static boolean debug = false;
    public final FileConfiguration config = this.getConfig();


    @Override
    public void onEnable() {


        createConfig();
        reloadConfig();
        translations = new MaterialTranslations(this);

        myExecutor = new MyCommandExecutor(this);
        getCommand("shopinfo").setExecutor(myExecutor);

    }

    @Override
    public void onDisable() {

    }

    public void createConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("Config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                getLogger().info("Config.yml found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class ConfigListener implements Listener {
        ChestShopInfo plugin;

        public ConfigListener(ChestShopInfo instance) {
            plugin = instance;
        }

    }

}
