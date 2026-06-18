# Chromatic Subtitles

Chromatic Subtitles is a continuation fork of [Colorful Subtitles](https://github.com/haykam821/Colorful-Subtitles) by
haykam821.

Changes subtitle colors based on their sound category.

The mod currently supports Fabric only, but NeoForge support is planned.

## Installation

1. Download Chromatic Subtitles from the project releases page.
2. Place the downloaded file in your `mods` folder.

## Usage

When this mod is installed, subtitles will automatically have a different color depending on their sound category:

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

Chromatic Subtitles creates its config at: `config/chromaticsubtitles.toml`. If a legacy Colorful Subtitles JSON config
exists at: `config/colorfulsubtitles.json`, it will be converted to: `config/chromaticsubtitles.toml` on first launch.
The legacy JSON file is left in place.

During development, an existing `config/chromaticsubtitles.json` file can also be converted to the new TOML format if no
TOML config exists.

Colors can be configured with the simple color form:

```toml
default_color = "white"

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
```

or with the table form when a custom background color is needed:

```toml
default_color = "white"

[colors]
music = "dark_purple"
record = "dark_red"

[colors.weather]
text = "aqua"
background = "#002233"

[colors.hostile]
text = "red"
background = "#330000"
```

`default_color` also supports the table form:

```toml
[default_color]
text = "white"
background = "#000000"
```

Colors can be Minecraft formatting color names, such as `dark_purple`, or hex colors, such as `#AA00AA`.

## License

The mod is licensed under MIT License.
See [LICENSE](https://github.com/Likos-Lupus/Chromatic-Subtitles/blob/master/LICENSE) file for details.
