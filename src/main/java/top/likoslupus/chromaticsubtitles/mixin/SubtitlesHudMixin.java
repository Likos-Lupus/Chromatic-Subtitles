// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.SubtitlesHud;
import net.minecraft.client.gui.hud.SubtitlesHud.SubtitleEntry;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.likoslupus.chromaticsubtitles.extension.SubtitleColorAccess;

import java.util.Iterator;
import java.util.List;

@Mixin(SubtitlesHud.class)
@Environment(EnvType.CLIENT)
public class SubtitlesHudMixin {

    @Unique
    private SubtitleColorAccess chromaticSubtitles$currentEntry;

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Iterator;next()Ljava/lang/Object;",
                    ordinal = 2
            )
    )
    private Object chromaticSubtitles$updateCurrentEntry(Iterator<Object> iterator) {
        var entry = iterator.next();
        this.chromaticSubtitles$currentEntry = entry instanceof SubtitleColorAccess access ? access : null;
        return entry;
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"
            ),
            index = 4
    )
    private int chromaticSubtitles$modifyTextDrawColor(int color) {
        if (this.chromaticSubtitles$currentEntry == null) {
            return color;
        }

        return ColorHelper.mix(color, this.chromaticSubtitles$currentEntry.chromaticSubtitles$getTextColor());
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"
            ),
            index = 4
    )
    private int chromaticSubtitles$modifyBackgroundDrawColor(int color) {
        if (this.chromaticSubtitles$currentEntry == null) {
            return color;
        }

        var backgroundColor = this.chromaticSubtitles$currentEntry.chromaticSubtitles$getBackgroundColor();

        // Use vanilla background color.
        if (backgroundColor < 0) {
            return color;
        }

        // Use custom RGB with vanilla opacity.
        return color | (backgroundColor & 0x00FFFFFF);
    }

    @Inject(
            method = "onSoundPlayed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/SubtitlesHud$SubtitleEntry;reset(Lnet/minecraft/util/math/Vec3d;)V"
            )
    )
    private void chromaticSubtitles$resetColor(
            SoundInstance sound,
            WeightedSoundSet soundSet,
            float range,
            CallbackInfo ci,
            @Local SubtitleEntry entry
    ) {
        ((SubtitleColorAccess) entry).chromaticSubtitles$setColor(sound);
    }

    @Redirect(
            method = "onSoundPlayed",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            )
    )
    private boolean chromaticSubtitles$setColor(
            List<Object> entries,
            Object entry,
            SoundInstance sound,
            WeightedSoundSet soundSet
    ) {
        ((SubtitleColorAccess) entry).chromaticSubtitles$setColor(sound);
        return entries.add(entry);
    }

}
