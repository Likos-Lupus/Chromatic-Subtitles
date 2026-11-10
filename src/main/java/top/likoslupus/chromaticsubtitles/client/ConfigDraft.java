// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.client;

import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import top.likoslupus.chromaticsubtitles.config.ChromaticSubtitlesConfig;
import top.likoslupus.chromaticsubtitles.config.SubtitleColor;

import java.util.*;
import org.jspecify.annotations.NonNull;

/**
 * Mutable copy of {@link ChromaticSubtitlesConfig} used while the Cloth screen is open. Values are
 * written into this draft by Cloth save consumers; the screen then persists the draft once.
 */
public final class ConfigDraft {

    private final Map<SoundSource, SubtitleColor> colors;
    private final Set<SoundSource> hiddenSources;
    private final Map<Identifier, SubtitleColor> soundOverrides;
    private boolean enabled;
    private SubtitleColor defaultColor;
    private boolean textShadow;
    private int backgroundOpacity;
    private boolean hideBackground;

    public ConfigDraft(@NonNull ChromaticSubtitlesConfig config) {
        this.enabled = config.enabled();
        this.defaultColor = config.defaultColor();
        this.colors = new EnumMap<>(SoundSource.class);
        this.colors.putAll(config.colors());
        this.textShadow = config.textShadow();
        this.backgroundOpacity = config.backgroundOpacity();
        this.hideBackground = config.hideBackground();
        this.hiddenSources = EnumSet.noneOf(SoundSource.class);
        this.hiddenSources.addAll(config.hiddenSources());
        this.soundOverrides = new LinkedHashMap<>(config.soundOverrides());
    }

    public boolean enabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public SubtitleColor defaultColor() {
        return this.defaultColor;
    }

    public boolean defaultHasBackground() {
        return this.defaultColor.background().isPresent();
    }

    public int defaultBackgroundRgb() {
        return this.defaultColor.background()
                .map(TextColor::getValue)
                .orElse(0x000000)
                & 0xFFFFFF;
    }

    public void setDefaultTextColor(int rgb) {
        this.defaultColor = this.defaultColor.withText(TextColor.fromRgb(rgb & 0xFFFFFF));
    }

    public void setDefaultBackground(Integer rgb) {
        this.defaultColor = this.defaultColor.withBackground(
                rgb == null
                        ? Optional.empty()
                        : Optional.of(TextColor.fromRgb(rgb & 0xFFFFFF))
        );
    }

    public boolean hasCustomBackground(@NonNull SoundSource source) {
        var color = this.colors.get(source);
        return color != null && color.background().isPresent();
    }

    public int backgroundRgb(@NonNull SoundSource source) {
        return this.color(source).background()
                .map(TextColor::getValue)
                .orElse(0x000000)
                & 0xFFFFFF;
    }

    public SubtitleColor color(@NonNull SoundSource source) {
        return this.colors.getOrDefault(source, this.defaultColor);
    }

    public void setTextColor(@NonNull SoundSource source, int rgb) {
        var current = this.color(source);
        this.colors.put(source, current.withText(TextColor.fromRgb(rgb & 0xFFFFFF)));
    }

    public void setBackground(@NonNull SoundSource source, Integer rgb) {
        var current = this.color(source);
        this.colors.put(
                source, current.withBackground(
                        rgb == null
                                ? Optional.empty()
                                : Optional.of(TextColor.fromRgb(rgb & 0xFFFFFF))
                )
        );
    }

    public boolean textShadow() {
        return this.textShadow;
    }

    public void setTextShadow(boolean textShadow) {
        this.textShadow = textShadow;
    }

    public int backgroundOpacity() {
        return this.backgroundOpacity;
    }

    public void setBackgroundOpacity(int backgroundOpacity) {
        this.backgroundOpacity = Math.clamp(backgroundOpacity, -1, 100);
    }

    public boolean hideBackground() {
        return this.hideBackground;
    }

    public void setHideBackground(boolean hideBackground) {
        this.hideBackground = hideBackground;
    }

    public boolean isHidden(@NonNull SoundSource source) {
        return this.hiddenSources.contains(source);
    }

    public void setHidden(@NonNull SoundSource source, boolean hidden) {
        if (hidden) {
            this.hiddenSources.add(source);
        } else {
            this.hiddenSources.remove(source);
        }
    }

    public Map<Identifier, SubtitleColor> soundOverrides() {
        return Map.copyOf(this.soundOverrides);
    }

    public void setSoundOverrides(@NonNull Map<Identifier, SubtitleColor> overrides) {
        this.soundOverrides.clear();
        this.soundOverrides.putAll(overrides);
    }

    public @NonNull ChromaticSubtitlesConfig toConfig() {
        return ChromaticSubtitlesConfig.of(
                this.enabled,
                this.colors,
                this.defaultColor,
                this.textShadow,
                this.backgroundOpacity,
                this.hideBackground,
                this.hiddenSources,
                this.soundOverrides
        );
    }

}
