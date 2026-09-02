<div align="center">

# KUPAL MC SEASON 2 MECHANICS

Main Theme: League of Legends Runeterra-Style Gameplay w/ Summoners Rift Structure and Combat System
</div>

<div align="center">

## The Territorial System

</div>

### Server Map

As we've already planned the map for this season will be **South East Asia (SEA)**. Players upon joining will spawn to one of the random **Country** below:

- Thailand
- Cambodia
- Vietnam
- Laos
- Myanmar
- The Philippines
- Timor-Leste
- Malaysia
- Singapore
- Indonesia &
- Brunei

Each country contains of 1 **Nexus** and 3 **Turrets** while also having 1 **Inhibitor** PER town. We want to prevent cross-teaming for this season and so will implement limitations & restrictions while also having perks.

#### Limitation & Restrictions:
- Immigrants - Players can only join a town from their own country.
- Language Barrier: Players can only send chat/message to players from the same country. (SVC Exception)
  - (Will add a diff feature for global communication)

#### Benefits & Risk:
- DEFFF OYY!! - Keeping your Country's **Nexus** protected is a must as it gives players the following buffs:
  - Defensive: [+10 Health](#health-bullet), [+15 Armor](#armor-bullet)
  - Offensive: [+10 Attack Damage](#ad-bullet), [+10 Ability Power](#ap-bullet)
  - Utility: Be able to recall to server spawn (HUB) & **Respawn**
- DEFEAT - What happens when a Country's Nexus were destroyed:
  - If inside - Players on that country will trigger combat log after disconnecting
  - If inside - Players on that country cannot respawn.
  - If outside - Players on that country are restricted to join. (Stuck on Spawn)

 #### <a id="nexus-info"></a>Nexus
  - Only targetable when there's minimum of **15** players online on respective country. 
  - After it being destroyed it will enter a **1 Hour** regeneration stage.

 #### <a id="turret-info"></a>Turret
  - Shoots players on from other country within its **20** Block Range
  - After it BEing Destroyed it will enter a **15 Minutes** regeneration stage.

 #### <a id="inhibitor-info"></a>Inhibitor
 - Gives The following buffs on its town members:
   - Defensive: [+20 Armor](#armor-bullet), [+20 Magic Resist](#mr-bullet)
- Can be destroyed even by players on the same Country.


<div align="center">

## The Combat System

</div>

### Player Statistics
When referring to player units a statistic measures the magnitude of an unmodified basic attribute or capability. For example: how durable the unit is; how fast it is able to move; how quickly it is able to perform attacks; etc. An effect (e.g. how much damage an attack deals) that increases in strength by a statistic is said to "scale with" or "scale off of" that statistic. Scaling type can either directly or indirectly influence the player's capabilities, and often slightly correlates to the player's country.

Units are typically given a group of non-zero, fundamental base stats by the server rules, while the remaining stats always have a base value of zero. A subset of those non-zero base stats can naturally improve, or grow, by progressing.

    A base statistic is a statistic's unmodified value.
    A growth statistic is an increase to the base amount that is gained explicitly by progressing.

Increases and reductions to the statistics that are gained from any other sources such as runes, items, buffs and debuffs, are called bonuses. The exception is attack speed growth, which base value is depends on classes (Fighter, Support, Assasin, Mage, Tank & Marksman) but uniquely counts toward the bonus amount.

#### Types

There are only **10** current types of player statistics divided into 3 different categories: **Defensive**, **Offensive** and **Utility**. In the list below, only the basic attribute of each statistic is presented. Inside the server, **weapons**, **items**, and **runes**, may freely scale off of any and/or multiple statistics (and sometimes other effects), whether in their damage, defense, or any other attribute. 

- ##### Defensive
  - <a id="health-bullet"></a>Health **[HP]**: A player dies when their health is reduced to zero. Some items and effects may scale off of your own, on your ally's, or on a target enemy's: current, bonus, missing, or maximum health.
  - Health Regeneration **[HR]**: The amount of health the player passively restores per 5 seconds. (Vanilla health regen from foods are disabled)
  - <a id="armor-bullet"></a>Armor **[AR]**: Reduces (mitigates) the amount of physical damage taken.
  - <a id="mr-bullet"></a>Magic Resistance **[MR]**: Reduces (mitigates) the amount of magic damage taken.
- ##### Offensive
  - Attack speed **[AS]**: The number of attacks the player is allowed to perform per second.
  - <a id="ad-bullet"></a>Attack damage **[AD]**: One of the two main offensive statistics, along with ability power. Unmodified basic attacks deal exactly this amount of damage.
  - <a id="ap-bullet"></a>Ability power **[AP]**: One of the two main offensive statistics, along with attack damage.
  - Critical strike chance **[Crit%]**: Denotes the chance that a basic attack will critically strike. (Yes vanilla jump crit attack deals no extra damage)
- ##### Utility
  - Saturation Regeneration **[SR]**: The amount of food saturation the player passively restores per 5 seconds. (Might use this to serve as mana/energy in the future)
  - Movement Speed **[MS]**: How quickly a player moves, measured in game-distance units per second.

---

### Damage

    Not to be confused with attack damage, a player statistic, nor conflated with health costs, which are not damage.


Damage is the result of a direct combat interaction between two units: a source unit causes a deduction of a target unit's current health via a attack (or accompanying debuff). Damage takes place in engine time ("instantaneously") and the application itself is formally called a damage instance or damage event. 

#### Types

The following three damage types exist: **Physical**, **Magic**, and **True**. Each damage event can fall under only one of these three categories.

These three types exist to add variability to gameplay beyond simple deduction of health equal to the source's outgoing damage value, by potentially reducing, or mitigating it instead.

  - **Physical damage** is innately mitigated by  armor, a statistic that units possess.
  - **Magic damage** is innately mitigated by  magic resistance, a statistic that units possess.
  - **True damage** cannot be mitigated by any innate statistic.

#### Adaptive Force

 Adaptive force is a stat that grants the player either  attack damage or  ability power, depending on the current amount of bonus attack damage and ability power of the champion, as follows:

     Higher Bonus AD →  Attack damage
     Higher AP →  Ability power

1 point of Adaptive Force provides **0.6** bonus AD or **0.8** AP.

If the bonus attack damage and the ability power of the unit are equal, the stat granted will always fallback to **AD**.

#### Adaptive Damage

A similar effect can be found on:

Adaptive Damage: This effect deals either physical or magic damage depending on whether you have more  bonus **attack damage** or  **ability power**.

    Greater  bonus attack damage → Physical damage
    Greater  bonus ability power → Magic damage

The player total adaptive damage scales with their current level across 5 tiers:

- Level 1-49: **1.03x**
- Level 50-99: **1.07x**
- Level 100-199: **1.2x**
- Level 200-299: **1.5x**
- Level 300+: **1.7x**
---

### Player Runes

Runes are enhancements that add new abilities or buffs to the player. Players can choose their loadout of runes inside the server spawn.

#### Rune paths

Runes are divided into five paths:

  - **Precision** - Improved attacks and sustained damage
  - **Domination** - Burst damage and target access
  - **Sorcery** - Empowered abilities and resource manipulation
  - **Resolve** - Durability and crowd control
  - **Inspiration** - Creative tools and rule bending
  

  `For League mfs: All runes currently consist of one path and a keystone only. More content along the way.`

#### Tree

<div align="center">
<table>
  <tr>
    <th colspan="3" align="center">PRECISION</th>
  </tr>
  <tr>
    <td>Keystones</td>
    <td>Description</td>
    <td>Cooldown</td>
  </tr>
  <tr align="justify">
    <td align="center">Press The Attack</td>
    <td>ᴘᴀꜱꜱɪᴠᴇ: Melee attacks against entities apply a stack for 3 seconds, refreshing on subsequent applications. Stacking up to 3 times, the third stack consumes all stacks to deal 3.5 - 5.9 (base on level) bonus adaptive damage</td>
    <td align="center">6s</td>
  </tr>
    <tr align="justify">
    <td align="center">Lethal Tempo</td>
    <td>ᴘᴀꜱꜱɪᴠᴇ: Melee attacks against entities grant a stack for 6 seconds, refreshing on subsequent attacks and stacking up to 6 times. While stacking, attacks deal 2.7 - 4.5 (based on level) bonus adaptive damage upon arrival. At maximum stacks, basic attacks empower to fire a bolt gaining 60% bonus attack speed, lasting 3 seconds with a 30 seconds cooldown after expiring</td>
    <td align="center">30s</td>
  </tr>
    <tr align="justify">
    <td align="center">Fleet Footwork</td>
    <td>ᴘᴀꜱꜱɪᴠᴇ: Moving and projectile attacks generate Charges, up to 100. At 100 Charges, your next projectile attack consumes all stacks and heals you for 25% (+ 10% from total AD & 5% for AP) of missing health plus gain +20% movement speed lasting for 5 seconds which refreshes on your next projectile attack.</td>
    <td align="center">N/A</td>
  </tr>
    </tr>
    <tr align="justify">
    <td align="center">Conqueror</td>
    <td>ᴘᴀꜱꜱɪᴠᴇ: Dealing damage to entities generates stacks of Conqueror, lasting 5 seconds and refreshing on subsequent damage. Stacking up to 12 times, each stack grants 1.7 bonus adaptive damage, stacking up to 20 bonus adaptive damage at maximum stacks.</td>
    <td align="center">N/A</td>
  </tr>
</table>

<table>
  <tr>
    <th colspan="3" align="center">DOMINATION</th>
  </tr>
  <tr>
    <td>Keystones</td>
    <td>Description</td>
    <td>Cooldown</td>
  </tr>
  <tr align="justify">
    <td align="center">Electrocute</td>
    <td>ᴘᴀꜱꜱɪᴠᴇ: Damaging melee or projectile attacks apply stacks against entities, up to one per cast instance per entity. Applying 3 stacks to a target within 3 second period causes them to be struck by lightning, dealing them 5.5 - 9.3 (based on level) (+ 5% from total AD and 10% for AP) adaptive damage.</td>
    <td align="center">25s</td>
  </tr>
    <tr align="justify">
    <td align="center">Dark Harvest</td>
    <td>ᴘᴀꜱꜱɪᴠᴇ:  Reap the souls of fallen enemies. Dealing either melee or projectile damage to entities below of their 50% of maximum health deals 0.4 - 1.0 (based on level) bonus adaptive damage (+ 0.4 per soul, up to 20 stacks) and after 0.75 seconds delay, reap 1 soul. This cannot occur again for 1 minute.</td>
    <td align="center">60s</td>
  </tr>
    <tr align="justify">
    <td align="center">Hail of Blades</td>
    <td>ᴘᴀꜱꜱɪᴠᴇ: Striking with a melee attack against entities triggers Hail of Blades, and if the windup completes you gain 4 stacks of the effect for 3 seconds, with the duration refreshes on each melee attacks until all stacks are consumed, expiring one by one after not attacking for 3 seconds. While Hail of blades are active, you gain 10% bonus attack speed and 7% true damage (+ 8% from total AD and 6% for AP.</td>
    <td align="center">60s</td>
  </tr>
</table>

<table>
  <tr>
    <th colspan="3" align="center">SORCERY</th>
  </tr>
  <tr>
    <td>Keystones</td>
    <td>Description</td>
    <td>Cooldown</td>
  </tr>
  <tr align="justify">
    <td align="center">Arcane Comet</td>
    <td>ᴘᴀꜱꜱɪᴠᴇ: Striking with a projectile hurls an Arcane Comet at your target's location that lands after 0.825 seconds, dealing 4.5 - 7.6 (based on level) adaptive damage (+ 5% from total AD and 15% for AP). The comet's impact is amplified 2x while falling, making distance and positioning crucial.</td>
    <td align="center">20s</td>
  </tr>
    <tr align="justify">
    <td align="center">Deathfire Torch</td>
    <td>ᴘᴀꜱꜱɪᴠᴇ:  Ignite your enemies with searing flames. Weapon attacks with Fire Aspect or Flame enchantments engulfs a target in fire for 5 seconds, ticking every 10 ticks for a total of 11 damage instances. Each tick deals magic damage from +5%  of total AD and 10% for AP, scaling massively with magical power. Multiple targets can burn simultaneously, letting you engulf entire groups in flames.</td>
    <td align="center">N/A</td>
  </tr>
    <tr align="justify">
    <td align="center">Storm Raider's Surge</td>
    <td>ᴘᴀꜱꜱɪᴠᴇ: Dealing damage to a entity equal to 25% of their maximum health within 3 seconds removes all active debuff effects and grants you 20% bonus movement speed.</td>
    <td align="center">20s</td>
  </tr>
</table>

<table>
  <tr>
    <th colspan="3" align="center">RESOLVE</th>
  </tr>
  <tr>
    <td>Keystones</td>
    <td>Description</td>
    <td>Cooldown</td>
  </tr>
  <tr align="justify">
    <td align="center">Grasp of the Undying</td>
    <td>ᴘᴀꜱꜱɪᴠᴇ: Entering combat generates stacks for 5 seconds, stacking up to 4 times. At maximum stacks, your next melee attack deals bonus damage multiplied by your current absorption hearts (20% per heart), heals you for 15% of missing health, and permanently grants 1 absorption heart.</td>
    <td align="center">60s</td>
  </tr>
    <tr align="justify">
    <td align="center">Aftershock</td>
    <td>ᴘᴀꜱꜱɪᴠᴇ:  Being down to 30% of your max health grants you a static 45 (+ 75% bonus armor) bonus armor and 45 (+ 75% bonus magic resistance) bonus magic resistance for 2.5 seconds. After the duration, you release a shockwave that deals 3.5 – 7 (based on level) (+ 8% of your bonus health) magic damage to entities within 5 blocks radius.</td>
    <td align="center">20s</td>
  </tr>
    <tr align="justify">
    <td align="center">Guardian</td>
    <td>ᴘᴀꜱꜱɪᴠᴇ: Being near with players (5 max) within 10 blocks radius without you getting damaged for 10 seconds grants all of you 80% absorption with a duration of 50 seconds</td>
    <td align="center">60s</td>
  </tr>
</table>

<table>
  <tr>
    <th colspan="3" align="center">INSPIRATION</th>
  </tr>
  <tr>
    <td>Keystones</td>
    <td>Description</td>
    <td>Cooldown</td>
  </tr>
  <tr align="justify">
    <td align="center">First Strike</td>
    <td>ᴘᴀꜱꜱɪᴠᴇ: Strike first, strike hard. Your first attack after 0.25 seconds of not attacking grants 10 XP and activates First Strike for 3 seconds. During this window, deal 7% true damage bonus on every attack while tracking total bonus damage dealt. When First Strike expires, gain bonus XP equal to your total tracked damage multiplied by (+ 20% from total AD and 15% for AP).</td>
    <td align="center">60s</td>
  </tr>
  <tr align="justify">
    <td align="center">Glacial Augment</td>
    <td>ᴘᴀꜱꜱɪᴠᴇ: Freeze your enemies in place. Striking with a projectile will cause powdered snow to encase and slow them by 20% (+ 7% from total AD and 6% for AP) and have their damage reduced by 15%</td>
    <td align="center">45s</td>
  </tr>
</table>

</div>




