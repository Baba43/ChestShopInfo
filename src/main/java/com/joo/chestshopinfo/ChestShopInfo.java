package com.joo.chestshopinfo;

import de.baba43.lib.config.ConfigHelper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestShopInfo extends JavaPlugin {

    public static boolean debug = false;

    private MaterialTranslations translations;

    @Override
    public void onEnable() {
        translations = new MaterialTranslations(this);
        getCommand("shopinfo").setExecutor(new ChestShopInfoCommands(this, translations));
        loadConfig();
    }

    private void loadConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();
        translations.loadMaterials(ConfigHelper.getSection(config, "translations"));
        saveConfig();
    }

    public void onReload() {
        reloadConfig();
        loadConfig();
    }
}
