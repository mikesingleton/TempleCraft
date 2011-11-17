package com.msingleton.templecraft.listeners;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

import com.msingleton.templecraft.TempleCraft;
import com.nijikokun.register.payment.*;
public class TCServerListener extends ServerListener {
	private Methods Methods = null;

    public TCServerListener(TempleCraft plugin) {
    	try{
    		this.Methods = new Methods();
    	}catch(NoClassDefFoundError e){
    		System.out.println("[TempleCraft] Error: Register not found.");
    		System.out.println("[TempleCraft] Not hooked into Economy");
    	}
    }

	public void onPluginDisable(PluginDisableEvent event) {
		try{
	    	if(Methods == null)
	    		return;
	        // Check to see if the plugin thats being disabled is the one we are using
	        if (this.Methods != null && com.nijikokun.register.payment.Methods.hasMethod()) {
	            Boolean check = com.nijikokun.register.payment.Methods.checkDisabled(event.getPlugin());
	
	            if(check) {
	                TempleCraft.method = null;
	                System.out.println("[TempleCraft] Un-hooked from Economy.");
	            }
	        }
		}catch(IncompatibleClassChangeError e){
			System.out.println("[TempleCraft] Error: Another Plugin is using an old version of Register.");
			System.out.println("[TempleCraft] Not hooked into Economy");
		}
    }


	public void onPluginEnable(PluginEnableEvent event) {
		try{
	    	if(Methods == null)
	    		return;
	    		
	        // Check to see if we need a payment method
	        if (!com.nijikokun.register.payment.Methods.hasMethod()) {
	            if(com.nijikokun.register.payment.Methods.setMethod(event.getPlugin().getServer().getPluginManager())) {
	                // You might want to make this a public variable inside your MAIN class public Method Method = null;
	                // then reference it through this.plugin.Method so that way you can use it in the rest of your plugin ;)
	            	TempleCraft.method = com.nijikokun.register.payment.Methods.getMethod();
	                System.out.println("[TempleCraft] Hooked into Economy (" + TempleCraft.method.getName() + " version: " + TempleCraft.method.getVersion() + ")");
	            }
	        }
		}catch(IncompatibleClassChangeError e){
			System.out.println("[TempleCraft] Error: Another Plugin is using an old version of Register.");
			System.out.println("[TempleCraft] Not hooked into Economy");
		}
    }
}
