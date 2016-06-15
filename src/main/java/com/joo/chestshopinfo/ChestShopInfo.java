package com.joo.chestshopinfo;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ChestShopInfo extends JavaPlugin {

    private ChestShopInfoCommands myExecutor;

    public MaterialTranslations translations;
    public static boolean debug = false;

    @Override
    public void onEnable() {
        createConfig();
        reloadConfig();
        translations = new MaterialTranslations(this);

        myExecutor = new ChestShopInfoCommands(this);
        getCommand("shopinfo").setExecutor(myExecutor);
    }

    private void createConfig() {
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
}
