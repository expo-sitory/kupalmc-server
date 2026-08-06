package dev.ixpu.leaguemechanics.manager;

import dev.ixpu.leaguemechanics.player.PlayerStats;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DamageManager {

    protected boolean isAdaptive = false;
    protected boolean isPerStack = false;

    protected final Map<UUID, Double> calculatedAP = new HashMap<>();
    protected final Map<UUID, Double> calculatedAD = new HashMap<>();

    public void enableAdaptiveScaling() {
        this.isAdaptive = true;
    }
    public void enablePerStackScaling() {
        this.isPerStack = true;
    }


    public double totalBonusDamage(Player player, Entity target, int currentStacks) {
        PlayerStats stats = new PlayerStats();
        UUID uuid = player.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        double playerBonusAD = stats.getAttackerAD(player);
       // double playerBonusAP = stats.getAttakerAP(player);
        double targetPhysicalArmor = stats.getTargetAR((LivingEntity) target);
       // double playerBonusMagicResist = stats.getTargetMR(player);

        double totalAD = (playerBonusAD - targetPhysicalArmor) / 2;
        calculatedAD.put(uuid, totalAD);

        if (isPerStack) {
            return totalAD * currentStacks;
        }
        else {
            return totalAD;
        }

    }

    public double levelBasedBonus(Player player) {
        double playerLevel = player.getLevel();
        if (playerLevel >= 100) {
            return 7.5;
        } else if (playerLevel >= 70) {
            return 4.5;
        } else if (playerLevel >= 40) {
            return 3.5;
        } else if (playerLevel >= 10) {
            return 1.5;
        } else {
            return 0.5;
        }
    }
}