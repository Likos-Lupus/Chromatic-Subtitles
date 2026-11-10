// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay.Subtitle;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.likoslupus.chromaticsubtitles.ChromaticSubtitles;
import top.likoslupus.chromaticsubtitles.extension.SubtitleColorAccess;

import java.util.List;
import org.jspecify.annotations.Nullable;

@Mixin(SubtitleOverlay.class)
public class SubtitleOverlayMixin {

    @Unique private @Nullable Subtitle chromaticSubtitles$currentSubtitle;

    @WrapOperation(
            method = "onPlaySound",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/SubtitleOverlay$Subtitle;refresh(Lnet/minecraft/world/phys/Vec3;)V"
            )
    )
    private void chromaticSubtitles$setColorWhenRefreshingExistingSubtitle(
            Subtitle subtitle,
            Vec3 location,
            Operation<Void> original,
            SoundInstance sound,
            WeighedSoundEvents soundEvent,
            float range
    ) {
        ((SubtitleColorAccess) subtitle).chromaticSubtitles$setColor(sound);
        original.call(subtitle, location);
    }

    @WrapOperation(
            method = "onPlaySound",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            )
    )
    private boolean chromaticSubtitles$setColorWhenAddingNewSubtitle(
            List<Subtitle> subtitles,
            Object subtitle,
            Operation<Boolean> original,
            SoundInstance sound,
            WeighedSoundEvents soundEvent,
            float range
    ) {
        if (subtitle instanceof SubtitleColorAccess access) {
            access.chromaticSubtitles$setColor(sound);
        }

        return original.call(subtitles, subtitle);
    }

    @Inject(
            method = "onPlaySound",
            at = @At("HEAD"),
            cancellable = true
    )
    private void chromaticSubtitles$hideSources(
            SoundInstance sound,
            WeighedSoundEvents soundEvent,
            float range,
            CallbackInfo ci
    ) {
        var config = ChromaticSubtitles.getConfig();

        if (config.enabled() && config.hiddenSources().contains(sound.getSource())) {
            ci.cancel();
        }
    }

    @WrapOperation(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/SubtitleOverlay$Subtitle;getText()Lnet/minecraft/network/chat/Component;"
            )
    )
    private Component chromaticSubtitles$trackRenderedSubtitle(
            Subtitle subtitle,
            Operation<Component> original
    ) {
        this.chromaticSubtitles$currentSubtitle = subtitle;
        return original.call(subtitle);
    }

    @WrapOperation(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"
            )
    )
    private void chromaticSubtitles$applyBackgroundColor(
            GuiGraphicsExtractor graphics,
            int x0,
            int y0,
            int x1,
            int y1,
            int col,
            Operation<Void> original
    ) {
        var config = ChromaticSubtitles.getConfig();

        if (!config.enabled()) {
            original.call(graphics, x0, y0, x1, y1, col);
            return;
        }

        if (config.hideBackground()) {
            return;
        }

        var color = this.chromaticSubtitles$modifyBackgroundColor(col);

        if (config.backgroundOpacity() >= 0) {
            var alpha = Math.round(255 * config.backgroundOpacity() / 100.0f);
            color = (alpha << 24) | (color & 0x00FFFFFF);
        }

        original.call(graphics, x0, y0, x1, y1, color);
    }

    @Unique
    private int chromaticSubtitles$modifyBackgroundColor(int vanillaColor) {
        var subtitle = this.chromaticSubtitles$currentSubtitle;

        if (!(subtitle instanceof SubtitleColorAccess access)) {
            return vanillaColor;
        }

        var backgroundColor = access.chromaticSubtitles$getBackgroundColor();

        if (backgroundColor < 0) {
            return vanillaColor;
        }

        return (vanillaColor & 0xFF000000) | (backgroundColor & 0x00FFFFFF);
    }

    @WrapOperation(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
            )
    )
    private void chromaticSubtitles$applySubtitleTextColor(
            GuiGraphicsExtractor graphics,
            Font font,
            Component str,
            int x,
            int y,
            int color,
            Operation<Void> original
    ) {
        var config = ChromaticSubtitles.getConfig();
        var modifiedColor = config.enabled()
                ? this.chromaticSubtitles$modifyTextColor(color)
                : color;

        if (config.enabled() && !config.textShadow()) {
            graphics.text(font, str, x, y, modifiedColor, false);
        } else {
            original.call(graphics, font, str, x, y, modifiedColor);
        }
    }

    @Unique
    private int chromaticSubtitles$modifyTextColor(int vanillaColor) {
        var subtitle = this.chromaticSubtitles$currentSubtitle;

        if (!(subtitle instanceof SubtitleColorAccess access)) {
            return vanillaColor;
        }

        var textColor = access.chromaticSubtitles$getTextColor();
        var brightness = vanillaColor & 0xFF;
        var alpha = vanillaColor & 0xFF000000;

        var red = ((textColor >> 16) & 0xFF) * brightness / 255;
        var green = ((textColor >> 8) & 0xFF) * brightness / 255;
        var blue = (textColor & 0xFF) * brightness / 255;

        return alpha | (red << 16) | (green << 8) | blue;
    }

    @WrapOperation(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
            )
    )
    private void chromaticSubtitles$applyIndicatorTextShadow(
            GuiGraphicsExtractor graphics,
            Font font,
            String str,
            int x,
            int y,
            int color,
            Operation<Void> original
    ) {
        var config = ChromaticSubtitles.getConfig();

        if (config.enabled() && !config.textShadow()) {
            graphics.text(font, str, x, y, color, false);
        } else {
            original.call(graphics, font, str, x, y, color);
        }
    }

    @Inject(
            method = "extractRenderState",
            at = @At("RETURN")
    )
    private void chromaticSubtitles$clearTrackedSubtitle(
            GuiGraphicsExtractor graphics,
            CallbackInfo ci
    ) {
        this.chromaticSubtitles$currentSubtitle = null;
    }

}
