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
import net.minecraft.sounds.SoundSource;
import top.likoslupus.chromaticsubtitles.ChromaticSubtitles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

public final class ChromaticSubtitlesToml {

    private static final String
            DEFAULT_COLOR_KEY = "default_color",
            COLORS_KEY = "colors",
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

        return ChromaticSubtitlesConfig.of(colors, defaultColor);
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
    }

    private static void addComments(CommentedFileConfig config) {
        config.setComment(
                DEFAULT_COLOR_KEY,
                "Used when a sound source has no explicit color."
        );
        config.setComment(
                COLORS_KEY,
                "Colors can be Minecraft formatting color names, such as \"dark_purple\", or hex colors, such as \"#AA00AA\"."
        );
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
