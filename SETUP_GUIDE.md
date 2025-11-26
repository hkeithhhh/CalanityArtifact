# CalanityArtifact Plugin Setup Guide

## Quick Start

### 1. Build the Plugin
```bash
cd /workspaces/CalanityArtifact
mvn clean package
```

The compiled JAR will be in `target/CalanityArtifact-1.0.0.jar`

### 2. Install on Server
Copy the JAR to your PaperMC server's plugins folder:
```bash
cp target/CalanityArtifact-1.0.0.jar ~/papermc/plugins/
```

### 3. Restart Server
Restart your PaperMC server. The plugin will:
- Create `plugins/CalanityArtifact/` directory
- Copy default config files
- Load all artifact data

### 4. Test in Game
```
/artifacts           # Opens artifact GUI
/artifactadmin reload # Reload configs (admin only)
```

## Configuration

### Default Artifacts
The plugin comes with 3 pre-configured artifacts:
- **FIREFROST** - Lava Bucket artifact with fire/strength stats
- **FROSTBOUND** - Packed Ice artifact with frost/speed stats  
- **EARTHEN** - Diamond Block artifact with resistance/mining stats

### Adding Custom Artifacts

Edit `plugins/CalanityArtifact/config/artifacts.yml`:

```yaml
artifacts:
  YOUR_ARTIFACT_ID:
    display_name: "&c&lYour Artifact"
    description:
      - "&7Description line 1"
      - "&7Description line 2"
    required_item: "ITEM_NAME"
    required_item_custom_model: 0
    tier_2_count: 2
    tier_2_stats:
      - "stat_name: value"
    tier_5_count: 5
    tier_5_stats:
      - "stat_name: value"
    lore:
      - "&7Lore text"
```

Then reload: `/artifactadmin reload`

## Color Code Reference

Use `&` followed by codes:
- `&c` = Red
- `&b` = Blue/Aqua
- `&2` = Dark Green
- `&6` = Gold/Yellow
- `&f` = White
- `&7` = Gray
- `&8` = Dark Gray
- `&l` = Bold
- `&m` = Strikethrough
- `&n` = Underline
- `&o` = Italic

## File Structure

```
plugins/CalanityArtifact/
├── config/
│   └── artifacts.yml        # Artifact definitions
└── players/
    └── [UUID].yml          # Player artifact data (auto-created)
```

## Admin Commands

- `/artifactadmin reload` - Reloads all configs without restart
  - Requires: `calanityartifact.admin` permission

## Player Commands

- `/artifacts` - Opens the artifact GUI inventory
  - Shows all artifacts
  - Displays current count
  - Shows tier unlock status
  - Displays stat bonuses

## Features Implemented

✓ GUI inventory system (5 rows, 45 slots)
✓ Configurable artifacts from YAML
✓ Tiered stat bonuses (2 and 5 count)
✓ Per-player data persistence
✓ Color code support
✓ Admin reload command
✓ Customizable display names and descriptions
✓ Custom item models support

## Troubleshooting

**Plugin doesn't load:**
- Check server logs for errors
- Ensure Java 17+ is installed
- Verify PaperMC 1.21.1+ is running

**Artifacts not showing:**
- Check config file syntax (YAML indentation)
- Verify item material names are valid
- Run `/artifactadmin reload` after config changes

**Commands not working:**
- Check permissions in server config
- Verify plugin.yml is in JAR
- Check for command typos

## Development

### Project Layout
- `CalanityArtifactPlugin.java` - Main plugin class
- `model/` - Data classes (Artifact, PlayerArtifactData)
- `manager/` - Business logic (ArtifactManager, PlayerArtifactManager)
- `gui/` - GUI inventory (ArtifactGUI)
- `commands/` - Command handlers
- `listener/` - Event listeners

### Building for Development
```bash
mvn clean package -DskipTests
```

### Dependencies
- PaperMC API (1.21.1)
- Java 17

## Next Steps / Future Enhancements

Potential features to add:
- Right-click artifact interaction
- Trading/combining artifacts
- Special artifact effects
- Achievement system
- Leaderboards
- Database persistence
- Custom particle effects
- Sound effects
