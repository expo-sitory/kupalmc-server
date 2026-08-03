package dev.ixpu.leaguerunes;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import dev.ixpu.leaguerunes.command.LeagueRunesCommand;
import dev.ixpu.leaguerunes.listener.PlayerEventListener;
import dev.ixpu.leaguerunes.listener.RuneListener;
import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.rune.RuneRegistry;

import dev.ixpu.leaguerunes.rune.keystones.precision.Conqueror;
import dev.ixpu.leaguerunes.rune.keystones.precision.FleetFootwork;
import dev.ixpu.leaguerunes.rune.keystones.precision.LethalTempo;
import dev.ixpu.leaguerunes.rune.keystones.precision.PressTheAttack;

import dev.ixpu.leaguerunes.rune.keystones.domination.DarkHarvest;
import dev.ixpu.leaguerunes.rune.keystones.domination.Electrocute;
import dev.ixpu.leaguerunes.rune.keystones.domination.HailOfBlades;

import dev.ixpu.leaguerunes.rune.keystones.resolve.GraspOfTheUndying;
import dev.ixpu.leaguerunes.rune.keystones.resolve.AfterShock;
import dev.ixpu.leaguerunes.rune.keystones.resolve.Guardian;

import dev.ixpu.leaguerunes.rune.keystones.sorcery.ArcaneComet;
import dev.ixpu.leaguerunes.rune.keystones.sorcery.StormRaiderSurge;
import dev.ixpu.leaguerunes.rune.keystones.sorcery.DeathfireTorch;

import dev.ixpu.leaguerunes.rune.keystones.inspiration.GlacialAugment;


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

        saveDefaultConfig();
        reloadConfig();

        registerRunes();

        getCommand("leaguerunes").setExecutor(new LeagueRunesCommand(this));

        Bukkit.getPluginManager().registerEvents(new RuneListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerEventListener(this), this);

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
        }, 0L, 1L);
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
        darkHarvest.setPlugin(this);
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

        ArcaneComet arcaneComet = new ArcaneComet(config);
        arcaneComet.setPlugin(this);
        loadRuneCooldown(arcaneComet, "runes.keystones.sorcery.arcane-comet.cooldown");
        runeRegistry.registerRune(arcaneComet);

        StormRaiderSurge stormRaiderSurge = new StormRaiderSurge(config);
        stormRaiderSurge.setPlugin(this);
        loadRuneCooldown(stormRaiderSurge, "runes.keystones.sorcery.storm-raider-surge.cooldown");
        runeRegistry.registerRune(stormRaiderSurge);

        DeathfireTorch deathfireTorch = new DeathfireTorch(config);
        deathfireTorch.setPlugin(this);
        runeRegistry.registerRune(deathfireTorch);

        GlacialAugment glacialAugment = new GlacialAugment(config);
        glacialAugment.setPlugin(this);
        loadRuneCooldown(glacialAugment, "runes.keystones.inspiration.glacial-augment.cooldown");
        runeRegistry.registerRune(glacialAugment);

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