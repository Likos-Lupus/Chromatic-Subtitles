// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Formatting;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public final class ChromaticSubtitlesConfig {

    private static final Map<SoundCategory, SubtitleColor> DEFAULT_COLORS = Map.ofEntries(
            Map.entry(SoundCategory.MUSIC, SubtitleColor.ofText(Formatting.DARK_PURPLE)),
            Map.entry(SoundCategory.RECORDS, SubtitleColor.ofText(Formatting.DARK_RED)),
            Map.entry(SoundCategory.WEATHER, SubtitleColor.ofText(Formatting.AQUA)),
            Map.entry(SoundCategory.BLOCKS, SubtitleColor.ofText(Formatting.GREEN)),
            Map.entry(SoundCategory.HOSTILE, SubtitleColor.ofText(Formatting.RED)),
            Map.entry(SoundCategory.NEUTRAL, SubtitleColor.ofText(Formatting.YELLOW)),
            Map.entry(SoundCategory.PLAYERS, SubtitleColor.ofText(Formatting.GOLD)),
            Map.entry(SoundCategory.AMBIENT, SubtitleColor.ofText(Formatting.GRAY)),
            Map.entry(SoundCategory.VOICE, SubtitleColor.ofText(Formatting.LIGHT_PURPLE)),
            Map.entry(SoundCategory.UI, SubtitleColor.ofText(Formatting.BLUE))
    );

    public static final ChromaticSubtitlesConfig DEFAULT = new ChromaticSubtitlesConfig(
            DEFAULT_COLORS,
            SubtitleColor.DEFAULT
    );

    public static final Codec<ChromaticSubtitlesConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ChromaticSubtitlesCodecs.SOUND_CATEGORY_TO_SUBTITLE_COLOR.optionalFieldOf(
                    "colors",
                    DEFAULT_COLORS
            ).forGetter(ChromaticSubtitlesConfig::colors),
            SubtitleColor.CODEC.optionalFieldOf(
                    "default_color",
                    SubtitleColor.DEFAULT
            ).forGetter(ChromaticSubtitlesConfig::defaultColor)
    ).apply(instance, ChromaticSubtitlesConfig::new));

    private final Map<SoundCategory, SubtitleColor> colors;
    private final SubtitleColor defaultColor;

    private ChromaticSubtitlesConfig(
            Map<SoundCategory, SubtitleColor> colors,
            SubtitleColor defaultColor
    ) {
        this.colors = Map.copyOf(colors);
        this.defaultColor = defaultColor;
    }

    public static @NonNull ChromaticSubtitlesConfig of(
            Map<SoundCategory, SubtitleColor> colors,
            SubtitleColor defaultColor
    ) {
        return new ChromaticSubtitlesConfig(colors, defaultColor);
    }

    public SubtitleColor getColorForCategory(SoundCategory category) {
        return this.colors.getOrDefault(category, this.defaultColor);
    }

    public SubtitleColor getDefaultColor() {
        return this.defaultColor;
    }

    public Map<SoundCategory, SubtitleColor> colors() {
        return this.colors;
    }

    public SubtitleColor defaultColor() {
        return this.defaultColor;
    }

}
