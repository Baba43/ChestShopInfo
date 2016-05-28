package com.JOO.ChestShopInfo;

import org.bukkit.plugin.java.JavaPlugin;

public class ChestShopInfo extends JavaPlugin {
	private MyCommandExecutor myExecutor;
	
	@Override
	public void onEnable() {
		myExecutor = new MyCommandExecutor(this);
		getCommand("shopinfo").setExecutor(myExecutor);
		
	}
	
	@Override
	public void onDisable() {
		
	}
	
}
