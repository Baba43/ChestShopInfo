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
import com.sainttx.auctions.util.ReflectionUtil;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MyCommandExecutor implements CommandExecutor {

	public MyCommandExecutor(ChestShopInfo plugin) {
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {

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

		// Kontrolle, ob der Befehl von einem Spieler ausgeführt wurde.
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Du musst ein Spieler sein.");
			return true;
		}

		Player player = (Player) sender;
		
		// Kontrolle der Permissions
		if (!player.hasPermission("ChestShopInfo.use")) {
			sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl auszuführen.");
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
			player.sendMessage(ChatColor.RED + "Dies ist kein gültiges Shopschild!");
			return true;
		}
		
		Sign sign = (Sign) block.getState();
		String amount = sign.getLine(1);
		String prices = sign.getLine(2);
		String signItemName = sign.getLine(3);
		boolean buy = prices.contains("B") | prices.contains("b");
		boolean sell = prices.contains("S") | prices.contains("s");

		ItemStack item = MaterialUtil.getItem(signItemName); // Bekomme ItemStack über ChestShop-Methode

		String displayName1 = ""; // Zusatz bei umbenannten Items
		String displayName2 = "";
		String displayName3 = "";
		if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
			displayName1 = " (umbenannt zu: ";
			displayName2 = item.getItemMeta().getDisplayName();
			displayName3 = ")";
		}

		String itemString = null; // Umwandlung des ItemStacks zu JSON. Für Hovereffekt
		try {
			itemString = convertItemStackToJson(item);
		} catch (IllegalArgumentException e) {
			System.out.println("ChestShopInfo: Es ist ein Fehler aufgetreten (IllegalArgumentException");
			player.sendMessage(ChatColor.RED + "Es ist ein Fehler aufgetreten. Bitte kontaktiere einen Admin.");
			return true;
		}
		HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(itemString).create()); // HoverEffekt erstellt

		String itemMaterialString = item.getType().toString(); // Übersetze aufs Deutsche
		String translatedName = null;
		if (amount.equalsIgnoreCase("1")) { // Für ein Item.
			amount = "Ein";
			translatedName = getTranslatedMaterial.translation(itemMaterialString);
		} else { // Plural
			translatedName = getTranslatedMaterial.translation2(itemMaterialString);

		}

		if (buy) { // Wenn der Shop verkauft (man kann kaufen)
			double buyPriceDouble = PriceUtil.getBuyPrice(prices);
			String buyPrice = null;
			if (buyPriceDouble == 1) { // Wenn das Item nur 1 Eskone kostet
				buyPrice = "eine Eskone";
			} else { // ansonsten "Zahl + Eskonen"
				buyPrice = String.valueOf(buyPriceDouble) + " Eskonen";
			}
			// 1. Zeile: [Info]: Dieser Shop verkauft:
			BaseComponent[] baseComponent = new ComponentBuilder("" + getInfoText() + "").color(ChatColor.GREEN)
					.append("Dieser Shop verkauft:").color(ChatColor.GRAY).create();
			TextComponent message = new TextComponent(baseComponent);
			player.spigot().sendMessage(message);

			// 2. Zeile: Anzahl von Items (umbenannt zu xxx) für xxx Eskonen.
			baseComponent = new ComponentBuilder("" + amount + " ").color(ChatColor.GOLD).append(translatedName)
					.color(ChatColor.GOLD).event(event).append(displayName1).reset().color(ChatColor.DARK_GRAY)
					.append(displayName2).color(ChatColor.GREEN).append(displayName3).color(ChatColor.DARK_GRAY)
					.append(" für ").color(ChatColor.GRAY).append("" + buyPrice + "").color(ChatColor.GOLD).append(".")
					.color(ChatColor.GRAY).create();
			message = new TextComponent(baseComponent);
			player.spigot().sendMessage(message);
		}
		if (sell) { // Ebenso für den Ankauf
			double sellPriceDouble = PriceUtil.getSellPrice(prices);
			String sellPrice = null;
			if (sellPriceDouble == 1) {
				sellPrice = "eine Eskone";
			} else {
				sellPrice = String.valueOf(sellPriceDouble) + " Eskonen";
			}
			BaseComponent[] baseComponent = new ComponentBuilder("" + getInfoText() + "").color(ChatColor.GREEN)
					.append("Dieser Shop kauft:").color(ChatColor.GRAY).create();
			TextComponent message = new TextComponent(baseComponent);
			player.spigot().sendMessage(message);

			baseComponent = new ComponentBuilder("" + amount + " ").color(ChatColor.GOLD).append(translatedName)
					.color(ChatColor.GOLD).event(event).append(displayName1).reset().color(ChatColor.DARK_GRAY)
					.append(displayName2).color(ChatColor.GREEN).append(displayName3).color(ChatColor.DARK_GRAY)
					.append(" für ").color(ChatColor.GRAY).append("" + sellPrice + "").color(ChatColor.GOLD).append(".")
					.color(ChatColor.GRAY).create();
			message = new TextComponent(baseComponent);
			player.spigot().sendMessage(message);
		}

		return true;

	}

	// Info-Text (TODO: Auslagerung in Methode von baba43lib zur
	// Vereinheitlichung auf Terraconia)
	private String getInfoText() {
		String info = "[Info]: ";
		return info;
	}

	// Angeschauter Block bekommen. Maximale Reichweite 5 Blöcke
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
