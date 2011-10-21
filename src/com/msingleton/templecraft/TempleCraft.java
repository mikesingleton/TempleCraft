package com.msingleton.templecraft;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

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
    public static final String[] COMMANDS = {"newgame", "join", "j", "leave", "l", "playerlist", "plist", "templelist", "tlist",
    										"gamelist","glist", "ready", "notready", "checkupdates", "forcestart", "forceend",
                                      		"new", "edit", "save", "reload"};
    private Logger log;
    public List<String> ENABLED_COMMANDS;
    public static Method method = null;
    public static PermissionHandler permissionHandler;
    public static String fileExtention = ".tcf";
    
    public TempleCraft()
    {
    }

    public void onEnable()
    {
        PluginDescriptionFile pdfFile = this.getDescription();
        
        log = getServer().getLogger();
        setupPermissions();
        // Initialize convenience variables in ArenaManager.
        TempleManager.init(this);
        ENABLED_COMMANDS = TCUtils.getEnabledCommands();
        
        // Bind the /tc and /tcraft commands to MACommands.
    	getCommand("tc").setExecutor(new TCCommands());
        
    	// Create event listeners.
        PluginManager pm = getServer().getPluginManager();
    	
        PlayerListener commandListener  = new TCEnabledCommands(this);
        PlayerListener playerListener   = new TCPlayerListener(this);
        PlayerListener maListener       = new MobArenaClasses(this);
        PlayerListener teleportListener = new TCTeleportListener(this);
        PlayerListener discListener     = new TCDisconnectListener(this);
        BlockListener  blockListener    = new TCBlockListener(this);
        EntityListener damageListener   = new TCDamageListener(this);
        EntityListener monsterListener  = new TCMonsterListener(this);
        ServerListener serverListener   = new TCServerListener(this);
        
        // TO-DO: PlayerListener to check for kills/deaths.
        
        // Register events.
    	
        pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, commandListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, playerListener,Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLAYER_MOVE,      playerListener,   Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT,  playerListener,   Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT,  maListener,       Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLAYER_TELEPORT,  teleportListener, Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLAYER_QUIT,      discListener,     Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLAYER_KICK,      discListener,     Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLAYER_JOIN,      discListener,     Priority.Normal,  this);
        pm.registerEvent(Event.Type.BLOCK_BREAK,      blockListener,    Priority.Normal,  this);
        pm.registerEvent(Event.Type.BLOCK_PLACE,      blockListener,    Priority.Normal,  this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGE,    damageListener,   Priority.Normal,  this);
        pm.registerEvent(Event.Type.ENTITY_DEATH,     damageListener,   Priority.Normal,  this);
        pm.registerEvent(Event.Type.CREATURE_SPAWN,   damageListener,   Priority.Normal,  this);
        pm.registerEvent(Event.Type.ENTITY_EXPLODE,   monsterListener,  Priority.Normal,  this);
        pm.registerEvent(Event.Type.ENTITY_COMBUST,   monsterListener,  Priority.Normal,  this);
        pm.registerEvent(Event.Type.ENTITY_TARGET,    monsterListener,  Priority.Normal,  this);
        pm.registerEvent(Event.Type.PLUGIN_ENABLE,    serverListener,   Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLUGIN_DISABLE,   serverListener,   Priority.Monitor, this);
        
        System.out.println(pdfFile.getName() + " v" + pdfFile.getVersion() + " enabled." );
    }
    
    
    public void onDisable()
    {    
    	permissionHandler = null;
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
            log.info("Permission system not detected, defaulting to OP");
            return;
        }
        
        permissionHandler = ((Permissions) permissionsPlugin).getHandler();
        log.info("Found and will use plugin "+((Permissions)permissionsPlugin).getDescription().getFullName());
    }
    
    public File getPluginFile(){
    	return getFile();
    }
}