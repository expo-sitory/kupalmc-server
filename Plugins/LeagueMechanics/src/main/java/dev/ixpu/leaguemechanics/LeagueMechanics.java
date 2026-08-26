package dev.ixpu.leaguemechanics;

import dev.ixpu.leaguemechanics.listener.PlayerEventListener;

import dev.ixpu.leaguemechanics.manager.RuneManager;
import dev.ixpu.leaguemechanics.manager.ItemStatsManager;
import dev.ixpu.leaguemechanics.manager.ItemShopManager;

import dev.ixpu.leaguemechanics.command.CommandHandler;
import dev.ixpu.leaguemechanics.command.CommandTabCompletions;

import dev.ixpu.leaguemechanics.placeholder.PlaceholderRegistry;
import dev.ixpu.leaguemechanics.util.ItemModifier;
import dev.ixpu.leaguemechanics.util.RunePersistence;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;

import dev.ixpu.leaguemechanics.rune.CooldownHandler;
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
    private PlayerEventListener playerEventListener;
    private boolean debugMode;

    @Override
    public void onEnable() {
        instance = this;
        runeRegistry = RuneRegistry.getInstance();
        runeManager = new RuneManager(this);
        itemStatsManager = new ItemStatsManager();
        runePersistence = new RunePersistence(this);
        playerEventListener = new PlayerEventListener(this, runePersistence);


        getLogger().info("League Mechanics is starting...");

        saveDefaultConfig();
        reloadConfig();
        debugMode = getConfig().getBoolean("debug", false);

        ItemModifier.initialize(this);
        registerRunes();
        registerCommands();
        registerRegenTask();
        registerHotbarCleanupTask();

        Bukkit.getPluginManager().registerEvents(playerEventListener, this);
        Bukkit.getPluginManager().registerEvents(ItemShopManager.getInstance(), this);

        startRuneTicker();
        registerPlaceholders();

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
            runeManager.loadPlayerRunes(player);

            String keystoneRuneId = runePersistence.loadKeystoneRune(player.getUniqueId());
            if (keystoneRuneId != null) {
                CooldownHandler keystone = runeRegistry.getRune(keystoneRuneId);
                if (keystone != null) {
                    runeManager.setPlayerKeystoneRune(player, keystone);
                }
            }
        }

        getLogger().info("LeagueMechanics reload completed!");
    }

    private void registerCommands() {
        CommandHandler commandExecutor = new CommandHandler(this, itemStatsManager, runeManager, runePersistence, playerEventListener);
        CommandTabCompletions tabCompleter = new CommandTabCompletions(runeRegistry);
        getCommand("leaguemechanics").setExecutor(commandExecutor);
        getCommand("leaguemechanics").setTabCompleter(tabCompleter);
        getLogger().info("Commands registered!");
    }

    public void registerRegenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStatsManager itemStatsManager = getStatsManager();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    double healthRegen = itemStatsManager.getItemHR(player);
                    double saturationRegen = itemStatsManager.getItemSR(player);

                    if (healthRegen > 0) {
                        double newHealth = Math.min(player.getHealth() + healthRegen, player.getMaxHealth());
                        player.setHealth(newHealth);
                    }

                    if (saturationRegen > 0) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, (int) saturationRegen, false, false));
                    }
                }
            }
        }.runTaskTimer(this, 0, 100);
    }

    public void registerHotbarCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack[] inventoryContents = player.getInventory().getContents();
                    for (int i = 0; i < 9; i++) {
                        ItemStack item = inventoryContents[i];
                        if (item != null && !item.getType().isAir() && ItemModifier.getItemId(item) != null) {
                            playerEventListener.moveLeagueItemToMainInventory(player, item);
                            player.getInventory().setItem(i, null);
                        }
                    }
                    if (inventoryContents[40] != null && !inventoryContents[40].getType().isAir() && ItemModifier.getItemId(inventoryContents[40]) != null) {
                        playerEventListener.moveLeagueItemToMainInventory(player, inventoryContents[40]);
                        player.getInventory().setItem(40, null);
                    }
                }
            }
        }.runTaskTimer(this, 0, 10L);
    }

    private void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderRegistry(this).register();
            getLogger().info("PlaceholderAPI placeholders registered!");
        } else {
            getLogger().warning("PlaceholderAPI not found! Placeholders will not work.");
        }
    }

    private void registerRunes() {
        FileConfiguration config = getConfig();

        PressTheAttack pressTheAttack = new PressTheAttack(config, playerEventListener);
        loadRuneCooldown(pressTheAttack, "runes.keystones.precision.press-the-attack.cooldown");
        runeRegistry.registerRune(pressTheAttack);

        LethalTempo lethalTempo = new LethalTempo(config, playerEventListener);
        loadRuneCooldown(lethalTempo, "runes.keystones.precision.lethal-tempo.cooldown");
        runeRegistry.registerRune(lethalTempo);

        Conqueror conqueror = new Conqueror(config, playerEventListener);
        loadRuneCooldown(conqueror, "runes.keystones.precision.conqueror.cooldown");
        runeRegistry.registerRune(conqueror);

        FleetFootwork fleetFootwork = new FleetFootwork(config);
        loadRuneCooldown(fleetFootwork, "runes.keystones.precision.fleet-footwork.cooldown");
        runeRegistry.registerRune(fleetFootwork);

        Electrocute electrocute = new Electrocute(config, playerEventListener);
        loadRuneCooldown(electrocute, "runes.keystones.domination.electrocute.cooldown");
        runeRegistry.registerRune(electrocute);

        DarkHarvest darkHarvest = new DarkHarvest(config, playerEventListener);
        loadRuneCooldown(darkHarvest, "runes.keystones.domination.dark-harvest.cooldown");
        runeRegistry.registerRune(darkHarvest);

        HailOfBlades hailOfBlades = new HailOfBlades(config, playerEventListener);
        loadRuneCooldown(hailOfBlades, "runes.keystones.domination.hail-of-blades.cooldown");
        runeRegistry.registerRune(hailOfBlades);

        GraspOfTheUndying grasp = new GraspOfTheUndying(config, playerEventListener);
        loadRuneCooldown(grasp, "runes.keystones.resolve.grasp-of-the-undying.cooldown");
        runeRegistry.registerRune(grasp);

        AfterShock aftershock = new AfterShock(config, playerEventListener);
        loadRuneCooldown(aftershock, "runes.keystones.resolve.aftershock.cooldown");
        runeRegistry.registerRune(aftershock);

        Guardian guardian = new Guardian(config);
        loadRuneCooldown(guardian, "runes.keystones.resolve.guardian.cooldown");
        runeRegistry.registerRune(guardian);

        ArcaneComet arcaneComet = new ArcaneComet(config, this, playerEventListener);
        loadRuneCooldown(arcaneComet, "runes.keystones.sorcery.arcane-comet.cooldown");
        runeRegistry.registerRune(arcaneComet);

        StormRaiderSurge stormRaiderSurge = new StormRaiderSurge(config, playerEventListener);
        loadRuneCooldown(stormRaiderSurge, "runes.keystones.sorcery.storm-raider-surge.cooldown");
        runeRegistry.registerRune(stormRaiderSurge);

        DeathfireTorch deathfireTorch = new DeathfireTorch(config, playerEventListener);
        runeRegistry.registerRune(deathfireTorch);

        GlacialAugment glacialAugment = new GlacialAugment(config, this);
        loadRuneCooldown(glacialAugment, "runes.keystones.inspiration.glacial-augment.cooldown");
        runeRegistry.registerRune(glacialAugment);

        FirstStrike firstStrike = new FirstStrike(config, this, playerEventListener);
        loadRuneCooldown(firstStrike, "runes.keystones.inspiration.first-strike.cooldown");
        runeRegistry.registerRune(firstStrike);

        getLogger().info(() -> "Registered " + runeRegistry.getAllRunes().size() + " runes");
    }

    private void loadRuneCooldown(CooldownHandler rune, String configPath) {
        if (getConfig().contains(configPath)) {
            double cooldown = getConfig().getDouble(configPath);
            rune.setCooldownSeconds(cooldown);
            getLogger().info(() -> "Loaded cooldown for " + rune.getId() + ": " + cooldown + "s");
        }
    }
}