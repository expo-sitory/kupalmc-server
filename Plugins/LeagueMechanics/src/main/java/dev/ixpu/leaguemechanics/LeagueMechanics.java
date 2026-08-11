package dev.ixpu.leaguemechanics;

import dev.ixpu.leaguemechanics.listener.PlayerEventListener;
import dev.ixpu.leaguemechanics.listener.RuneListener;

import dev.ixpu.leaguemechanics.manager.RuneManager;
import dev.ixpu.leaguemechanics.manager.ItemStatsManager;

import dev.ixpu.leaguemechanics.command.CommandHandler;
import dev.ixpu.leaguemechanics.command.CommandTabCompletions;

import dev.ixpu.leaguemechanics.util.ItemStatHelper;
import dev.ixpu.leaguemechanics.util.RunePersistence;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RuneRegistry;

import dev.ixpu.leaguemechanics.rune.keystones.precision.Conqueror;
import dev.ixpu.leaguemechanics.rune.keystones.precision.FleetFootwork;
import dev.ixpu.leaguemechanics.rune.keystones.precision.LethalTempo;
import dev.ixpu.leaguemechanics.rune.keystones.precision.PressTheAttack;

import dev.ixpu.leaguemechanics.rune.keystones.domination.DarkHarvest;
import dev.ixpu.leaguemechanics.rune.keystones.domination.Electrocute;
import dev.ixpu.leaguemechanics.rune.keystones.domination.HailOfBlades;

import dev.ixpu.leaguemechanics.rune.keystones.resolve.GraspOfTheUndying;
import dev.ixpu.leaguemechanics.rune.keystones.resolve.AfterShock;
import dev.ixpu.leaguemechanics.rune.keystones.resolve.Guardian;

import dev.ixpu.leaguemechanics.rune.keystones.sorcery.ArcaneComet;
import dev.ixpu.leaguemechanics.rune.keystones.sorcery.StormRaiderSurge;
import dev.ixpu.leaguemechanics.rune.keystones.sorcery.DeathfireTorch;

import dev.ixpu.leaguemechanics.rune.keystones.inspiration.GlacialAugment;
import dev.ixpu.leaguemechanics.rune.keystones.inspiration.FirstStrike;


public class LeagueMechanics extends JavaPlugin {
    private static LeagueMechanics instance;
    private RuneRegistry runeRegistry;
    private RuneManager runeManager;
    private ItemStatsManager itemStatsManager;
    private RunePersistence runePersistence;
    private boolean debugMode;

