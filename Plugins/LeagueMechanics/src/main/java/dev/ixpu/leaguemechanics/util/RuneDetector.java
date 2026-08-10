package dev.ixpu.leaguemechanics.util;

import dev.ixpu.leaguemechanics.player.PlayerRuneData;
import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneRegistry;
import org.bukkit.entity.Player;

public class RuneDetector {
    private static final RuneRegistry registry = RuneRegistry.getInstance();

    private static boolean hasPermission(Player player, String permission) {
        // Save current OP status
        boolean wasOp = player.isOp();
        
        try {
            player.setOp(false);
            boolean hasIt = player.hasPermission(permission);
            return hasIt;
        } finally {
            player.setOp(wasOp);
        }
    }

    public static PlayerRuneData detectPlayerRunes(Player player) {
        PlayerRuneData data = new PlayerRuneData(player);

        // Detect primary path
        RunePath primaryPath = detectPrimaryPath(player);
        data.setPrimaryPath(primaryPath);

        // Detect secondary path
        RunePath secondaryPath = detectSecondaryPath(player);
        data.setSecondaryPath(secondaryPath);

        // Detect keystone
        BaseRune keystone = detectKeystone(player);
        data.setKeystoneRune(keystone);

        // Detect primary runes
        BaseRune primarySlot1 = detectPrimarySlot1(player);
        data.setPrimarySlot1Rune(primarySlot1);

        BaseRune primarySlot2 = detectPrimarySlot2(player);
        data.setPrimarySlot2Rune(primarySlot2);

        BaseRune primarySlot3 = detectPrimarySlot3(player);
        data.setPrimarySlot3Rune(primarySlot3);

        // Detect secondary runes
        BaseRune secondarySlot1 = detectSecondarySlot1(player);
        data.setSecondarySlot1Rune(secondarySlot1);

        BaseRune secondarySlot2 = detectSecondarySlot2(player);
        data.setSecondarySlot2Rune(secondarySlot2);

        return data;
    }

    private static RunePath detectPrimaryPath(Player player) {
        for (RunePath path : RunePath.values()) {
            if (hasPermission(player, "primary-rune-path." + path.getId())) {
                return path;
            }
        }
        return null;
    }

    private static RunePath detectSecondaryPath(Player player) {
        for (RunePath path : RunePath.values()) {
            if (hasPermission(player, "secondary-rune-path." + path.getId())) {
                return path;
            }
        }
        return null;
    }

    private static BaseRune detectKeystone(Player player) {
        String[] keystones = {
            "press-the-attack", "lethal-tempo", "fleet-footwork", "conqueror",
            "electrocute", "dark-harvest", "hail-of-blades",
            "grasp-of-the-undying", "aftershock", "guardian",
            "summon-aery", "arcane-comet", "storm-raider-surge", "deathfire-torch",
            "glacial-augment", "unsealed-spellbook", "first-strike"
        };

        for (String keystoneId : keystones) {
            if (hasPermission(player, "rune-keystone." + keystoneId)) {
                return registry.getRune(keystoneId);
            }
        }
        return null;
    }

    private static BaseRune detectPrimarySlot1(Player player) {
        String[] runes = {
            "absorb-life", "triump", "presence-of-mind",
            "cheap-shot", "taste-of-blood", "sudden-impact",
            "demolish", "font-of-life", "shield-bash",
            "axiom-arcanist", "manaflow-band", "nimbus-cloak",
            "hextech-flashtraption", "magical-footwear", "cash-back"
        };

        for (String runeId : runes) {
            if (hasPermission(player, "primary-rune.slot-1." + runeId)) {
                return registry.getRune(runeId);
            }
        }
        return null;
    }

    private static BaseRune detectPrimarySlot2(Player player) {
        String[] runes = {
            "legend-alacrity", "legend-haste", "legend-bloodline",
            "six-sense", "grisly-mementos", "deep-ward",
            "conditioning", "second-wind", "bone-plating",
            "trascendence", "celerity", "absolute-focus",
            "tripple-tonic", "time-warp-tonic", "biscuit-delivery"
        };

        for (String runeId : runes) {
            if (hasPermission(player, "primary-rune.slot-2." + runeId)) {
                return registry.getRune(runeId);
            }
        }
        return null;
    }

    private static BaseRune detectPrimarySlot3(Player player) {
        String[] runes = {
            "coup-de-grace", "cut-down", "last-stand",
            "treasure-hunter", "relentless-hunter", "ultimate-hunter",
            "overgrowth", "revitalize", "unflinching",
            "scorch", "water-walking", "gathering-storm",
            "cosmic-insight", "approach-velocity", "jack-of-all-trades"
        };

        for (String runeId : runes) {
            if (hasPermission(player, "primary-rune.slot-3." + runeId)) {
                return registry.getRune(runeId);
            }
        }
        return null;
    }

    private static BaseRune detectSecondarySlot1(Player player) {
        String[] runes = {
            "absorb-life", "triump", "presence-of-mind",
            "legend-alacrity", "legend-haste", "legend-bloodline",
            "coup-de-grace", "cut-down", "last-stand",
            "cheap-shot", "taste-of-blood", "sudden-impact",
            "six-sense", "grisly-mementos", "deep-ward",
            "treasure-hunter", "relentless-hunter", "ultimate-hunter",
            "demolish", "font-of-life", "shield-bash",
            "conditioning", "second-wind", "bone-plating",
            "overgrowth", "revitalize", "unflinching",
            "axiom-arcanist", "manaflow-band", "nimbus-cloak",
            "trascendence", "celerity", "absolute-focus",
            "scorch", "water-walking", "gathering-storm",
            "hextech-flashtraption", "magical-footwear", "cash-back",
            "tripple-tonic", "time-warp-tonic", "biscuit-delivery",
            "cosmic-insight", "approach-velocity", "jack-of-all-trades"
        };

        for (String runeId : runes) {
            if (hasPermission(player, "secondary-rune.slot-1." + runeId)) {
                return registry.getRune(runeId);
            }
        }
        return null;
    }

    private static BaseRune detectSecondarySlot2(Player player) {
        String[] runes = {
            "absorb-life", "triump", "presence-of-mind",
            "legend-alacrity", "legend-haste", "legend-bloodline",
            "coup-de-grace", "cut-down", "last-stand",
            "cheap-shot", "taste-of-blood", "sudden-impact",
            "six-sense", "grisly-mementos", "deep-ward",
            "treasure-hunter", "relentless-hunter", "ultimate-hunter",
            "demolish", "font-of-life", "shield-bash",
            "conditioning", "second-wind", "bone-plating",
            "overgrowth", "revitalize", "unflinching",
            "axiom-arcanist", "manaflow-band", "nimbus-cloak",
            "trascendence", "celerity", "absolute-focus",
            "scorch", "water-walking", "gathering-storm",
            "hextech-flashtraption", "magical-footwear", "cash-back",
            "tripple-tonic", "time-warp-tonic", "biscuit-delivery",
            "cosmic-insight", "approach-velocity", "jack-of-all-trades"
        };

        for (String runeId : runes) {
            if (hasPermission(player, "secondary-rune.slot-2." + runeId)) {
                return registry.getRune(runeId);
            }
        }
        return null;
    }
}
