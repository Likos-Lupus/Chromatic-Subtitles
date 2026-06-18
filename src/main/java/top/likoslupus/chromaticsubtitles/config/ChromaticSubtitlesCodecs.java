// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import net.minecraft.sounds.SoundSource;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Codecs used for legacy JSON config migration.
 */
public final class ChromaticSubtitlesCodecs {

    private static final SoundSource[] SOUND_SOURCES = SoundSource.values();

    private static final Codec<SoundSource> SOUND_SOURCE = Codec.STRING.comapFlatMap(
            ChromaticSubtitlesCodecs::getSoundSource,
            SoundSource::getName
    );

    private static final Keyable SOUND_SOURCE_KEYS = new Keyable() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Arrays.stream(SOUND_SOURCES)
                    .map(SoundSource::getName)
                    .map(ops::createString);
        }
    };

    static final Codec<Map<SoundSource, SubtitleColor>> SOUND_SOURCE_TO_SUBTITLE_COLOR = Codec.simpleMap(
            SOUND_SOURCE,
            SubtitleColor.CODEC,
            SOUND_SOURCE_KEYS
    ).codec();

    private ChromaticSubtitlesCodecs() {
    }

    private static DataResult<SoundSource> getSoundSource(String name) {
        return SoundSourceNames.byName(name)
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "Unknown sound source '" + name + "'"));
    }

}
