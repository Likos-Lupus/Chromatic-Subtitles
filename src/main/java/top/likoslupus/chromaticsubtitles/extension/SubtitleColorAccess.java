// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.extension;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;
import top.likoslupus.chromaticsubtitles.ChromaticSubtitles;

@Environment(EnvType.CLIENT)
public interface SubtitleColorAccess {

    int chromaticSubtitles$getTextColor();

    int chromaticSubtitles$getBackgroundColor();

    default void chromaticSubtitles$setColor(@NonNull SoundInstance sound) {
        var config = ChromaticSubtitles.getConfig();
        var color = config.getColorForSource(sound.getSource());
        var fallbackColor = config.getDefaultColor();

        this.chromaticSubtitles$setTextColor(ARGB.opaque(color.text().getValue()));
        this.chromaticSubtitles$setBackgroundColor(
                color.background()
                        .or(fallbackColor::background)
                        .map(TextColor::getValue)
                        .orElse(-1)
        );
    }

    void chromaticSubtitles$setTextColor(int color);

    void chromaticSubtitles$setBackgroundColor(int color);

}
