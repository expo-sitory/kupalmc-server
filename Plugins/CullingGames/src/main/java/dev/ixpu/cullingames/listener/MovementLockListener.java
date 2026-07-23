package dev.ixpu.cullingames.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import dev.ixpu.cullingames.manager.EventManager;

public class MovementLockListener implements Listener {

    private final EventManager eventManager;

    public MovementLockListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!eventManager.isActive() || !eventManager.isPaused()) return;
        
        if (!eventManager.isParticipant(e.getPlayer().getUniqueId())) return;

        if (e.getPlayer().isOp()) return;

        if (e.getFrom().getX() == e.getTo().getX() &&
            e.getFrom().getY() == e.getTo().getY() &&
            e.getFrom().getZ() == e.getTo().getZ()) {
            return;
        }

        e.setCancelled(true);
    }
}