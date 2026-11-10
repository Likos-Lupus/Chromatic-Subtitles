// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import top.likoslupus.chromaticsubtitles.ChromaticSubtitles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.jspecify.annotations.NonNull;

public final class ChromaticSubtitlesToml {

    private static final String
            ENABLED_KEY = "enabled",
            DEFAULT_COLOR_KEY = "default_color",
            COLORS_KEY = "colors",
            TEXT_SHADOW_KEY = "text_shadow",
            BACKGROUND_OPACITY_KEY = "background_opacity",
            HIDE_BACKGROUND_KEY = "hide_background",
            HIDDEN_SOURCES_KEY = "hidden_sources",
            SOUND_OVERRIDES_KEY = "sound_overrides",
            SOUND_KEY = "sound",
            TEXT_KEY = "text",
            BACKGROUND_KEY = "background";

    private ChromaticSubtitlesToml() {
    }

    public static @NonNull ChromaticSubtitlesConfig read(Path path) {
        try (
                var config = CommentedFileConfig.builder(path)
                        .sync()
                        .build()
        ) {
            config.load();
            return fromConfig(config);
        }
    }

    public static @NonNull ChromaticSubtitlesConfig fromConfig(@NonNull Config config) {
        var enabled = readBoolean(config, ENABLED_KEY, true);
        var defaultColor = readDefaultColor(config);
        Map<SoundSource, SubtitleColor> colors = new EnumMap<>(SoundSource.class);
        colors.putAll(ChromaticSubtitlesConfig.DEFAULT.colors());

        var colorsValue = config.get(COLORS_KEY);

        if (colorsValue instanceof UnmodifiableConfig colorsConfig) {
            readColors(colorsConfig, colors);
        } else if (colorsValue != null) {
            ChromaticSubtitles.LOGGER.warn(
                    "Expected '{}' to be a TOML table; using built-in default subtitle colors",
                    COLORS_KEY
            );
        }

        var textShadow = readBoolean(config, TEXT_SHADOW_KEY, true);
        var backgroundOpacity = readInt(config, BACKGROUND_OPACITY_KEY, -1);
        var hideBackground = readBoolean(config, HIDE_BACKGROUND_KEY, false);
        var hiddenSources = readHiddenSources(config);
        var soundOverrides = readSoundOverrides(config);

        return ChromaticSubtitlesConfig.of(
                enabled,
                colors,
                defaultColor,
                textShadow,
                backgroundOpacity,
                hideBackground,
                hiddenSources,
                soundOverrides
        );
    }

    private static boolean readBoolean(Config config, String key, boolean fallback) {
        var value = config.get(key);

        if (value == null) {
            return fallback;
        }

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        ChromaticSubtitles.LOGGER.warn(
                "Expected '{}' to be a boolean; using {}",
                key,
                fallback
        );
        return fallback;
    }

