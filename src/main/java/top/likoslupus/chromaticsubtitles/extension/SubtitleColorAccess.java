// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.extension;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.ColorHelper;
import org.jspecify.annotations.NonNull;import top.likoslupus.chromaticsubtitles.ChromaticSubtitles;

@Environment(EnvType.CLIENT)
public interface SubtitleColorAccess {

    int chromaticSubtitles$getTextColor();

    int chromaticSubtitles$getBackgroundColor();

    default void chromaticSubtitles$setColor(@NonNull SoundInstance sound) {
        var config = ChromaticSubtitles.getConfig();
        var color = config.getColorForCategory(sound.getCategory());
        var fallbackColor = config.getDefaultColor();

        this.chromaticSubtitles$setTextColor(ColorHelper.fullAlpha(color.text().getRgb()));
        this.chromaticSubtitles$setBackgroundColor(
                color.background()
                        .or(fallbackColor::background)
                        .map(TextColor::getRgb)
                        .orElse(-1)
        );
    }

    void chromaticSubtitles$setTextColor(int color);

    void chromaticSubtitles$setBackgroundColor(int color);

}
