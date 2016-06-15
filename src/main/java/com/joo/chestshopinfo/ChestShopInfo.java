package com.joo.chestshopinfo;

import de.baba43.lib.config.ConfigHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestShopInfo extends JavaPlugin {

    private ChestShopInfoCommands myExecutor;

    public MaterialTranslations translations;
    public static boolean debug = false;

    @Override
    public void onEnable() {
        FileConfiguration config = getConfig();
        ConfigurationSection translations = ConfigHelper.getSection(config, "translations");
        this.translations = new MaterialTranslations(translations);

        myExecutor = new ChestShopInfoCommands(this);
        getCommand("shopinfo").setExecutor(myExecutor);
        saveConfig();
    }
}
