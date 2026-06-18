// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public final class ChromaticSubtitlesConfig {

    private static final Map<SoundSource, SubtitleColor> DEFAULT_COLORS = Map.ofEntries(
            Map.entry(SoundSource.MUSIC, SubtitleColor.ofText(ChatFormatting.DARK_PURPLE)),
            Map.entry(SoundSource.RECORDS, SubtitleColor.ofText(ChatFormatting.DARK_RED)),
            Map.entry(SoundSource.WEATHER, SubtitleColor.ofText(ChatFormatting.AQUA)),
            Map.entry(SoundSource.BLOCKS, SubtitleColor.ofText(ChatFormatting.GREEN)),
            Map.entry(SoundSource.HOSTILE, SubtitleColor.ofText(ChatFormatting.RED)),
            Map.entry(SoundSource.NEUTRAL, SubtitleColor.ofText(ChatFormatting.YELLOW)),
            Map.entry(SoundSource.PLAYERS, SubtitleColor.ofText(ChatFormatting.GOLD)),
            Map.entry(SoundSource.AMBIENT, SubtitleColor.ofText(ChatFormatting.GRAY)),
            Map.entry(SoundSource.VOICE, SubtitleColor.ofText(ChatFormatting.LIGHT_PURPLE)),
            Map.entry(SoundSource.UI, SubtitleColor.ofText(ChatFormatting.BLUE))
    );

    public static final ChromaticSubtitlesConfig DEFAULT = new ChromaticSubtitlesConfig(
            DEFAULT_COLORS,
            SubtitleColor.DEFAULT
    );

    public static final Codec<ChromaticSubtitlesConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ChromaticSubtitlesCodecs.SOUND_SOURCE_TO_SUBTITLE_COLOR.optionalFieldOf(
                    "colors",
                    DEFAULT_COLORS
            ).forGetter(ChromaticSubtitlesConfig::colors),
            SubtitleColor.CODEC.optionalFieldOf(
                    "default_color",
                    SubtitleColor.DEFAULT
            ).forGetter(ChromaticSubtitlesConfig::defaultColor)
    ).apply(instance, ChromaticSubtitlesConfig::new));

    private final Map<SoundSource, SubtitleColor> colors;
    private final SubtitleColor defaultColor;

    private ChromaticSubtitlesConfig(
            Map<SoundSource, SubtitleColor> colors,
            SubtitleColor defaultColor
    ) {
        this.colors = Map.copyOf(colors);
        this.defaultColor = defaultColor;
    }

    public static @NonNull ChromaticSubtitlesConfig of(
            Map<SoundSource, SubtitleColor> colors,
            SubtitleColor defaultColor
    ) {
        return new ChromaticSubtitlesConfig(colors, defaultColor);
    }

    public SubtitleColor getColorForSource(SoundSource source) {
        return this.colors.getOrDefault(source, this.defaultColor);
    }

    public SubtitleColor getDefaultColor() {
        return this.defaultColor;
    }

    public Map<SoundSource, SubtitleColor> colors() {
        return this.colors;
    }

    public SubtitleColor defaultColor() {
        return this.defaultColor;
    }

}
