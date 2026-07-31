package dev.ixpu.leaguerunes;

import dev.ixpu.leaguerunes.listener.RuneListener;
import dev.ixpu.leaguerunes.listener.PlayerEventListener;
import dev.ixpu.leaguerunes.rune.RuneRegistry;
import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.rune.keystones.precision.PressTheAttack;
import dev.ixpu.leaguerunes.rune.keystones.precision.LethalTempo;
import dev.ixpu.leaguerunes.rune.keystones.precision.Conqueror;
import dev.ixpu.leaguerunes.rune.keystones.precision.FleetFootwork;
import dev.ixpu.leaguerunes.rune.keystones.domination.Electrocute;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LeagueRunes extends JavaPlugin {
    private static LeagueRunes instance;
    private RuneRegistry runeRegistry;
    private RuneManager runeManager;

    @Override
    public void onEnable() {
        instance = this;
        runeRegistry = RuneRegistry.getInstance();
        runeManager = new RuneManager(this);

        getLogger().info("LeagueRunes is starting...");

        // Load configuration
        saveDefaultConfig();
        reloadConfig();

        // Register all runes
        registerRunes();

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new RuneListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerEventListener(this), this);

        // Start the main rune tick task
        startRuneTicker();

        getLogger().info("LeagueRunes has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LeagueRunes is shutting down...");

        if (runeRegistry != null) {
            runeRegistry.clearRegistry();
        }

        getLogger().info("LeagueRunes has been disabled!");
    }

    private void startRuneTicker() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (runeManager != null) {
                runeManager.tickAllPlayerRunes();
            }
        }, 0L, 1L); // Runs every tick
    }

    public static LeagueRunes getInstance() {
        return instance;
    }

    public RuneRegistry getRuneRegistry() {
        return runeRegistry;
    }

    public RuneManager getRuneManager() {
        return runeManager;
    }

    private void registerRunes() {
        // Register Keystones - Precision
        PressTheAttack pressTheAttack = new PressTheAttack();
        loadRuneCooldown(pressTheAttack, "keystones.precision.press-the-attack");
        runeRegistry.registerRune(pressTheAttack);

        LethalTempo lethalTempo = new LethalTempo();
        loadRuneCooldown(lethalTempo, "keystones.precision.lethal-tempo");
        runeRegistry.registerRune(lethalTempo);

        Conqueror conqueror = new Conqueror();
        loadRuneCooldown(conqueror, "keystones.precision.conqueror");
        runeRegistry.registerRune(conqueror);

        FleetFootwork fleetFootwork = new FleetFootwork();
        loadRuneCooldown(fleetFootwork, "keystones.precision.fleet-footwork");
        runeRegistry.registerRune(fleetFootwork);

        // Register Keystones - Domination
        Electrocute electrocute = new Electrocute();
        loadRuneCooldown(electrocute, "keystones.domination.electrocute");
        runeRegistry.registerRune(electrocute);

        // TODO: Register other keystones
        // TODO: Register slot runes

        getLogger().info(() -> "Registered " + runeRegistry.getAllRunes().size() + " runes");
    }


    // Load cooldown from config for a rune
    private void loadRuneCooldown(BaseRune rune, String configPath) {
        if (getConfig().contains(configPath)) {
            double cooldown = getConfig().getDouble(configPath);
            rune.setCooldownSeconds(cooldown);
            getLogger().info(() -> "Loaded cooldown for " + rune.getId() + ": " + cooldown + "s");
        }
    }
}
