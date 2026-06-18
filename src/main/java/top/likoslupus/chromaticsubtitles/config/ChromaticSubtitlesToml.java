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
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

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
            return ChromaticSubtitlesToml.fromConfig(config);
        }
    }

    public static @NonNull ChromaticSubtitlesConfig fromConfig(@NonNull Config config) {
        var defaultColorValue = config.get(DEFAULT_COLOR_KEY);
        var defaultColor = defaultColorValue == null
                ? SubtitleColor.DEFAULT
                : ChromaticSubtitlesToml.parseSubtitleColor(defaultColorValue, DEFAULT_COLOR_KEY);

        Map<SoundSource, SubtitleColor> colors = new EnumMap<>(SoundSource.class);
        var colorsValue = config.get(COLORS_KEY);

        if (colorsValue == null) {
            colors.putAll(ChromaticSubtitlesConfig.DEFAULT.colors());
        } else if (colorsValue instanceof UnmodifiableConfig colorsConfig) {
            for (var entry : colorsConfig.entrySet()) {
                var sourceName = entry.getKey();
                var source = SoundSourceNames.byName(sourceName)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Unknown sound source '%s' at colors.%s".formatted(sourceName, sourceName))
                        );

                colors.put(source, ChromaticSubtitlesToml.parseSubtitleColor(entry.getValue(), COLORS_KEY + "." + sourceName));
            }
        } else {
            throw new IllegalArgumentException("Expected '%s' to be a TOML table".formatted(COLORS_KEY));
        }

        return ChromaticSubtitlesConfig.of(colors, defaultColor);
    }

    private static SubtitleColor parseSubtitleColor(Object value, String path) {
        if (value instanceof String textColor) {
            return SubtitleColor.ofText(ChromaticSubtitlesToml.parseTextColor(textColor));
        }

        if (value instanceof UnmodifiableConfig colorConfig) {
            var textValue = colorConfig.get(TEXT_KEY);

            if (!(textValue instanceof String textColor)) {
                throw new IllegalArgumentException("Expected '%s.%s' to be a color string".formatted(path, TEXT_KEY));
            }

            var backgroundValue = colorConfig.get(BACKGROUND_KEY);
            Optional<TextColor> background = backgroundValue == null
                    ? Optional.empty()
                    : Optional.of(ChromaticSubtitlesToml.parseBackgroundColor(backgroundValue, path));

            return new SubtitleColor(ChromaticSubtitlesToml.parseTextColor(textColor), background);
        }

        throw new IllegalArgumentException("Expected '%s' to be a color string or TOML table".formatted(path));
    }

    private static TextColor parseTextColor(String value) {
        return TextColor.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(value))
                .getOrThrow();
    }

    private static TextColor parseBackgroundColor(Object value, String path) {
        if (value instanceof String backgroundColor) {
            return ChromaticSubtitlesToml.parseTextColor(backgroundColor);
        }

        throw new IllegalArgumentException("Expected '%s.%s' to be a color string".formatted(path, BACKGROUND_KEY));
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
            ChromaticSubtitlesToml.toConfig(config, chromaticConfig);
            ChromaticSubtitlesToml.addComments(config);
            config.save();
        }
    }

    public static void toConfig(
            @NonNull Config config,
            @NonNull ChromaticSubtitlesConfig chromaticConfig
    ) {
        ChromaticSubtitlesToml.writeSubtitleColor(config, DEFAULT_COLOR_KEY, chromaticConfig.defaultColor());

        var colors = chromaticConfig.colors();
        for (var source : SoundSource.values()) {
            var color = colors.get(source);

            if (color != null) {
                ChromaticSubtitlesToml.writeSubtitleColor(config, COLORS_KEY + "." + source.getName(), color);
            }
        }
    }

    private static void addComments(CommentedFileConfig config) {
        config.setComment(DEFAULT_COLOR_KEY, "Used when a sound source has no explicit color.");
        config.setComment(COLORS_KEY, "Colors can be Minecraft formatting color names, such as \"dark_purple\", or hex colors, such as \"#AA00AA\".");
    }

    private static void writeSubtitleColor(Config config, String path, SubtitleColor color) {
        if (color.background().isEmpty()) {
            config.set(path, ChromaticSubtitlesToml.encodeTextColor(color.text()));
            return;
        }

        config.set(path + "." + TEXT_KEY, ChromaticSubtitlesToml.encodeTextColor(color.text()));
        config.set(path + "." + BACKGROUND_KEY, ChromaticSubtitlesToml.encodeTextColor(color.background()
                .orElseThrow()));
    }

    private static String encodeTextColor(TextColor color) {
        return TextColor.CODEC.encodeStart(JsonOps.INSTANCE, color)
                .getOrThrow()
                .getAsString();
    }

}
