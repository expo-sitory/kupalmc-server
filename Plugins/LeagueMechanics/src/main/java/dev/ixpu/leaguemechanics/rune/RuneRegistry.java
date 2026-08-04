package dev.ixpu.leaguemechanics.rune;

import java.util.HashMap;
import java.util.Map;

public class RuneRegistry {
    private static final RuneRegistry instance = new RuneRegistry();
    private final Map<String, BaseRune> runes = new HashMap<>();

    private RuneRegistry() {}

    public static RuneRegistry getInstance() {
        return instance;
    }

    public void registerRune(BaseRune rune) {
        runes.put(rune.getId(), rune);
    }

    public BaseRune getRune(String id) {
        return runes.get(id);
    }

    public boolean containsRune(String id) {
        return runes.containsKey(id);
    }

    public Map<String, BaseRune> getAllRunes() {
        return new HashMap<>(runes);
    }

    public void clearRegistry() {
        runes.clear();
    }
}