    @Override
    public void onEnable() {
        instance = this;
        runeRegistry = RuneRegistry.getInstance();
        runeManager = new RuneManager(this);
        itemStatsManager = new ItemStatsManager();
        runePersistence = new RunePersistence(this);

        getLogger().info("League Mechanics is starting...");

        saveDefaultConfig();
        reloadConfig();
        debugMode = getConfig().getBoolean("debug", false);

        ItemStatHelper.initialize(this);
        registerCommands();
        registerRunes();

        Bukkit.getPluginManager().registerEvents(new RuneListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerEventListener(this, runePersistence), this);

        startRuneTicker();

        getLogger().info("League Mechanics has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("League Mechanics is shutting down...");

        if (runeRegistry != null) {
            runeRegistry.clearRegistry();
        }

        getLogger().info("League Mechanics has been disabled!");
    }

    private void startRuneTicker() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (runeManager != null) {
                runeManager.tickAllPlayerRunes();
            }
        }, 0L, 1L);
    }

    public static LeagueMechanics getInstance() {
        return instance;
    }

    public RuneRegistry getRuneRegistry() {
        return runeRegistry;
    }

    public RuneManager getRuneManager() {
        return runeManager;
    }

    public ItemStatsManager getStatsManager() {
        return itemStatsManager;
    }

    public RunePersistence getRunePersistence() {
        return runePersistence;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void reloadPlugin() {
        getLogger().info("Reloading LeagueMechanics configuration...");

        reloadConfig();
        debugMode = getConfig().getBoolean("debug", false);

        if (runeRegistry != null) {
            runeRegistry.clearRegistry();
        }
        registerRunes();

        for (Player player : Bukkit.getOnlinePlayers()) {
            runeManager.reloadPlayerRunes(player);
        }

        getLogger().info("LeagueMechanics reload completed!");
    }

    private void registerCommands() {
        CommandHandler commandExecutor = new CommandHandler(this, itemStatsManager, runeManager, runePersistence);
        CommandTabCompletions tabCompleter = new CommandTabCompletions(runeRegistry);
        getCommand("leaguemechanics").setExecutor(commandExecutor);
        getCommand("leaguemechanics").setTabCompleter(tabCompleter);
        getLogger().info("Commands registered!");
    }

    private void registerRunes() {
        FileConfiguration config = getConfig();

        PressTheAttack pressTheAttack = new PressTheAttack(config);
        loadRuneCooldown(pressTheAttack, "runes.keystones.precision.press-the-attack.cooldown");
        runeRegistry.registerRune(pressTheAttack);

        LethalTempo lethalTempo = new LethalTempo(config);
        loadRuneCooldown(lethalTempo, "runes.keystones.precision.lethal-tempo.cooldown");
        runeRegistry.registerRune(lethalTempo);

        Conqueror conqueror = new Conqueror(config);
        loadRuneCooldown(conqueror, "runes.keystones.precision.conqueror.cooldown");
        runeRegistry.registerRune(conqueror);

        FleetFootwork fleetFootwork = new FleetFootwork(config);
        loadRuneCooldown(fleetFootwork, "runes.keystones.precision.fleet-footwork.cooldown");
        runeRegistry.registerRune(fleetFootwork);

        Electrocute electrocute = new Electrocute(config);
        loadRuneCooldown(electrocute, "runes.keystones.domination.electrocute.cooldown");
        runeRegistry.registerRune(electrocute);

        DarkHarvest darkHarvest = new DarkHarvest(config);
        loadRuneCooldown(darkHarvest, "runes.keystones.domination.dark-harvest.cooldown");
        runeRegistry.registerRune(darkHarvest);

        HailOfBlades hailOfBlades = new HailOfBlades(config);
        loadRuneCooldown(hailOfBlades, "runes.keystones.domination.hail-of-blades.cooldown");
        runeRegistry.registerRune(hailOfBlades);

        GraspOfTheUndying grasp = new GraspOfTheUndying(config);
        loadRuneCooldown(grasp, "runes.keystones.resolve.grasp-of-the-undying.cooldown");
        runeRegistry.registerRune(grasp);

        AfterShock aftershock = new AfterShock(config);
        loadRuneCooldown(aftershock, "runes.keystones.resolve.aftershock.cooldown");
        runeRegistry.registerRune(aftershock);

        Guardian guardian = new Guardian(config);
        loadRuneCooldown(guardian, "runes.keystones.resolve.guardian.cooldown");
        runeRegistry.registerRune(guardian);

        ArcaneComet arcaneComet = new ArcaneComet(config, this);
        loadRuneCooldown(arcaneComet, "runes.keystones.sorcery.arcane-comet.cooldown");
        runeRegistry.registerRune(arcaneComet);

        StormRaiderSurge stormRaiderSurge = new StormRaiderSurge(config);
        loadRuneCooldown(stormRaiderSurge, "runes.keystones.sorcery.storm-raider-surge.cooldown");
        runeRegistry.registerRune(stormRaiderSurge);

        DeathfireTorch deathfireTorch = new DeathfireTorch(config);
        runeRegistry.registerRune(deathfireTorch);

        GlacialAugment glacialAugment = new GlacialAugment(config, this);
        loadRuneCooldown(glacialAugment, "runes.keystones.inspiration.glacial-augment.cooldown");
        runeRegistry.registerRune(glacialAugment);

        FirstStrike firstStrike = new FirstStrike(config, this);
        loadRuneCooldown(firstStrike, "runes.keystones.inspiration.first-strike.cooldown");
        runeRegistry.registerRune(firstStrike);

        getLogger().info(() -> "Registered " + runeRegistry.getAllRunes().size() + " runes");
    }

    private void loadRuneCooldown(BaseRune rune, String configPath) {
        if (getConfig().contains(configPath)) {
            double cooldown = getConfig().getDouble(configPath);
            rune.setCooldownSeconds(cooldown);
            getLogger().info(() -> "Loaded cooldown for " + rune.getId() + ": " + cooldown + "s");
        }
    }
}