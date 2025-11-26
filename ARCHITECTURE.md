# CalanityArtifact - System Overview

## What is CalanityArtifact?

A PaperMC Minecraft plugin that implements a **5-slot artifact inventory system** with tiered stat bonuses. Players can collect artifacts and unlock passive stat bonuses at tier 2 (2 artifacts) and tier 5 (5 artifacts).

## Example Use Case

### FireFrost Artifact
- **Required Item:** Lava Bucket (used as the icon)
- **At 2 FireFrost Artifacts:**
  - +1 Fire Resistance
  - +0.5 Strength
- **At 5 FireFrost Artifacts:**
  - +2 Fire Resistance
  - +1 Strength
  - +2 Health

This allows players to specialize in certain artifact types and receive progressive stat bonuses.

## Key Features

### 1. GUI Inventory System
- `/artifacts` command opens a 5-row inventory
- Displays all available artifacts
- Shows current count per artifact
- Displays unlocked stats based on count
- Beautiful color-coded interface

### 2. Fully Configurable
- Add/edit artifacts in `artifacts.yml`
- Custom display names with color codes
- Custom item icons (any Minecraft material)
- Customizable stats at each tier
- Custom lore text

### 3. Player Persistence
- Each player's artifact data auto-saved
- Data stored in `players/[UUID].yml`
- Survives server restarts
- Works with offline players

### 4. Admin Tools
- `/artifactadmin reload` - Reload configs without restart
- Requires `calanityartifact.admin` permission

## Project Architecture

### Core Components

```
CalanityArtifactPlugin (Main)
├── ArtifactManager
│   └── Loads and manages artifact definitions
├── PlayerArtifactManager
│   └── Manages player artifact data and persistence
├── ArtifactGUI
│   └── Displays artifact inventory GUI
├── Commands
│   ├── ArtifactCommand (/artifacts)
│   └── ArtifactAdminCommand (/artifactadmin)
└── Event Listeners
    ├── PlayerInteractListener
    └── InventoryClickListener
```

### Data Models

**Artifact.java**
- Stores artifact definition (name, description, stats, etc.)
- Immutable artifact configuration

**PlayerArtifactData.java**
- Tracks artifact counts per player
- Manages add/remove operations
- Calculates totals

### File Structure

```
CalanityArtifact/
├── pom.xml                 # Maven build config
├── build.sh               # Build script
├── README.md              # Basic documentation
├── SETUP_GUIDE.md         # Installation & setup
├── ARCHITECTURE.md        # This file
│
├── config/
│   └── artifacts.yml      # Artifact definitions
│
└── src/main/
    ├── java/com/calanity/artifact/
    │   ├── CalanityArtifactPlugin.java
    │   ├── model/
    │   │   ├── Artifact.java
    │   │   └── PlayerArtifactData.java
    │   ├── manager/
    │   │   ├── ArtifactManager.java
    │   │   └── PlayerArtifactManager.java
    │   ├── gui/
    │   │   └── ArtifactGUI.java
    │   ├── commands/
    │   │   ├── ArtifactCommand.java
    │   │   └── ArtifactAdminCommand.java
    │   └── listener/
    │       ├── PlayerInteractListener.java
    │       └── InventoryClickListener.java
    └── resources/
        └── plugin.yml
```

## Configuration Example

```yaml
artifacts:
  FIREFROST:
    display_name: "&c&lFireFrost Artifact"
    description:
      - "&7A legendary artifact forged in fire"
    required_item: "LAVA_BUCKET"
    tier_2_count: 2
    tier_2_stats:
      - "stat_fire_resistance: 1"
      - "stat_strength: 0.5"
    tier_5_count: 5
    tier_5_stats:
      - "stat_fire_resistance: 2"
      - "stat_strength: 1"
      - "stat_health: 2"
    lore:
      - "&8━━━━━━━━━━━━━━━━━━━━━━"
      - "&7Type: &cFire Artifact"
```

## How It Works

### 1. Player opens artifact GUI
```
/artifacts → Opens ArtifactGUI
```

### 2. GUI displays:
- All 5 artifact slots
- Current artifact counts
- Unlocked tier status (2/5)
- Associated stat bonuses

### 3. Artifact data persists
- Counts stored in `players/[UUID].yml`
- Automatically saved when modified
- Loaded on player login

### 4. Admin can reload
```
/artifactadmin reload → Reloads config without restart
```

## Building & Deployment

### Build
```bash
mvn clean package
```

### Deploy
```bash
cp target/CalanityArtifact-1.0.0.jar ~/papermc/plugins/
```

### Server Restart
- Plugin loads and creates default config
- Loads existing player data
- Ready for players to use

## Extensibility

The plugin is designed to be easily extended:

1. **Add new artifacts** - Edit `artifacts.yml`
2. **Add new commands** - Create new `CommandExecutor` classes
3. **Add new listeners** - Create new event listener classes
4. **Add new stats** - Modify artifact tier stats in config
5. **Custom item models** - Use `required_item_custom_model` for resource pack integration

## Technical Specifications

- **Language:** Java 17
- **Framework:** PaperMC 1.21.1+
- **Build Tool:** Maven
- **Configuration:** YAML
- **Storage:** File-based (YAML)

## Requirements Met

✓ GUI inventory system (5 slots)
✓ Artifact configuration via config file
✓ Tiered stat bonuses (2 and 5 count)
✓ Per-player tracking
✓ Admin reload command
✓ Fully customizable artifacts
✓ Player persistence
✓ Easy to add new artifacts

## Future Enhancement Possibilities

- Database persistence (MySQL, MongoDB)
- Artifact trading/combining
- Special artifact effects/abilities
- Leaderboards
- Achievements
- Custom particles/sounds
- Web dashboard
- API for other plugins
- Artifact rarity system
- Crafting/discovery system
