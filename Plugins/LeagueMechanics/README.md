# LeagueMechanics


## Features

**Rune System**: 14 keystones across 5 paths (Precision, Resolve, Domination, Sorcery, Inspiration).

**Item Stats Management**: Equip gear with custom stat values. 

  - **Current:** 
    - Attack Damage
    - Ability Power
    - Armor
    - Magic Resist
    - Health
    - Attack Speed
    - Movement Speed

**Adaptive Damage**: All damage scales dynamically based on your total AD/AP versus enemy AR/MR, choosing the most effective damage type automatically.


## Stat System

### Player Stats
Players accumulate stats from two sources:

1. **Base Stats** (hardcoded):
  - Attack Damage (AD): Base 1.0 + Weapon sharpness
  - Physical Armor (AR): Base 0.0 + Armor protection
  - Ability Power (AP): Base 12.0
  - Magic Resist (MR): Base 5.0

2. **Item Stats** (NBT-driven):
  - Bonus AD, AR, AP, MR
  - Bonus HP, AS (attack speed %), MS (movement speed %)
  - Health Regen (HR) and Saturation Regen (SR)


Each item's stats are stored in custom NBT tags and synced when equipped.

### Stat Conversion

For adaptive damage calculations:
- 1 AD = 0.25 HP equivalent
- 1 AP = 0.25 HP equivalent


## List of Current Items

**Stat Legend:**
- **AD**: Attack Damage
- **AP**: Ability Power
- **AR**: Armor
- **MR**: Magic Resist
- **HP**: Health
- **HR**: Health Regen per 15s
- **SR**: Saturation Regen per 25s
- **AS**: Attack Speed %
- **MS**: Movement Speed %

### Starter Items

Limitations: Limited to 1 **ꜱᴛᴀʀᴛᴇʀ** item

| Item | AD | AP | AR | MR | HP | HR |
|---|---|---|---|---|---|---|
| Doran's Blade | 10 | — | — | — | 8 | — |
| Doran's Bow | 8 | — | — | — | — | — |
| Doran's Helm | — | — | 8 | 8 | 10 | — |
| Doran's Ring | — | 18 | — | — | 9 | — |
| Doran's Shield | — | — | — | — | 11 | 0.5 |
| Dark Seal | — | 15 | — | — | 5 | — |
| Cull | 7 | — | — | — | — | — | — | — | — |



### Basic Items

For future items recipes

| Item | AD | AP | AR | MR | HP | HR | SR | AS | MS |
|---|---|---|---|---|---|---|---|---|---|
| Amplifying Tome | — | 20 | — | — | — | — | — | — | — |
| Blasting Wand | — | 45 | — | — | — | — | — | — | — |
| Needlessly Large Rod | — | 65 | — | — | — | — | — | — | — |
| Long Sword | 10 | — | — | — | — | — | — | — | — |
| Pickaxe | 25 | — | — | — | — | — | — | — | — |
| B.F. Sword | 45 | — | — | — | — | — | — | — | — |
| Cloth Armor | — | — | 15 | — | — | — | — | — | — |
| Chain Vest | — | — | 40 | — | — | — | — | — | — |
| Null-Magic Mantle | — | — | — | 20 | — | — | — | — | — |
| Negatron Cloak | — | — | — | 45 | — | — | — | — | — |
| Ruby Crystal | — | — | — | — | 10 | — | — | — | — |
| Rejuvenation Bead | — | — | — | — | — | 1.0 | — | — | — |
| Faeri Charm | — | — | — | — | — | — | 1.0 | — | — |
| Dagger | — | — | — | — | — | — | — | 10 | — |
| Boots | — | — | — | — | — | — | — | — | 25 |

---

## Damage System

### Adaptive Scaling
Keystones automatically compare physical and magic damage paths, dealing whichever is higher based on your gear and enemy defenses.

**Physical Damage** = (Attacker AD - Target AR) / 2
**Magic Damage** = (Attacker AP - Target MR) / 4
**Adaptive Damage** = max(Physical, Magic) × Level Multiplier

### Level Multipliers
Damage scales with player level across 5 tiers:
- Level 1-49: 1.03x
- Level 50-99: 1.07x
- Level 100-199: 1.2x
- Level 200-299: 1.5x
- Level 300+: 1.7x

