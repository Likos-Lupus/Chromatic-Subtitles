# Chromatic Subtitles

[![Stars](https://img.shields.io/github/stars/Likos-Lupus/Chromatic-Subtitles?style=flat-square&label=Stars&labelColor=444444&color=eac54f)](https://github.com/Likos-Lupus/Chromatic-Subtitles/)
[![Release](https://img.shields.io/github/v/release/Likos-Lupus/Chromatic-Subtitles?style=flat-square&labelColor=444444&label=Release&include_prereleases)](https://github.com/Likos-Lupus/Chromatic-Subtitles/releases)
[![GitHub CI](https://img.shields.io/github/actions/workflow/status/Likos-Lupus/Chromatic-Subtitles/ci.yml?style=flat-square&labelColor=444444&branch=master&label=GitHub%20CI)](https://github.com/Likos-Lupus/Chromatic-Subtitles/actions/workflows/ci.yml)
[![Modrinth](https://img.shields.io/badge/Modrinth-Chromatic%20Subtitles-22ff84?style=flat-square&labelColor=444444)](https://modrinth.com/mod/chromatic-subtitles/)
[![CurseForge](https://img.shields.io/badge/CurseForge-Chromatic%20Subtitles-f16436?style=flat-square&labelColor=444444)](https://www.curseforge.com/minecraft/mc-mods/chromatic-subtitles)

Chromatic Subtitles is a continuation fork
of [Colorful Subtitles](https://github.com/haykam821/Colorful-Subtitles) by haykam821.

Changes subtitle colors based on their sound category.

The mod supports Fabric and NeoForge on Minecraft 26.1.2 and 26.3.

## Installation

1. Download Chromatic Subtitles from the project releases page.
2. Pick the jar for your loader (Fabric or NeoForge) and Minecraft version.
3. Place the downloaded file in your `mods` folder.

## Usage

When this mod is installed, subtitles will automatically have a different color depending on their
sound category:

* Music: dark purple
* Records: dark red
* Weather: aqua
* Blocks: green
* Hostile: red
* Neutral: yellow
* Players: gold
* Ambient: gray
* Voice: light purple
* UI: blue

## Configuration

Chromatic Subtitles creates its config at: `config/chromaticsubtitles.toml`. If a legacy Colorful
Subtitles JSON config exists at: `config/colorfulsubtitles.json`, it will be converted to:
`config/chromaticsubtitles.toml` on first launch. The legacy JSON file is left in place.

During development, an existing `config/chromaticsubtitles.json` file can also be converted to the
new TOML format if no TOML config exists.

### In-game configuration

The config screen is optional. It
requires [Cloth Config API](https://modrinth.com/mod/cloth-config):

* **Fabric:** install Cloth Config and [Mod Menu](https://modrinth.com/mod/modmenu). The config
  button is available from Mod Menu. Without Mod Menu there is no in-game entry point.
* **NeoForge:** install Cloth Config. The config button is available from the mod list.

If Cloth Config is not installed, the mod still works normally and the TOML file can be edited by
hand.

### Config format

```toml
# Set to false to restore vanilla subtitle rendering.
enabled = true

# Used when a sound source has no explicit color.
default_color = "white"

# Whether subtitle text is rendered with a drop shadow. Vanilla uses true.
text_shadow = true

# Background opacity from 0 to 100. -1 keeps the vanilla opacity.
background_opacity = -1

# Set to true to hide the subtitle background entirely.
hide_background = false

# Sound sources that should not produce subtitles.
hidden_sources = ["music", "weather"]

[colors]
music = "dark_purple"
record = "dark_red"
weather = "aqua"
block = "green"
hostile = "red"
neutral = "yellow"
player = "gold"
ambient = "gray"
voice = "light_purple"
ui = "blue"

[colors.weather]
text = "aqua"
background = "#002233"

[colors.hostile]
text = "red"
background = "#330000"

# Per-sound overrides. These take priority over the colors table.
[[sound_overrides]]
sound = "minecraft:block.note_block.harp"
text = "#FFAA00"

[[sound_overrides]]
sound = "minecraft:entity.creeper.primed"
text = "#FF5555"
background = "#440000"
```

`default_color` also supports the table form:

```toml
[default_color]
text = "white"
background = "#000000"
```

Colors can be Minecraft formatting color names, such as `dark_purple`, or hex colors, such as
`#AA00AA`. Background colors are RGB only; use `background_opacity` for transparency.

The `MASTER` sound source is not listed in the config screen. It continues to use `default_color`.

Changes made through the config screen apply to newly created subtitles after saving. Subtitles that
are already visible keep their old colors until they disappear.

## License

The mod is licensed under MIT License.
See [LICENSE](https://github.com/Likos-Lupus/Chromatic-Subtitles/blob/master/LICENSE) file for
details.
