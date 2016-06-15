package com.joo.chestshopinfo;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.UUIDs.NameManager;
import com.joo.chestshopinfo.help.FormatHelper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.joo.chestshopinfo.help.BukkitHelper.convertItemStackToJson;
import static com.joo.chestshopinfo.help.BukkitHelper.targetBlock;

public class ChestShopInfoCommands implements CommandExecutor {

    private ChestShopInfo plugin;

    public ChestShopInfoCommands(ChestShopInfo instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        Player player = null;
        if (sender instanceof Player) player = (Player) sender;
        // Rückmeldung bei "/chestshop info"
        if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
            sender.sendMessage(ChatColor.GRAY + "Informationsplugin von JOO200.");
            return true;
        }

        // Rückmeldung bei "/chestshop help"
        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.GRAY
                    + "Schaue ein Shopschild an, von welchem du wissen möchtest, welches Item dieses verkauft. Führe dann "
                    + ChatColor.GOLD + "/shopinfo" + ChatColor.GRAY + " aus.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("ChestShopInfo.reload")) {
                sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl auszuführen.");
                return true;
            }
            plugin.getLogger().info("Das Plugin wird neu geladen.");
            if (sender instanceof Player) sender.sendMessage(ChatColor.RED + "Das Plugin wird neu geladen.");
            plugin.onEnable();
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("debug")) {
            if (!sender.hasPermission("ChestShopInfo.debug")) {
                sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl auszuführen.");
                return true;
            }
            if (args.length == 2 && args[1].equalsIgnoreCase("true")) {
                ChestShopInfo.debug = true;
                plugin.getLogger().info("Debug-Modus aktiviert");
                sender.sendMessage(ChatColor.RED + "Debug-Modus aktiviert.");
                return true;
            } else if (args.length == 2 && args[1].equalsIgnoreCase("false")) {
                ChestShopInfo.debug = false;
                plugin.getLogger().info("Debug-Modus deaktiviert");
                sender.sendMessage(ChatColor.RED + "Debug-Modus deaktiviert.");
                return true;
            } else if (args.length == 2 && args[1].equalsIgnoreCase("status")) {
                if (ChestShopInfo.debug) {
                    sender.sendMessage(ChatColor.RED + "Debug-Modus ist aktiviert.");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Debug-Modus ist deaktiviert.");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Ungültige Eingabe. (true | false)");
                return true;
            }
        }

        // Kontrolle, ob der Befehl von einem Spieler ausgeführt wurde.
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Du musst ein Spieler sein.");
            return true;
        }

        // Kontrolle der Permissions
        if (!player.hasPermission("ChestShopInfo.use")) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl auszuführen.");
            return true;
        }

        Block block = targetBlock(player);
        // Kontrolle, ob angeschauter Block ein Schild ist.
        if (block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
            if (ChestShopInfo.debug)
                plugin.getLogger().info("Spieler " + player.getDisplayName() + " schaut nicht auf ein Schild.");
            player.sendMessage(ChatColor.RED + "Du musst ein Schild anschauen");
            return true;
        }

        // Kontrolle, ob Schild ein ChestShop-Schild ist
        if (!ChestShopSign.isValid(block)) {
            if (ChestShopInfo.debug)
                plugin.getLogger().info("Spieler " + player.getDisplayName() + " schaut auf kein gültiges ShopSchild.");
            player.sendMessage(ChatColor.RED + "Dies ist kein gültiges Shopschild!");
            return true;
        }

        Sign sign = (Sign) block.getState();
        String name = sign.getLine(0);
        String ownerName = NameManager.getFullUsername(name);        //Name vom ChestShop Besitzer

        String amount = sign.getLine(1);
        Double amountDouble = Double.parseDouble(amount);
        int amountInt = Integer.parseInt(amount);
        String prices = sign.getLine(2);
        String signItemName = sign.getLine(3);
        boolean buy = prices.contains("B") | prices.contains("b");
        boolean sell = prices.contains("S") | prices.contains("s");
        if (ChestShopInfo.debug) {
            plugin.getLogger().info("Spieler " + player.getDisplayName() + " schaut auf Shopschild:");
            plugin.getLogger().info("Schild beinhaltet: Name: " + name + " (AnzeigeName: " + ownerName + "), Menge: " + amount + ", Ver- bzw. Ankauf: " + prices + ", signItemName: " + signItemName);
        }

        ItemStack item = MaterialUtil.getItem(signItemName); // Bekomme ItemStack über ChestShop-Methode
        Material material = item.getType();
        MaterialTranslations translations = plugin.translations;

        TextComponent temp;    //temporar TextComponent
        TextComponent displayName = null;
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            displayName = new TextComponent(" (umbenannt zu: ");
            displayName.setColor(ChatColor.DARK_GRAY);

            temp = new TextComponent(item.getItemMeta().getDisplayName());
            temp.setColor(ChatColor.GREEN);
            displayName.addExtra(temp);

            temp = new TextComponent(")");
            temp.setColor(ChatColor.DARK_GRAY);
            displayName.addExtra(temp);
        }

        String itemString; // Umwandlung des ItemStacks zu JSON. Für Hovereffekt
        try {
            itemString = convertItemStackToJson(item);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("ChestShopInfo: Es ist ein Fehler aufgetreten (IllegalArgumentException");
            player.sendMessage(ChatColor.RED + "Es ist ein Fehler aufgetreten. Bitte kontaktiere einen Admin.");
            return true;
        }
        HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(itemString).create()); // HoverEffekt erstellt

        String translatedName; // getTranslation(material, Integer.parseInt(amount));
        translatedName = translations.getTranslation(material, amountInt);
        if (amount.equalsIgnoreCase("1")) { // Singular
            amount = translations.getArticle(material);
        }
        player.spigot().sendMessage(getInfoText(ownerName));

        if (buy) { // Wenn der Shop verkauft (man kann kaufen)
            double buyPriceDouble = PriceUtil.getBuyPrice(prices);
            String buyPrice = FormatHelper.formatBuyPrice(buyPriceDouble);

            double pricePerItem = Math.round(100.0 * buyPriceDouble / amountDouble);
            double pricePerStack = Math.round(6400.0 * buyPriceDouble / amountDouble);
            pricePerItem = pricePerItem / 100;
            pricePerStack = pricePerStack / 100;


            HoverEvent price = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                    ChatColor.GRAY + "Preis pro Stück: " + ChatColor.GOLD + pricePerItem + " Eskonen. \n" +
                            ChatColor.GRAY + "Preis pro Stack: " + ChatColor.GOLD + pricePerStack + " Eskonen.").create());

            // [Info]: Dieser Shop verkauft xx Items (umbenannt zu xxx) für x Eskonen.

            TextComponent toSend = new TextComponent("Dieser Shop verkauft ");
            toSend.setColor(ChatColor.GRAY);

            temp = new TextComponent("" + amount + " ");
            temp.setColor(ChatColor.GOLD);
            toSend.addExtra(temp);

            temp = new TextComponent(translatedName);
            temp.setColor(ChatColor.GOLD);
            temp.setHoverEvent(event);
            toSend.addExtra(temp);

            if (displayName != null) {
                player.spigot().sendMessage(toSend);

                toSend = new TextComponent(displayName.duplicate());
            }

            temp = new TextComponent(" für ");
            temp.setColor(ChatColor.GRAY);
            toSend.addExtra(temp);

            temp = new TextComponent("" + buyPrice + "");
            temp.setHoverEvent(price);
            temp.setColor(ChatColor.GOLD);
            toSend.addExtra(temp);

            temp = new TextComponent(".");
            temp.setColor(ChatColor.GRAY);
            toSend.addExtra(temp);

            player.spigot().sendMessage(toSend);

        }
        if (sell) { // Ebenso für den Ankauf
            double sellPriceDouble = PriceUtil.getSellPrice(prices);
            String sellPrice = FormatHelper.formatBuyPrice(sellPriceDouble);

            //Anzeige des Preises/Stack und /Stück im Hover.
            double pricePerItem = Math.round(100.0 * sellPriceDouble / amountDouble);
            double pricePerStack = Math.round(6400.0 * sellPriceDouble / amountDouble);
            pricePerItem = pricePerItem / 100;
            pricePerStack = pricePerStack / 100;

            HoverEvent price = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                    ChatColor.GRAY + "Preis pro Stück: " + ChatColor.GOLD + pricePerItem + " Eskonen. \n" +
                            ChatColor.GRAY + "Preis pro Stack: " + ChatColor.GOLD + pricePerStack + " Eskonen.").create());

            //[Info]: Dieser Shop kauft xx Items (umbenannt zu xxx) für x Eskonen an.
            TextComponent toSend = new TextComponent("Dieser Shop kauft ");
            toSend.setColor(ChatColor.GRAY);

            temp = new TextComponent("" + amount + " ");
            temp.setColor(ChatColor.GOLD);
            toSend.addExtra(temp);

            temp = new TextComponent(translatedName);
            temp.setColor(ChatColor.GOLD);
            temp.setHoverEvent(event);
            toSend.addExtra(temp);

            if (displayName != null) {
                player.spigot().sendMessage(toSend);

                toSend = new TextComponent(displayName.duplicate());
            }

            temp = new TextComponent(" für ");
            temp.setColor(ChatColor.GRAY);
            toSend.addExtra(temp);

            temp = new TextComponent("" + sellPrice + "");
            temp.setHoverEvent(price);
            temp.setColor(ChatColor.GOLD);
            toSend.addExtra(temp);

            temp = new TextComponent(" an.");
            temp.setColor(ChatColor.GRAY);
            toSend.addExtra(temp);

            player.spigot().sendMessage(toSend);

        }
        return true;
    }

    // Info-Text
    private TextComponent getInfoText(String pString) {
        TextComponent info = new TextComponent("[ShopInfo]: ");
        info.setColor(ChatColor.GREEN);
        TextComponent temp = new TextComponent("Informationen über einen ChestShop von " + pString + ":");
        temp.setColor(ChatColor.GRAY);
        info.addExtra(temp);
        return info;
    }
}