### Stat Scaling
Each keystone applies bonus damage as a percentage of your total AD and AP, separate from adaptive calculations.

### Per-Stack Damage
Stacking runes multiply base damage by current stack count.


# Keystones

## PRECISION PATH

### Conqueror
**PASSIVE**: Dealing damage to entities generates stacks of Conqueror, lasting 5 seconds and refreshing on subsequent damage. Stacking up to 12 times, each stack grants 2.5 bonus adaptive damage, stacking up to 30 bonus adaptive damage at maximum stacks.

**ADAPTIVE DAMAGE**: Conqueror deals either physical or magical damage depending on whether you have more attack damage or ability power. If the damage contribution of attack damage and ability power are equal, the damage type depends on your adaptive type.

**Cooldown**: None

### Press the Attack
**PASSIVE**: Melee attacks against entities apply a stack for 3 seconds, refreshing on subsequent applications. Stacking up to 3 times, the third stack consumes all stacks to deal 17.67-30.04 (base on level) bonus adaptive damage.

**ADAPTIVE DAMAGE**: Press the Attack deals either physical or magical damage depending on whether you have more attack damage or ability power. If the damage contribution are equal, the damage type depends on your adaptive type.

**Cooldown**: 6 seconds

### Lethal Tempo
**PASSIVE**: Melee attacks against entities grant a stack for 6 seconds, refreshing on subsequent attacks and stacking up to 6 times. While stacking, basic attacks deal 4.5-7.65 (based on level) bonus adaptive damage upon arrival. At maximum stacks, basic attacks empower to fire a bolt dealing additional adaptive damage based on attack speed, lasting 3 seconds with a 30-second cooldown after expiring.

**ADAPTIVE DAMAGE**: Lethal Tempo deals either physical or magical damage depending on whether you have more attack damage or ability power. Stacks expire one by one every 0.5 seconds when the duration ends.

**Cooldown**: 30 seconds

### FleetFootwork
**PASSIVE**: Moving and projectile attacks generate Charges, up to 100. At 100 Charges, your next attack consumes all stacks and heals you for 25% (+10% from total AD & 5% for AP) of missing health plus gain +20% movement speed lasting for 5 seconds which refreshes on your next projectile attack.

**Cooldown**: None

---

## RESOLVE PATH

### AfterShock
**PASSIVE**: Taking damage from enemy champions grants a stack lasting 6 seconds, refreshing on subsequent hits. Stacking up to 3 times, each stack grants +10% damage reduction. At maximum stacks, your next attack deals 15 bonus damage and knocks back enemies.

**Cooldown**: 20 seconds

### Guardian
**PASSIVE**: Being near with a players (5 max) without entering combat for 10 seconds grants all of you 80% absorption with a duration of 50 seconds.

**Cooldown**: 60 seconds

### Grasp of the Undying
**PASSIVE**: Entering combat generates stacks for 5 seconds, stacking up to 4 times. At maximum stacks, your next melee attack deals bonus damage multiplied by your current absorption hearts (20% per heart), heals you for 15% of missing health, and permanently grants 1 absorption heart.

**ABSORPTION SCALING**: Each absorption heart you gain from Grasp acts as a 0.2x multiplier on your attack damage, stacking infinitely. With 5 absorption hearts, your empowered attack deals 2x damage.

**Cooldown**: 60 seconds

---

## DOMINATION PATH

### Electrocute

Damaging melee or projectile attacks apply stacks against entities, up to one per cast instance per entity. Applying 3 stacks to a target within 3 second period causes them to be struck by lightning, dealing them 25.15-42.76 (based on level) adaptive damage.

**ADAPTIVE DAMAGE**: Electrocute deals either physical or magical damage depending on whether you have more attack damage or ability power.

### DarkHarvest
**PASSIVE**: Reap the souls of fallen enemies. Dealing either melee or projectile damage to entities below of their 50% of maximum health deals 1.5-2.6 (based on level) bonus adaptive damage (+2.5 per soul, up to 20 stacks) and after 0.75 seconds delay, reap 1 soul. This cannot occur again for 1 minute.

**VARIABLE DAMAGE**: Dark Harvest deals adaptive damage, choosing between physical and magical based on what penetrates enemy defenses most effectively. This rune thrives in skirmishes where every hit counts.