    private static int readInt(Config config, String key, int fallback) {
        var value = config.get(key);

        if (value == null) {
            return fallback;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        ChromaticSubtitles.LOGGER.warn(
                "Expected '{}' to be an integer; using {}",
                key,
                fallback
        );
        return fallback;
    }

    private static Set<SoundSource> readHiddenSources(Config config) {
        var value = config.get(HIDDEN_SOURCES_KEY);

        if (value == null) {
            return Set.of();
        }

        if (!(value instanceof Iterable<?> entries)) {
            ChromaticSubtitles.LOGGER.warn(
                    "Expected '{}' to be a list of sound source names; ignoring it",
                    HIDDEN_SOURCES_KEY
            );
            return Set.of();
        }

        Set<SoundSource> hiddenSources = EnumSet.noneOf(SoundSource.class);

        for (var entry : entries) {
            if (!(entry instanceof String sourceName)) {
                ChromaticSubtitles.LOGGER.warn(
                        "Expected entries of '{}' to be strings; skipping an entry",
                        HIDDEN_SOURCES_KEY
                );
                continue;
            }

            var source = SoundSourceNames.byName(sourceName);

            if (source.isEmpty()) {
                ChromaticSubtitles.LOGGER.warn(
                        "Unknown sound source '{}' at {}; skipping this entry",
                        sourceName,
                        HIDDEN_SOURCES_KEY
                );
                continue;
            }

            hiddenSources.add(source.get());
        }

        return hiddenSources;
    }

    private static Map<Identifier, SubtitleColor> readSoundOverrides(Config config) {
        var value = config.get(SOUND_OVERRIDES_KEY);

        if (value == null) {
            return Map.of();
        }

        if (!(value instanceof Iterable<?> entries)) {
            ChromaticSubtitles.LOGGER.warn(
                    "Expected '{}' to be an array of tables; ignoring it",
                    SOUND_OVERRIDES_KEY
            );
            return Map.of();
        }

        Map<Identifier, SubtitleColor> overrides = new LinkedHashMap<>();

        for (var entry : entries) {
            if (!(entry instanceof UnmodifiableConfig overrideConfig)) {
                ChromaticSubtitles.LOGGER.warn(
                        "Expected entries of '{}' to be TOML tables; skipping an entry",
                        SOUND_OVERRIDES_KEY
                );
                continue;
            }

            var soundValue = overrideConfig.get(SOUND_KEY);

            if (!(soundValue instanceof String sound)) {
                ChromaticSubtitles.LOGGER.warn(
                        "Expected '{}' to contain a '{}' string; skipping this entry",
                        SOUND_OVERRIDES_KEY,
                        SOUND_KEY
                );
                continue;
            }

            Identifier soundId;

            try {
                soundId = Identifier.CODEC.parse(
                                JsonOps.INSTANCE,
                                new JsonPrimitive(sound)
                        )
                        .getOrThrow();
            } catch (Exception exception) {
                ChromaticSubtitles.LOGGER.warn(
                        "Invalid sound identifier '{}' at {}; skipping this entry",
                        sound,
                        SOUND_OVERRIDES_KEY,
                        exception
                );
                continue;
            }

            var textValue = overrideConfig.get(TEXT_KEY);

            if (!(textValue instanceof String text)) {
                ChromaticSubtitles.LOGGER.warn(
                        "Expected '{}' entry for '{}' to contain a '{}' string; skipping this entry",
                        SOUND_OVERRIDES_KEY,
                        sound,
                        TEXT_KEY
                );
                continue;
            }

            try {
                var textColor = parseTextColor(
                        text,
                        SOUND_OVERRIDES_KEY + "." + sound + "." + TEXT_KEY
                );
                var background = Optional.<TextColor>empty();
                var backgroundValue = overrideConfig.get(BACKGROUND_KEY);

                if (backgroundValue instanceof String backgroundColor) {
                    background = Optional.of(parseTextColor(
                            backgroundColor,
                            SOUND_OVERRIDES_KEY + "." + sound + "." + BACKGROUND_KEY
                    ));
                } else if (backgroundValue != null) {
                    ChromaticSubtitles.LOGGER.warn(
                            "Expected '{}.{}.{}' to be a color string; ignoring the background color",
                            SOUND_OVERRIDES_KEY,
                            sound,
                            BACKGROUND_KEY
                    );
                }

                overrides.put(soundId, new SubtitleColor(textColor, background));
            } catch (RuntimeException exception) {
                ChromaticSubtitles.LOGGER.warn(
                        "Failed to read sound override for '{}'; skipping this entry",
                        sound,
                        exception
                );
            }
        }

        return overrides;
    }

    private static SubtitleColor readDefaultColor(Config config) {
        var defaultColorValue = config.get(DEFAULT_COLOR_KEY);

        if (defaultColorValue == null) {
            return ChromaticSubtitlesConfig.DEFAULT.defaultColor();
        }

        try {
            return parseSubtitleColor(defaultColorValue, DEFAULT_COLOR_KEY);
        } catch (RuntimeException exception) {
            ChromaticSubtitles.LOGGER.warn(
                    "Failed to read '{}'; using the built-in default subtitle color",
                    DEFAULT_COLOR_KEY,
                    exception
            );
            return ChromaticSubtitlesConfig.DEFAULT.defaultColor();
        }
    }

    private static void readColors(
            UnmodifiableConfig colorsConfig,
            Map<SoundSource, SubtitleColor> colors
    ) {
        colorsConfig.entrySet().forEach(entry -> {
            var sourceName = entry.getKey();
            var source = SoundSourceNames.byName(sourceName);

            if (source.isEmpty()) {
                ChromaticSubtitles.LOGGER.warn(
                        "Unknown sound source '{}' at colors.{}; skipping this subtitle color entry",
                        sourceName,
                        sourceName
                );
                return;
            }

            try {
                colors.put(
                        source.get(),
                        parseSubtitleColor(
                                entry.getValue(),
                                COLORS_KEY + "." + sourceName
                        )
                );
            } catch (RuntimeException exception) {
                ChromaticSubtitles.LOGGER.warn(
                        "Failed to read subtitle color at colors.{}; skipping this entry",
                        sourceName,
                        exception
                );
            }
        });
    }

    private static SubtitleColor parseSubtitleColor(Object value, String path) {
        if (value instanceof String textColor) {
            return SubtitleColor.ofText(parseTextColor(textColor, path));
        }

        if (value instanceof UnmodifiableConfig colorConfig) {
            var textValue = colorConfig.get(TEXT_KEY);

            if (!(textValue instanceof String textColor)) {
                throw new IllegalArgumentException("Expected '%s.%s' to be a color string".formatted(
                        path,
                        TEXT_KEY
                ));
            }

            var backgroundValue = colorConfig.get(BACKGROUND_KEY);
            Optional<TextColor> background = Optional.empty();

            if (backgroundValue != null) {
                try {
                    background = Optional.of(parseBackgroundColor(
                            backgroundValue,
                            path
                    ));
                } catch (RuntimeException exception) {
                    ChromaticSubtitles.LOGGER.warn(
                            "Failed to read '{}.{}'; using no custom background color for this entry",
                            path,
                            BACKGROUND_KEY,
                            exception
                    );
                }
            }

            return new SubtitleColor(
                    parseTextColor(
                            textColor,
                            path + "." + TEXT_KEY
                    ), background
            );
        }

        throw new IllegalArgumentException(
                "Expected '%s' to be a color string or TOML table".formatted(path)
        );
    }

    private static TextColor parseTextColor(String value, String path) {
        try {
            return TextColor.CODEC.parse(
                            JsonOps.INSTANCE,
                            new JsonPrimitive(value)
                    )
                    .getOrThrow();
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid color string '%s' at '%s'".formatted(value, path),
                    exception
            );
        }
    }

