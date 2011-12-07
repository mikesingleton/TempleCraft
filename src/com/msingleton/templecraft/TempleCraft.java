package com.msingleton.templecraft;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.server.ServerListener;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.getspout.spoutapi.event.inventory.InventoryListener;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.HeroManager;
import com.msingleton.templecraft.listeners.TCBlockListener;
import com.msingleton.templecraft.listeners.TCInventoryListener;
import com.msingleton.templecraft.listeners.TCDamageListener;
import com.msingleton.templecraft.listeners.TCDisconnectListener;
import com.msingleton.templecraft.listeners.TCMonsterListener;
import com.msingleton.templecraft.listeners.TCPlayerListener;
import com.msingleton.templecraft.listeners.TCServerListener;
import com.msingleton.templecraft.listeners.TCTeleportListener;
import com.msingleton.templecraft.util.MobArenaClasses;
import com.msingleton.templecraft.util.Translation;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijikokun.register.payment.Method;

/**
 * TempleCraft
 *
 * @author msingleton
 */
public class TempleCraft extends JavaPlugin
{
    /* Array of commands used to determine if a command belongs to TempleCraft
     * or Mean Admins. */
    private Logger log;
    public List<String> ENABLED_COMMANDS;
    public static Method method = null;
    public static PermissionHandler permissionHandler;
    public static HeroManager heroManager = null;
    public static String language;
    public static String fileExtention = ".tcf";
    public static ChatColor c1 = ChatColor.DARK_AQUA;
	public static ChatColor c2 = ChatColor.WHITE;
	public static ChatColor c3 = ChatColor.GREEN;
    
    public TempleCraft()
    {
    }

    public void onEnable()
    {
        PluginDescriptionFile pdfFile = this.getDescription();
        
        log = getServer().getLogger();
        
        // Create event listeners.
        PluginManager pm = getServer().getPluginManager();
        
        // Initialize convenience variables in ArenaManager.
        TempleManager.init(this);
        
        boolean disable = false;
        //Check for Register, if not found copy file to plugins folder
        if (pm.getPlugin("Register") == null) {
            log.log(Level.SEVERE, "[TempleCraft] Register not found. Copying to plugins folder...");
            TCUtils.copyFromJarToDisk("Register.jar", new File("plugins/"));
            disable = true;
        }
        
        //Check for Spout, if not found copy file to plugins folder
        if (pm.getPlugin("Spout") == null) {
            log.log(Level.SEVERE, "[TempleCraft] Spout not found. Copying to plugins folder...");
            TCUtils.copyFromJarToDisk("Spout.jar", new File("plugins/"));
            disable = true;
        }
        
        if(disable){
        	System.out.println("[TempleCraft] Plugin disabling, try reloading...");
        	pm.disablePlugin(this);
        	return;
        }
        
        setupTranslations();
        setupPermissions();
        setupHeroes();
        
        ENABLED_COMMANDS = TCUtils.getEnabledCommands();
        
        // Bind the /tc and /tcraft commands to MACommands.
    	getCommand("tc").setExecutor(new TCCommands());
    	
        PlayerListener commandListener      = new TCEnabledCommands(this);
        PlayerListener playerListener       = new TCPlayerListener(this);
        InventoryListener inventoryListener = new TCInventoryListener();
        PlayerListener maListener           = new MobArenaClasses(this);
        PlayerListener teleportListener     = new TCTeleportListener(this);
        PlayerListener discListener         = new TCDisconnectListener(this);
        BlockListener  blockListener        = new TCBlockListener(this);
        EntityListener damageListener       = new TCDamageListener(this);
        EntityListener monsterListener      = new TCMonsterListener(this);
        ServerListener serverListener       = new TCServerListener(this);
        
        // TO-DO: PlayerListener to check for kills/deaths.
        
        // Register events.
    	
        pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, commandListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, playerListener,Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLAYER_MOVE,      playerListener,   Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT,  playerListener,   Priority.Normal,  this);
        pm.registerEvent(Event.Type.CUSTOM_EVENT,     inventoryListener,   Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT,  maListener,       Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLAYER_TELEPORT,  teleportListener, Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLAYER_QUIT,      discListener,     Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLAYER_KICK,      discListener,     Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLAYER_JOIN,      discListener,     Priority.Normal,  this);
        pm.registerEvent(Event.Type.BLOCK_BREAK,      blockListener,    Priority.Normal,  this);
        pm.registerEvent(Event.Type.BLOCK_PLACE,      blockListener,    Priority.Normal,  this);
        pm.registerEvent(Event.Type.SIGN_CHANGE,      blockListener,    Priority.Normal,  this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGE,    damageListener,   Priority.Normal,  this);
        pm.registerEvent(Event.Type.ENTITY_DEATH,     damageListener,   Priority.Normal,  this);
        pm.registerEvent(Event.Type.CREATURE_SPAWN,   monsterListener,  Priority.Normal,  this);
        pm.registerEvent(Event.Type.ENTITY_EXPLODE,   monsterListener,  Priority.Normal,  this);
        pm.registerEvent(Event.Type.ENTITY_COMBUST,   monsterListener,  Priority.Normal,  this);
        pm.registerEvent(Event.Type.ENTITY_TARGET,    monsterListener,  Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLUGIN_ENABLE,    serverListener,   Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLUGIN_DISABLE,   serverListener,   Priority.Monitor, this);
        
        System.out.println(Translation.tr("enableMessage", pdfFile.getName(), pdfFile.getVersion()));
    }

	private void setupTranslations() {			
		File configFile = TCUtils.getConfig("config");
		language = TCUtils.getString(configFile, "settings.language", "en-US");
		Translation.reload(new File(getDataFolder(), "templecraft-"+language+".csv"));
		
		if(Translation.getVersion()<1){
			TCUtils.copyFromJarToDisk("templecraft-"+language+".csv", getDataFolder());
			log.log(Level.INFO, "[TempleCraft] copied new translation file for "+language+" to disk.");
			Translation.reload(new File(getDataFolder(), "templecraft-"+language+".csv"));
		}
	}

	public void onDisable()
    {    
    	permissionHandler = null;
    	TempleManager.SBManager.save();
        TempleManager.removeAll();
        TCUtils.deleteTempWorlds();
        TCUtils.cleanConfigFiles();
    }
    
    private void setupPermissions() {
        if (permissionHandler != null) {
            return;
        }
        
        Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
        
        if (permissionsPlugin == null) {
            log.info(Translation.tr("permissionsUnavailable"));
            return;
        }
        
        permissionHandler = ((Permissions) permissionsPlugin).getHandler();
        log.info(Translation.tr("permissionsFound",((Permissions)permissionsPlugin).getDescription().getFullName()));
    }
    
    private void setupHeroes()
    {
        Plugin heroes = this.getServer().getPluginManager().getPlugin("Heroes");
        if (heroes == null)
        	return;
        
        heroManager = ((Heroes) heroes).getHeroManager();
    }
    
    public File getPluginFile(){
    	return getFile();
    }
}