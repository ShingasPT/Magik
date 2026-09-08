# Magik

**Magik** is a Minecraft magic plugin built around three categories of spells: **Attack**, **Support**, and **Utility**.

The project is designed as a modular magic system, with individual spells separated into their own classes and registered through a central `MagicManager`.

## Features

- Attack, Support, and Utility magic categories
- Modular spell architecture
- Central magic registration and management
- Magic selection GUI
- Player interaction handling for casting magic
- Storm-related spell management and event handling
- Custom combat and utility abilities
- Nexo integration
- Folia support

### Included Magic

The current implementation includes:

**Attack**
- Area-of-effect magic
- Beam
- Black Hole
- Chain Lightning
- Fireball
- Ice Spear
- Meteor
- Orbital
- Storm
- Void Rift

**Support**
- Bless
- Aid
- Blindness
- Swiftwind
- Barrier

**Utility**
- Teleport
- Light
- Featherfall

The spell list is registered centrally during plugin startup, making it straightforward to add or remove magic without restructuring the entire plugin.

## Architecture

Magik is separated into several functional areas:

```text
me.shingas.magik
├── commands
├── gui
├── listeners
├── magic
├── managers
├── spells
│   ├── attack
│   ├── support
│   └── utility
└── utils
```

This separation keeps spell logic, GUI handling, event listeners, and management systems independent.

## Requirements

- Minecraft/Paper 26.2
- Java
- Nexo

The plugin declares Folia support and requires Nexo at runtime.

## Building

The project uses Gradle with Kotlin build scripts.

```bash
./gradlew build
```

On Windows:

```bat
gradlew.bat build
```

The resulting plugin JAR can be found in:

```text
build/libs/
```

## Installation

1. Build or download the plugin JAR.
2. Install the required Nexo dependency.
3. Place `Magik.jar` into your server's `plugins` directory.
4. Start the server.
5. Configure the plugin as needed.

## License

This project is licensed under the MIT License.
