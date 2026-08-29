package dev.ixpu.itemsorigin;

import dev.ixpu.itemsorigin.listener.PlayerEventListener;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemsOrigin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);
        
        getLogger().info("ItemOrigin plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ItemOrigin plugin has been disabled!");
    }
}