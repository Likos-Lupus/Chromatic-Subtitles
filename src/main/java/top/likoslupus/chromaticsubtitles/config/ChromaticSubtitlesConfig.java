// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;

import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.NonNull;

public final class ChromaticSubtitlesConfig {

    private static final Map<SoundSource, SubtitleColor> DEFAULT_COLORS =
            Map.ofEntries(
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

    public static final Codec<ChromaticSubtitlesConfig> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ChromaticSubtitlesCodecs.SOUND_SOURCE_TO_SUBTITLE_COLOR.optionalFieldOf(
                            "colors",
                            DEFAULT_COLORS
                    ).forGetter(ChromaticSubtitlesConfig::colors),
                    SubtitleColor.CODEC.optionalFieldOf(
                            "default_color",
                            SubtitleColor.DEFAULT
                    ).forGetter(ChromaticSubtitlesConfig::defaultColor),
                    Codec.BOOL.optionalFieldOf(
                            "enabled",
                            true
                    ).forGetter(ChromaticSubtitlesConfig::enabled),
                    Codec.BOOL.optionalFieldOf(
                            "text_shadow",
                            true
                    ).forGetter(ChromaticSubtitlesConfig::textShadow),
                    Codec.INT.optionalFieldOf(
                            "background_opacity",
                            -1
                    ).forGetter(ChromaticSubtitlesConfig::backgroundOpacity),
                    Codec.BOOL.optionalFieldOf(
                            "hide_background",
                            false
                    ).forGetter(ChromaticSubtitlesConfig::hideBackground),
                    ChromaticSubtitlesCodecs.SOUND_SOURCE_SET.optionalFieldOf(
                            "hidden_sources",
                            Set.of()
                    ).forGetter(ChromaticSubtitlesConfig::hiddenSources),
                    ChromaticSubtitlesCodecs.SOUND_OVERRIDES.optionalFieldOf(
                            "sound_overrides",
                            Map.of()
                    ).forGetter(ChromaticSubtitlesConfig::soundOverrides)
            ).apply(
                    instance,
                    (
                            colors,
                            defaultColor,
                            enabled,
                            textShadow,
                            backgroundOpacity,
                            hideBackground,
                            hiddenSources,
                            soundOverrides
                    ) -> new ChromaticSubtitlesConfig(
                            enabled,
                            colors,
                            defaultColor,
                            textShadow,
                            backgroundOpacity,
                            hideBackground,
                            hiddenSources,
                            soundOverrides
                    )
            ));

    public static final ChromaticSubtitlesConfig DEFAULT = new ChromaticSubtitlesConfig(
            true,
            DEFAULT_COLORS,
            SubtitleColor.DEFAULT,
            true,
            -1,
            false,
            Set.of(),
            Map.of()
    );

    private final boolean enabled;
    private final Map<SoundSource, SubtitleColor> colors;
    private final SubtitleColor defaultColor;
    private final boolean textShadow;
    private final int backgroundOpacity;
    private final boolean hideBackground;
    private final Set<SoundSource> hiddenSources;
    private final Map<Identifier, SubtitleColor> soundOverrides;

    private ChromaticSubtitlesConfig(
            boolean enabled,
            Map<SoundSource, SubtitleColor> colors,
            SubtitleColor defaultColor,
            boolean textShadow,
            int backgroundOpacity,
            boolean hideBackground,
            Set<SoundSource> hiddenSources,
            Map<Identifier, SubtitleColor> soundOverrides
    ) {
        this.enabled = enabled;
        this.colors = Map.copyOf(colors);
        this.defaultColor = defaultColor;
        this.textShadow = textShadow;
        this.backgroundOpacity = Math.clamp(backgroundOpacity, -1, 100);
        this.hideBackground = hideBackground;
        this.hiddenSources = Set.copyOf(hiddenSources);
        this.soundOverrides = Map.copyOf(soundOverrides);
    }

    public static @NonNull ChromaticSubtitlesConfig of(
            Map<SoundSource, SubtitleColor> colors,
            SubtitleColor defaultColor
    ) {
        return of(
                true,
                colors,
                defaultColor,
                true,
                -1,
                false,
                Set.of(),
                Map.of()
        );
    }

    public static @NonNull ChromaticSubtitlesConfig of(
            boolean enabled,
            Map<SoundSource, SubtitleColor> colors,
            SubtitleColor defaultColor,
            boolean textShadow,
            int backgroundOpacity,
            boolean hideBackground,
            Set<SoundSource> hiddenSources,
            Map<Identifier, SubtitleColor> soundOverrides
    ) {
        return new ChromaticSubtitlesConfig(
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

    public SubtitleColor resolveColor(@NonNull SoundInstance sound) {
        var override = this.soundOverrides.get(sound.getIdentifier());
        return override != null
                ? override
                : this.getColorForSource(sound.getSource());
    }

    public SubtitleColor getColorForSource(SoundSource source) {
        return this.colors.getOrDefault(source, this.defaultColor);
    }

    public SubtitleColor getDefaultColor() {
        return this.defaultColor;
    }

    public boolean enabled() {
        return this.enabled;
    }

    public Map<SoundSource, SubtitleColor> colors() {
        return this.colors;
    }

    public SubtitleColor defaultColor() {
        return this.defaultColor;
    }

    public boolean textShadow() {
        return this.textShadow;
    }

    public int backgroundOpacity() {
        return this.backgroundOpacity;
    }

    public boolean hideBackground() {
        return this.hideBackground;
    }

    public Set<SoundSource> hiddenSources() {
        return this.hiddenSources;
    }

    public Map<Identifier, SubtitleColor> soundOverrides() {
        return this.soundOverrides;
    }

}
