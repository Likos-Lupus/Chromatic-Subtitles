// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay.Subtitle;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.util.ARGB;
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

@Mixin(SubtitleOverlay.class)
@Environment(EnvType.CLIENT)
public class SubtitleOverlayMixin {

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
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
            ),
            index = 4
    )
    private int chromaticSubtitles$modifyTextDrawColor(int color) {
        if (this.chromaticSubtitles$currentEntry == null) {
            return color;
        }

        return ARGB.multiply(color, this.chromaticSubtitles$currentEntry.chromaticSubtitles$getTextColor());
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"
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
            method = "onPlaySound",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/SubtitleOverlay$Subtitle;refresh(Lnet/minecraft/world/phys/Vec3;)V"
            )
    )
    private void chromaticSubtitles$resetColor(
            SoundInstance sound,
            WeighedSoundEvents soundSet,
            float range,
            CallbackInfo ci,
            @Local Subtitle entry
    ) {
        ((SubtitleColorAccess) entry).chromaticSubtitles$setColor(sound);
    }

    @Redirect(
            method = "onPlaySound",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            )
    )
    private boolean chromaticSubtitles$setColor(
            List<Object> entries,
            Object entry,
            SoundInstance sound,
            WeighedSoundEvents soundSet
    ) {
        ((SubtitleColorAccess) entry).chromaticSubtitles$setColor(sound);
        return entries.add(entry);
    }

}
