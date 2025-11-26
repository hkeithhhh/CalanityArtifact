# CalanityArtifact

A PaperMC Minecraft plugin that adds a customizable artifact system with a GUI inventory.

## Features

✨ **Artifact System**
- Display up to 5 artifacts in a player's profile
- Customizable artifacts with tiered stat bonuses
- Artifact icons based on Minecraft items
- Beautiful GUI inventory system

📊 **Tiered Stat Bonuses**
- Tier 2: Unlock stats at 2 artifacts
- Tier 5: Unlock additional stats at 5 artifacts
- Completely configurable stats per artifact

⚙️ **Configuration**
- YAML-based artifact configuration
- Easy to add new artifacts
- Customizable GUI title and rows
- Per-player artifact persistence

## Installation

### Requirements
- PaperMC 1.21.1 or later
- Java 17+
- Maven (for building)

### Build Instructions

1. Build the plugin:
```bash
mvn clean package
```

2. Copy the JAR file to your PaperMC plugins folder:
```bash
cp target/CalanityArtifact-1.0.0.jar /path/to/server/plugins/
```

3. Restart your server

## Configuration

### artifacts.yml

Located in `plugins/CalanityArtifact/config/artifacts.yml`

Add artifacts with tiered stat bonuses. Each artifact can grant different stats at 2 and 5 artifact counts.

Example artifact configuration:
- Display name with color codes
- Required item (e.g., LAVA_BUCKET)
- Tier 2 stats (at 2 artifacts)
- Tier 5 stats (at 5 artifacts)
- Custom lore text

## Commands

- `/artifacts` - Opens the artifact GUI inventory
- `/artifactadmin reload` - Reloads all configuration files (requires `calanityartifact.admin` permission)

## Adding New Artifacts

1. Open `plugins/CalanityArtifact/config/artifacts.yml`
2. Add a new artifact entry with display name, description, required item, and stat bonuses
3. Run `/artifactadmin reload` to apply changes

## Data Storage

Player artifact data is stored in:
```
plugins/CalanityArtifact/players/[UUID].yml
```

## Development

### Project Structure

```
CalanityArtifact/
├── pom.xml
├── src/main/
│   ├── java/com/calanity/artifact/
│   │   ├── CalanityArtifactPlugin.java
│   │   ├── commands/
│   │   ├── gui/
│   │   ├── listener/
│   │   ├── manager/
│   │   └── model/
│   └── resources/
│       └── plugin.yml
└── config/
    └── artifacts.yml
```

Built for PaperMC 1.21.1+ with Java 17