    private static TextColor parseBackgroundColor(Object value, String path) {
        if (value instanceof String backgroundColor) {
            return parseTextColor(
                    backgroundColor,
                    path + "." + BACKGROUND_KEY
            );
        }

        throw new IllegalArgumentException(
                "Expected '%s.%s' to be a color string".formatted(
                        path,
                        BACKGROUND_KEY
                )
        );
    }

    public static void write(
            @NonNull Path path,
            @NonNull ChromaticSubtitlesConfig chromaticConfig
    ) throws IOException {
        Files.createDirectories(path.getParent());

        try (
                var config = CommentedFileConfig.builder(path)
                        .sync()
                        .build()
        ) {
            toConfig(config, chromaticConfig);
            addComments(config);
            config.save();
        }
    }

    public static void toConfig(
            @NonNull Config config,
            @NonNull ChromaticSubtitlesConfig chromaticConfig
    ) {
        config.set(ENABLED_KEY, chromaticConfig.enabled());
        config.set(TEXT_SHADOW_KEY, chromaticConfig.textShadow());
        config.set(BACKGROUND_OPACITY_KEY, chromaticConfig.backgroundOpacity());
        config.set(HIDE_BACKGROUND_KEY, chromaticConfig.hideBackground());

        var hiddenSources = chromaticConfig.hiddenSources().stream()
                .map(SoundSource::getName)
                .sorted()
                .toList();
        if (!hiddenSources.isEmpty()) {
            config.set(HIDDEN_SOURCES_KEY, hiddenSources);
        }

        writeSubtitleColor(
                config,
                DEFAULT_COLOR_KEY,
                chromaticConfig.defaultColor()
        );

        var colors = chromaticConfig.colors();
        Arrays.stream(SoundSource.values())
                .forEach(source -> {
                    var color = colors.get(source);
                    if (color != null) {
                        writeSubtitleColor(
                                config,
                                COLORS_KEY + "." + source.getName(),
                                color
                        );
                    }
                });

        var overrides = chromaticConfig.soundOverrides().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)))
                .map(entry -> {
                    var override = Config.inMemory();
                    override.set(SOUND_KEY, entry.getKey().toString());
                    override.set(TEXT_KEY, encodeTextColor(entry.getValue().text()));
                    entry.getValue().background()
                            .ifPresent(background -> override.set(
                                    BACKGROUND_KEY,
                                    encodeTextColor(background)
                            ));
                    return override;
                })
                .toList();

        if (!overrides.isEmpty()) {
            config.set(SOUND_OVERRIDES_KEY, overrides);
        }
    }

    private static void addComments(CommentedFileConfig config) {
        config.setComment(
                ENABLED_KEY,
                "Set to false to restore vanilla subtitle rendering."
        );
        config.setComment(
                DEFAULT_COLOR_KEY,
                "Used when a sound source has no explicit color."
        );
        config.setComment(
                COLORS_KEY,
                "Colors can be Minecraft formatting color names, such as \"dark_purple\", or hex colors, such as \"#AA00AA\"."
        );
        config.setComment(
                TEXT_SHADOW_KEY,
                "Whether subtitle text is rendered with a drop shadow. Vanilla uses true."
        );
        config.setComment(
                BACKGROUND_OPACITY_KEY,
                "Background opacity from 0 to 100. -1 keeps the vanilla opacity."
        );
        config.setComment(
                HIDE_BACKGROUND_KEY,
                "Set to true to hide the subtitle background entirely."
        );
        if (config.contains(HIDDEN_SOURCES_KEY)) {
            config.setComment(
                    HIDDEN_SOURCES_KEY,
                    "Sound sources that should not produce subtitles."
            );
        }
    }

    private static void writeSubtitleColor(
            Config config,
            String path,
            SubtitleColor color
    ) {
        if (color.background().isEmpty()) {
            config.set(path, encodeTextColor(color.text()));
            return;
        }

        config.set(
                path + "." + TEXT_KEY,
                encodeTextColor(color.text())
        );
        config.set(
                path + "." + BACKGROUND_KEY,
                encodeTextColor(color.background().orElseThrow())
        );
    }

    private static String encodeTextColor(TextColor color) {
        return TextColor.CODEC.encodeStart(JsonOps.INSTANCE, color)
                .getOrThrow()
                .getAsString();
    }

}
