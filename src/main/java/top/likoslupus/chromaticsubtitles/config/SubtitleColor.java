// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.config;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.Optional;

public record SubtitleColor(
        @NonNull TextColor text,
        @NonNull Optional<TextColor> background
) {

    public static final SubtitleColor DEFAULT = SubtitleColor.ofText(TextColor.fromRgb(0xFFFFFF));

    private static final Codec<SubtitleColor> RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TextColor.CODEC.fieldOf("text")
                    .forGetter(SubtitleColor::text),
            TextColor.CODEC.optionalFieldOf("background")
                    .forGetter(SubtitleColor::background)
    ).apply(instance, SubtitleColor::new));

    private static final Codec<SubtitleColor> SIMPLE_CODEC = TextColor.CODEC.xmap(
            SubtitleColor::ofText,
            SubtitleColor::text
    );

    public static final Codec<SubtitleColor> CODEC = Codec.either(RECORD_CODEC, SIMPLE_CODEC).xmap(
            either -> either.map(
                    left -> left,
                    right -> right
            ),
            color -> color.background().isEmpty()
                    ? Either.right(color)
                    : Either.left(color)
    );

    public SubtitleColor {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(background, "background");
    }

    public static @NonNull SubtitleColor ofText(ChatFormatting formatting) {
        return SubtitleColor.ofText(
                Objects.requireNonNull(TextColor.fromLegacyFormat(formatting), "formatting must be a color")
        );
    }

    public static @NonNull SubtitleColor ofText(TextColor text) {
        return new SubtitleColor(text, Optional.empty());
    }

}
