package dev.ixpu.leaguemechanics.player;

import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import org.bukkit.entity.Player;

public class PlayerRuneData {
    private final Player player;
    private RunePath primaryPath;
    private RunePath secondaryPath;
    private BaseRune keystoneRune;
    private BaseRune primarySlot1Rune;
    private BaseRune primarySlot2Rune;
    private BaseRune primarySlot3Rune;
    private BaseRune secondarySlot1Rune;
    private BaseRune secondarySlot2Rune;

    public PlayerRuneData(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public RunePath getPrimaryPath() {
        return primaryPath;
    }

    public void setPrimaryPath(RunePath path) {
        this.primaryPath = path;
    }

    public RunePath getSecondaryPath() {
        return secondaryPath;
    }

    public void setSecondaryPath(RunePath path) {
        this.secondaryPath = path;
    }

    public BaseRune getKeystoneRune() {
        return keystoneRune;
    }

    public void setKeystoneRune(BaseRune rune) {
        this.keystoneRune = rune;
    }

    public BaseRune getPrimarySlot1Rune() {
        return primarySlot1Rune;
    }

    public void setPrimarySlot1Rune(BaseRune rune) {
        this.primarySlot1Rune = rune;
    }

    public BaseRune getPrimarySlot2Rune() {
        return primarySlot2Rune;
    }

    public void setPrimarySlot2Rune(BaseRune rune) {
        this.primarySlot2Rune = rune;
    }

    public BaseRune getPrimarySlot3Rune() {
        return primarySlot3Rune;
    }

    public void setPrimarySlot3Rune(BaseRune rune) {
        this.primarySlot3Rune = rune;
    }

    public BaseRune getSecondarySlot1Rune() {
        return secondarySlot1Rune;
    }

    public void setSecondarySlot1Rune(BaseRune rune) {
        this.secondarySlot1Rune = rune;
    }

    public BaseRune getSecondarySlot2Rune() {
        return secondarySlot2Rune;
    }

    public void setSecondarySlot2Rune(BaseRune rune) {
        this.secondarySlot2Rune = rune;
    }

    public BaseRune[] getAllRunes() {
        return new BaseRune[] {
            keystoneRune,
            primarySlot1Rune,
            primarySlot2Rune,
            primarySlot3Rune,
            secondarySlot1Rune,
            secondarySlot2Rune
        };
    }
}