**Cooldown**: 60 seconds

### HailOfBlades
**PASSIVE**: Striking with a melee attack against entities triggers Hail of Blades, and if the windup completes you gain 2 stacks of the effect for 3 seconds, with the duration refreshes on each melee attacks, expiring one by one after not attacking for 3 seconds. While Hail of blades are active, you gain 10% bonus attack speed and a 1.25 true damage multiplier (+8% from total AD and 6% for AP).

**ADAPTIVE DAMAGE**: Hail of Blades' true damage component scales adaptively, the further your opponent is, the more pure damage bypasses their defenses. During Windup and Cooldown phases, you deal raw attack damage with no adaptive scaling.

**Cooldown**: 30 seconds (after Active phase)

---

## SORCERY PATH

### ArcaneComet
**PASSIVE**: Striking with a projectile hurls an Arcane Comet at your target's location that lands after 0.825 seconds, dealing 20.5-34.6 (based on level) adaptive damage plus 5% bonus attack damage and 15% bonus ability power. The comet's impact is amplified 2x while falling, making distance and positioning crucial.

**VARIABLE DAMAGE**: Arcane Comet's adaptive scaling compares your attack damage and ability power against enemy defenses, choosing whichever penetrates more effectively. The 30-tick fall animation rewards patience timing the comet's impact with enemy positioning maximizes devastation.

**Cooldown**: 20 seconds

### DeathfireTorch
**PASSIVE**: Ignite your enemies with searing flames. Weapon attacks with Fire Aspect or Flame enchantments engulfs a target in fire for 5 seconds, ticking every 10 ticks for a total of 11 damage instances. Each tick deals 10.5-17.8 (based on level) magic damage (+5% from total AD and 10% for AP), scaling massively with magical power. Multiple targets can burn simultaneously, letting you engulf entire groups in flames.

**VARIABLE DAMAGE**: DeathfireTorch is ability power centric, scaling primarily with your magical stats while gaining minimal benefit from attack damage. The burn persists independently per target, rewarding multi-target positioning and ability rotations.

**Cooldown**: None (burns stack infinitely)

---

## INSPIRATION PATH

### FirstStrike
**PASSIVE**: Strike first, strike hard. Your first attack after 0.25 seconds of not attacking grants 10 XP and activates First Strike for 3 seconds. During this window, deal 7% true damage bonus on every attack while tracking total bonus damage dealt. When First Strike expires, gain bonus XP equal to your total tracked damage multiplied by the higher of (+20% from total AD or 15% for AP).

**VARIABLE DAMAGE**: First Strike tracks damage from both attack and ability power, then grants XP based on whichever stat dominates your build.

**Cooldown**: 25 seconds

### GlacialAugment
**PASSIVE**: Freeze your enemies in place. Striking with a projectile encases a target in ice for 6 seconds (+7% from total AD and 6% for AP). During the freeze, enemies suffer Slowness III and Weakness I, making escape impossible. Powder snow blocks manifest around frozen targets, creating hazardous terrain. If your weapon carries Flame enchantment, Glacial Augment cannot trigger, fire and ice cannot coexist.

**VARIABLE DAMAGE**: Glacial Augment deals no damage but scales its utility immensely. The longer freeze duration rewards high-scaling builds, turning kite-heavy playstyles into complete lockdowns.

**Cooldown**: 45 seconds

---

## Damage Calculation Formula

### Physical Damage
```
physicalDamage = (attackerAD - targetAR) / 2
```

### Magic Damage
```
magicDamage = (attackerAP - targetMR) / 4
```

### Adaptive Damage
```
adaptiveDamage = max(physicalDamage, magicDamage) * levelMultiplier
```

### Bonus Scaling
```
bonusScaling = max(attackerAD * adPercentage, attackerAP * apPercentage)
totalDamage = adaptiveDamage + bonusScaling
```

### BuffManager Integration
Utility runes (FleetFootwork, GlacialAugment, Grasp) use BuffManager for scaling:
```
scaledValue = baseValue + (AD × adMultiplier) + (AP × apMultiplier)
```

### Per-Stack Damage
Stacking runes apply damage per-stack:
```
totalDamage = (baseDamage * levelMultiplier) * currentStacks
```
