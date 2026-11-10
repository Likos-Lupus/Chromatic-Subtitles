// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.client;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import top.likoslupus.chromaticsubtitles.ChromaticSubtitles;
import top.likoslupus.chromaticsubtitles.config.SubtitleColor;

import java.util.*;

/**
 * Converts the compact string-list representation used by the config screen to the map stored in
 * the TOML config. Each line has the form {@code sound=text[,background]}.
 */
public final class SoundOverrideFormat {

    private SoundOverrideFormat() {
    }

    public static List<String> format(Map<Identifier, SubtitleColor> overrides) {
        return overrides.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)))
                .map(entry -> format(entry.getKey(), entry.getValue()))
                .toList();
    }

    public static String format(Identifier sound, SubtitleColor color) {
        var text = color.text().serialize();
        var background = color.background().map(TextColor::serialize);

        return background
                .map(value -> sound + "=" + text + "," + value)
                .orElseGet(() -> sound + "=" + text);
    }

    public static Map<Identifier, SubtitleColor> parseAll(List<String> lines) {
        Map<Identifier, SubtitleColor> overrides = new LinkedHashMap<>();

        for (var line : lines) {
            if (line.isBlank()) {
                continue;
            }

            var sound = parseSound(line);
            var color = parse(line);

            if (sound.isEmpty() || color.isEmpty()) {
                ChromaticSubtitles.LOGGER.warn(
                        "Ignoring invalid sound override line: {}",
                        line
                );
                continue;
            }

            overrides.put(sound.get(), color.get());
        }

        return overrides;
    }

    public static Optional<Identifier> parseSound(String line) {
        var separator = line.indexOf('=');

        if (separator <= 0) {
            return Optional.empty();
        }

        try {
            return Optional.of(Identifier.CODEC.parse(
                            JsonOps.INSTANCE,
                            new JsonPrimitive(line.substring(0, separator).trim())
                    )
                    .getOrThrow());
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    public static Optional<SubtitleColor> parse(String line) {
        var separator = line.indexOf('=');

        if (separator <= 0 || separator == line.length() - 1) {
            return Optional.empty();
        }

        var soundValue = line.substring(0, separator).trim();
        var colorValue = line.substring(separator + 1).trim();

        if (soundValue.isEmpty() || colorValue.isEmpty()) {
            return Optional.empty();
        }

        try {
            var _ = Identifier.CODEC.parse(
                            JsonOps.INSTANCE,
                            new JsonPrimitive(soundValue)
                    )
                    .getOrThrow();
        } catch (Exception exception) {
            return Optional.empty();
        }

        var colorParts = colorValue.split(",", -1);

        if (colorParts.length < 1 || colorParts.length > 2) {
            return Optional.empty();
        }

        var text = parseColor(colorParts[0]);

        if (text.isEmpty()) {
            return Optional.empty();
        }

        Optional<TextColor> background = colorParts.length == 2
                ? parseColor(colorParts[1])
                : Optional.empty();

        if (colorParts.length == 2 && background.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new SubtitleColor(text.get(), background));
    }

    private static Optional<TextColor> parseColor(String value) {
        try {
            return Optional.of(TextColor.CODEC.parse(
                            JsonOps.INSTANCE,
                            new JsonPrimitive(value.trim())
                    )
                    .getOrThrow());
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    public static boolean isValid(String line) {
        return line.isBlank() || parse(line).isPresent();
    }

}
