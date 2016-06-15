package com.joo.chestshopinfo;

import de.baba43.lib.config.ConfigHelper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestShopInfo extends JavaPlugin {

    public static boolean debug = false;

    @Override
    public void onEnable() {
        FileConfiguration config = getConfig();
        MaterialTranslations translations = new MaterialTranslations(ConfigHelper.getSection(config, "translations"));
        getCommand("shopinfo").setExecutor(new ChestShopInfoCommands(this, translations));
        saveConfig();
    }
}
