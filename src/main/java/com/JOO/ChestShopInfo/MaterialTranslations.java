package com.JOO.ChestShopInfo;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.Map;


public class MaterialTranslations {

    private Map<Material, Translation> translations = new EnumMap<>(Material.class);
    ChestShopInfo plugin;

    /*
     * 	Die Itemnamen werden aus der Config geladen und in einer Map gespeichert. Diese Map beinhaltet:
     * 	- das Material des Items
     *  	- das übersetzte Wort im singular
     *     - das übersetzte Wort im Plural
     *     - den unbestimmten Artikel für den Singularfall.
     *
     */
    public MaterialTranslations(ChestShopInfo instance) {
        translations.clear();
        plugin = instance;
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();
        if (cfg == null) plugin.getLogger().warning("Config nicht gefunden!");    // Einsatz vom Logger

        ConfigurationSection cfgSection = cfg.getConfigurationSection("translations");
        for (String key : cfgSection.getKeys(false)) {
            try {
                Material mat = Material.getMaterial(key);
                String singular = cfg.getString("translations." + key + ".singular");
                String plural = cfg.getString("translations." + key + ".plural");
                String article = cfg.getString("translations." + key + ".article");
                if (singular == null || plural == null || article == null)
                    plugin.getLogger().warning("Fehlerhafte Formatierung bei Teil " + key);
                if (ChestShopInfo.debug)
                    plugin.getLogger().info("New map: " + key + ", " + singular + ", " + plural + ", " + article + "");
                addMaterial(mat, singular, plural, article);
            } catch (NullPointerException e) {
                plugin.getLogger().warning("Material unbekannt:" + key + ", Ueberspringe dieses Material.");
            }
        }
        plugin.getLogger().info("Es wurden " + translations.size() + " Uebersetzungen gefunden und gespeichert.");
    }

    // Gibt die Übersetzung des übergebenen Strings und der Anzahl aus. Sollte der Anzahl eins sein, so wird der Singularfall ausgegeben.
    public String getTranslation(Material material, int amount) {
        Translation translation = translations.get(material);
        if (translation != null)
            return amount == 1 ? translation.getSingular() : translation.getPlural();
        plugin.getLogger().warning("Material " + material + " nicht in der Config enthalten. Benutze Umformatierung.");
        return material.name().toLowerCase();
    }

    public String getArticle(Material material) {
        Translation translation = translations.get(material);
        if (translation == null) return "ein";
        return translation.getArticle();
    }

    private void addMaterial(Material material, String singular, String plural, String article) {
        translations.put(material, new Translation(singular, plural, article));
    }

    public static class Translation {
        private String singular;
        private String plural;
        private String article;

        public Translation(String singular, String plural, String article) {
            this.singular = singular;
            this.plural = plural;
            this.article = article;
        }

        public String getSingular() {
            return singular;
        }

        public String getPlural() {
            return plural;
        }

        public String getArticle() {
            return article;
        }
    }

}

	
