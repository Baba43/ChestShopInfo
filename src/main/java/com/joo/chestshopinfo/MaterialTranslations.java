package com.joo.chestshopinfo;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.EnumMap;
import java.util.Map;


public class MaterialTranslations {

    private Map<Material, Translation> translations = new EnumMap<>(Material.class);
    private ChestShopInfo plugin;

    /*
     * 	Die Itemnamen werden aus der Config geladen und in einer Map gespeichert. Diese Map beinhaltet:
     * 	- das Material des Items
     *  	- das übersetzte Wort im singular
     *     - das übersetzte Wort im Plural
     *     - den unbestimmten Artikel für den Singularfall.
     *
     */
    public MaterialTranslations(ChestShopInfo plugin) {
        this.plugin = plugin;
    }

    public void loadMaterials(ConfigurationSection cfgSection) {
        translations.clear();
        for (String key : cfgSection.getKeys(false)) {
            ConfigurationSection cfg = cfgSection.getConfigurationSection(key);

            Material mat;
            try {
                mat = Material.getMaterial(key);
            } catch (NullPointerException e) {
                plugin.getLogger().warning("Material unbekannt:" + key + ", Ueberspringe dieses Material.");
                continue;
            }

            String singular = cfg.getString("singular");
            String plural = cfg.getString("plural");
            String article = cfg.getString("article");

            if (singular == null || plural == null || article == null) {
                plugin.getLogger().warning("Fehlerhafte Formatierung bei Teil " + key);
                continue;
            }

            addMaterial(mat, singular, plural, article);
        }
        plugin.getLogger().info("Es wurden " + translations.size() + " Uebersetzungen gefunden und gespeichert.");
    }

    private void addMaterial(Material material, String singular, String plural, String article) {
        translations.put(material, new Translation(singular, plural, article));
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

	
