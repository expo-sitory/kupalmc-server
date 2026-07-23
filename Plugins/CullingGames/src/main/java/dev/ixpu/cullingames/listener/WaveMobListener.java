package dev.ixpu.cullingames.listener;

import dev.ixpu.cullingames.manager.EventManager;
import dev.ixpu.cullingames.wave.WaveManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTransformEvent;

import java.time.Duration;

public class WaveMobListener implements Listener {

    private final EventManager eventManager;

    public WaveMobListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @EventHandler
    public void onWaveMobDeath(EntityDeathEvent e) {
        if (!eventManager.isActive()) return;

        Player killer = e.getEntity().getKiller();
        if (killer == null) return;

        if (!eventManager.isParticipant(killer.getUniqueId())) return;

        WaveManager waveManager = eventManager.getWaveManager();
        
        if (!waveManager.isWaveMob(e.getEntity().getUniqueId())) {
            return;
        }

        int points = waveManager.getPointsForMob(e.getEntity().getUniqueId());
        
        eventManager.addPoints(killer.getUniqueId(), points);
        eventManager.incrementMobKill(killer.getUniqueId());

        Title title = Title.title(
            Component.text("+" + points + " wave kill", NamedTextColor.LIGHT_PURPLE),
            Component.empty(),
            Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(1000), Duration.ofMillis(500))
        );
        killer.showTitle(title);
    }

    @EventHandler
    public void onPiglinBruteTransform(EntityTransformEvent event) {
        if (!(event.getEntity() instanceof PiglinBrute)) return;
        
        WaveManager waveManager = eventManager.getWaveManager();
        if (!waveManager.isWaveMob(event.getEntity().getUniqueId())) return;
        
        PiglinBrute brute = (PiglinBrute) event.getEntity();
        
        org.bukkit.inventory.EntityEquipment equipment = brute.getEquipment();
        org.bukkit.inventory.ItemStack mainHand = equipment.getItemInMainHand().clone();
        org.bukkit.inventory.ItemStack offHand = equipment.getItemInOffHand().clone();
        org.bukkit.inventory.ItemStack helmet = equipment.getHelmet() != null ? equipment.getHelmet().clone() : null;
        org.bukkit.inventory.ItemStack chestplate = equipment.getChestplate() != null ? equipment.getChestplate().clone() : null;
        org.bukkit.inventory.ItemStack leggings = equipment.getLeggings() != null ? equipment.getLeggings().clone() : null;
        org.bukkit.inventory.ItemStack boots = equipment.getBoots() != null ? equipment.getBoots().clone() : null;
        
        event.setCancelled(true);
        
        equipment.setItemInMainHand(mainHand);
        equipment.setItemInOffHand(offHand);
        equipment.setHelmet(helmet);
        equipment.setChestplate(chestplate);
        equipment.setLeggings(leggings);
        equipment.setBoots(boots);
    }
}