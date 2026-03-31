# Items of Legends

Items of Legends is a Minecraft Forge mod for **Minecraft 1.20.1** that adds a set of endgame-style legendary items with combat abilities, etc. and server-side access control for special skills.

## What the Mod Adds

The mod currently adds four items:

- **Cool Stick I found in the woods**
- **Divine Liberator**
- **Immortal Shadow**
- **Reality Fracture**

## Item Overview

### Cool Stick

- Base legendary weapon and crafting component for the other items.
- Has a right-click area ability with cooldown.
- Designed to spare tamed pets/horses from its AoE damage effect.
- Can be obtained from leaves (1% chance) and is used in all current recipes.

### Divine Liberator

- Heavy sword with charge-based special wave attack.
- Visual-heavy skill (particles/sounds), with cooldown and durability usage.
- Integrates with the mod's defensive counter-system against certain special abilities.

### Immortal Shadow

- Mobility/combat sword with charge-and-execute teleport strike behavior.
- Target acquisition is directional and range-limited.
- Includes invincibility/defense event handling and custom visuals.

### Reality Fracture

- Utility item for cross-dimension teleporting.
- Shift + right-click opens configuration (start dimension, destination, teleport mode).
- Teleportation modes: last location, same coordinates, target spawn.
- Right-click executes teleport based on saved configuration.
- Supports custom dimensions

## Ability Whitelist System (Server/Admin)

Legendary ability usage is controlled via an item whitelist manager.

Available command root:

- `/itemsoflegends`

Subcommands:

- `/itemsoflegends list`
- `/itemsoflegends whitelist list <itemname>`
- `/itemsoflegends whitelist add <itemname> <playername>`
- `/itemsoflegends whitelist remove <itemname> <playername>`

Whitelist data is stored in:

- `/config/itemsoflegends/whitelist.json`

## Credits

Some Item Models & Textures by [nongko99](https://www.curseforge.com/minecraft/texture-packs/nongko-3d-weapons)