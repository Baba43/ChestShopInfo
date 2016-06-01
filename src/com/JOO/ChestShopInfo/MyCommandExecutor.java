package com.JOO.ChestShopInfo;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.UUIDs.NameManager;
import com.sainttx.auctions.util.ReflectionUtil;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MyCommandExecutor implements CommandExecutor {
	ChestShopInfo plugin;

	public MyCommandExecutor(ChestShopInfo instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {

		Player player = (Player) sender;
		// R�ckmeldung bei "/chestshop info"
		if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
			sender.sendMessage(ChatColor.GRAY + "Informationsplugin von JOO200.");
			return true;
		}

		// R�ckmeldung bei "/chestshop help"
		if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
			sender.sendMessage(ChatColor.GRAY
					+ "Schaue ein Shopschild an, von welchem du wissen m�chtest, welches Item dieses verkauft. F�hre dann "
					+ ChatColor.GOLD + "/shopinfo" + ChatColor.GRAY + " aus.");
			return true;
		}
		
		if(args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			if(!player.hasPermission("ChestShopInfo.reload")) {
				sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl auszuf�hren.");
				return true;
			}
			// hier fehlt noch eine Konsolenausgabe!
			sender.sendMessage(ChatColor.RED + "Das Plugin wird neu geladen.");
			plugin.onEnable();
			return true;
		}
		
		if(args.length == 2 && args[0].equalsIgnoreCase("debug")) {
			if(!player.hasPermission("ChestShopInfo.debug")) {
				sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl auszuf�hren.");
				return true;
			}
			if(args[1].equalsIgnoreCase("true")) {
				ChestShopInfo.debug = true;
				sender.sendMessage(ChatColor.RED + "Debug-Modus aktiviert.");
				return true;
			} else if(args[1].equalsIgnoreCase("false")) {
				ChestShopInfo.debug = false;
				sender.sendMessage(ChatColor.RED + "Debug-Modus deaktiviert.");
				return true;
			} else if(args[1].equalsIgnoreCase("status")) {
				if(ChestShopInfo.debug) { 
					sender.sendMessage(ChatColor.RED + "Debug-Modus ist aktiviert.");
					return true;
				} else {
					 sender.sendMessage(ChatColor.RED + "Debug-Modus ist deaktiviert.");
					 return true;
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "Ung�ltige Eingabe. (true | false)");
				return true;
			}
		}

		// Kontrolle, ob der Befehl von einem Spieler ausgef�hrt wurde.
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Du musst ein Spieler sein.");
			return true;
		}
				
		// Kontrolle der Permissions
		if (!player.hasPermission("ChestShopInfo.use")) {
			sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl auszuf�hren.");
			return true;
		}

		Block block = targetBlock(player);
		// Kontrolle, ob angeschauter Block ein Schild ist.
		if (block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
			player.sendMessage(ChatColor.RED + "Du musst ein Schild anschauen");
			return true;
		}
	
		// Kontrolle, ob Schild ein ChestShop-Schild ist
		if (!ChestShopSign.isValid(block)) {
			player.sendMessage(ChatColor.RED + "Dies ist kein g�ltiges Shopschild!");
			return true;
		}
		
		Sign sign = (Sign) block.getState();
		String name = sign.getLine(0);
        String ownerName = NameManager.getFullUsername(name);		//Name vom ChestShop Besitzer
        
		String amount = sign.getLine(1);
		Double amountDouble = Double.parseDouble(amount);
		String prices = sign.getLine(2);
		String signItemName = sign.getLine(3);
		boolean buy = prices.contains("B") | prices.contains("b");
		boolean sell = prices.contains("S") | prices.contains("s");
		
		ItemStack item = MaterialUtil.getItem(signItemName); // Bekomme ItemStack �ber ChestShop-Methode
		Material material = item.getType();
		MaterialTranslations translations = plugin.translations;
		
		TextComponent temp = null;	//temporar TextComponent
		TextComponent displayName = null;
		if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
			displayName = new TextComponent (" (umbenannt zu: ");
			displayName.setColor(ChatColor.DARK_GRAY);
			
			temp = new TextComponent (item.getItemMeta().getDisplayName());
			temp.setColor(ChatColor.GREEN);
			displayName.addExtra(temp);
			
			temp = new TextComponent (")");
			temp.setColor(ChatColor.DARK_GRAY);
			displayName.addExtra(temp);
		}
		

		String itemString = null; // Umwandlung des ItemStacks zu JSON. F�r Hovereffekt
		try {
			itemString = convertItemStackToJson(item);
		} catch (IllegalArgumentException e) {
			System.out.println("ChestShopInfo: Es ist ein Fehler aufgetreten (IllegalArgumentException");
			player.sendMessage(ChatColor.RED + "Es ist ein Fehler aufgetreten. Bitte kontaktiere einen Admin.");
			return true;
		}
		HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(itemString).create()); // HoverEffekt erstellt

		String translatedName = translations.getTranslation(material, Integer.parseInt(amount));

		if (amount.equalsIgnoreCase("1")) { // Singular
			amount = translations.getArticle(material);
		}
		
		player.spigot().sendMessage(getInfoText(ownerName));

		if (buy) { // Wenn der Shop verkauft (man kann kaufen)
			double buyPriceDouble = PriceUtil.getBuyPrice(prices);
			String buyPrice = null;
			if (buyPriceDouble == 1) { // Wenn das Item nur 1 Eskone kostet
				buyPrice = "eine Eskone";
			} else if (buyPriceDouble == 0) {
				buyPrice = "umsonst";
			} else { // ansonsten "Zahl + Eskonen"
				buyPrice = String.valueOf(buyPriceDouble) + " Eskonen";
			}
			
			double pricePerItem = Math.round(100.0*buyPriceDouble / amountDouble);
			double pricePerStack = Math.round(6400.0*buyPriceDouble / amountDouble) ;
			pricePerItem = pricePerItem/100;
			pricePerStack = pricePerStack/100;
			
			
			HoverEvent price = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
					ChatColor.GRAY + "Preis pro St�ck: " + ChatColor.GOLD + pricePerItem + " Eskonen. \n" +
					ChatColor.GRAY + "Preis pro Stack: " + ChatColor.GOLD + pricePerStack + " Eskonen.").create());
			
			// [Info]: Dieser Shop verkauft xx Items (umbenannt zu xxx) f�r x Eskonen.
			
			TextComponent toSend = new TextComponent("Dieser Shop verkauft ");
			toSend.setColor(ChatColor.GRAY);

			temp = new TextComponent(""+ amount + " ");
			temp.setColor(ChatColor.GOLD);
			toSend.addExtra(temp);
						
			temp = new TextComponent (translatedName);
			temp.setColor(ChatColor.GOLD);
			temp.setHoverEvent(event);
			toSend.addExtra(temp);
						
			if(displayName != null) {
				player.spigot().sendMessage(toSend);
				
				toSend = new TextComponent(displayName.duplicate());
			}
			
			temp = new TextComponent(" f�r ");
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
		if (sell) { // Ebenso f�r den Ankauf
			double sellPriceDouble = PriceUtil.getSellPrice(prices);
			String sellPrice = null;
			if (sellPriceDouble == 1) {
				sellPrice = "eine Eskone";
			} else if (sellPriceDouble == 0) {
				sellPrice = "umsonst";
			} else {
				sellPrice = String.valueOf(sellPriceDouble) + " Eskonen";
			}

			//Anzeige des Preises/Stack und /St�ck im Hover.
			double pricePerItem = Math.round(100.0*sellPriceDouble / amountDouble);
			double pricePerStack = Math.round(6400.0*sellPriceDouble / amountDouble) ;
			pricePerItem = pricePerItem/100;
			pricePerStack = pricePerStack/100;
			
			HoverEvent price = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
					ChatColor.GRAY + "Preis pro St�ck: " + ChatColor.GOLD + pricePerItem + " Eskonen. \n" +
					ChatColor.GRAY + "Preis pro Stack: " + ChatColor.GOLD + pricePerStack + " Eskonen.").create());
			
			//[Info]: Dieser Shop kauft xx Items (umbenannt zu xxx) f�r x Eskonen an.
			TextComponent toSend  = new TextComponent("Dieser Shop kauft ");
			toSend.setColor(ChatColor.GRAY);

			temp = new TextComponent(""+ amount + " ");
			temp.setColor(ChatColor.GOLD);
			toSend.addExtra(temp);
						
			temp = new TextComponent (translatedName);
			temp.setColor(ChatColor.GOLD);
			temp.setHoverEvent(event);
			toSend.addExtra(temp);
						
			if(displayName != null) {
				player.spigot().sendMessage(toSend);

				toSend = new TextComponent(displayName.duplicate());
			}
					
			temp = new TextComponent(" f�r ");
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
		TextComponent temp = new TextComponent("Informationen �ber einen ChestShop von " + pString+ ":");
		temp.setColor(ChatColor.GRAY);
		info.addExtra(temp);
		
		
		return info;
	}

	// Angeschauter Block bekommen. Maximale Reichweite 5 Bl�cke
	private Block targetBlock(Player p) {
		return p.getTargetBlock((Set<Material>) null, 5);
	}

	// Convert to JSON:
	public String convertItemStackToJson(ItemStack itemStack) {
		// ItemStack methods to get a net.minecraft.server.ItemStack object for
		// serialization
		Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
		Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);

		// NMS Method to serialize a net.minecraft.server.ItemStack to a valid
		// Json string
		Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
		Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
		Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

		Object nmsNbtTagCompoundObj; // This will just be an empty
										// NBTTagCompound instance to invoke the
										// saveNms method
		Object nmsItemStackObj; // This is the net.minecraft.server.ItemStack
								// object received from the asNMSCopy method
		Object itemAsJsonObject; // This is the net.minecraft.server.ItemStack
									// after being put through saveNmsItem
									// method

		try {
			nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
			nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
			itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
		} catch (Throwable t) {
			Bukkit.getLogger().log(Level.SEVERE, "failed to serialize itemstack to nms item", t);
			return null;
		}

		// Return a string representation of the serialized object
		return itemAsJsonObject.toString();
	}

}
