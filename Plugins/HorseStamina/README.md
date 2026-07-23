# HorseStamina

A Minecraft plugin that adds uma racing mechanics with stamina management and minigames.

## What's it do?

- Adds a stamina system to horses based on health
- Random minigames trigger during races for stamina rewards
- Collision damage when horses ram into each other
- Stamina regeneration that scales based on current stamina levels
- Slowness effects apply when stamina gets too low

## Commands

- `/race` - Activate horse race mode
- `/horsestamina reload` - Reload plugin configuration

## Permissions

- `horsepower.race` - Allows players to use race mode (default: true)
- `horsepower.admin` - Allows operators to reload config (default: op)

## Configuration

Everything is customizable in config.yml:
- Stamina depletion rates per pace level
- Minigame settings and rewards
- Collision damage and range
- Regeneration rates by stamina threshold
