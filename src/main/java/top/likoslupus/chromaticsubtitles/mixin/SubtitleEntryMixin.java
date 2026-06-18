// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.SubtitleOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import top.likoslupus.chromaticsubtitles.extension.SubtitleColorAccess;

@Mixin(SubtitleOverlay.Subtitle.class)
@Environment(EnvType.CLIENT)
public class SubtitleEntryMixin implements SubtitleColorAccess {

    @Unique
    private int chromaticSubtitles$textColor = 0xFFFFFFFF;

    @Unique
    private int chromaticSubtitles$backgroundColor = -1;

    @Override
    public int chromaticSubtitles$getTextColor() {
        return this.chromaticSubtitles$textColor;
    }

    @Override
    public int chromaticSubtitles$getBackgroundColor() {
        return this.chromaticSubtitles$backgroundColor;
    }

    @Override
    public void chromaticSubtitles$setTextColor(int color) {
        this.chromaticSubtitles$textColor = color;
    }

    @Override
    public void chromaticSubtitles$setBackgroundColor(int color) {
        this.chromaticSubtitles$backgroundColor = color;
    }

}
