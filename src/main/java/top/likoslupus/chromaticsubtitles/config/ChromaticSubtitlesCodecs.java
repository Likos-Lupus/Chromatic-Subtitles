// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import net.minecraft.sound.SoundCategory;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Codecs used for legacy JSON config migration.
 */
public final class ChromaticSubtitlesCodecs {

    private static final SoundCategory[] SOUND_CATEGORIES = SoundCategory.values();

    private static final Codec<SoundCategory> SOUND_CATEGORY = Codec.STRING.comapFlatMap(
            ChromaticSubtitlesCodecs::getSoundCategory,
            SoundCategory::getName
    );

    private static final Keyable SOUND_CATEGORY_KEYS = new Keyable() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Arrays.stream(SOUND_CATEGORIES)
                    .map(SoundCategory::getName)
                    .map(ops::createString);
        }
    };

    static final Codec<Map<SoundCategory, SubtitleColor>> SOUND_CATEGORY_TO_SUBTITLE_COLOR = Codec.simpleMap(
            SOUND_CATEGORY,
            SubtitleColor.CODEC,
            SOUND_CATEGORY_KEYS
    ).codec();

    private ChromaticSubtitlesCodecs() {
    }

    private static DataResult<SoundCategory> getSoundCategory(String name) {
        return SoundCategoryNames.byName(name)
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "Unknown sound category '" + name + "'"));
    }

}
