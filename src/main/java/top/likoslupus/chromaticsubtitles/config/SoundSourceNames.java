// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.config;

import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Optional;

public final class SoundSourceNames {

    private SoundSourceNames() {
    }

    public static @NonNull Optional<SoundSource> byName(String name) {
        return Arrays.stream(SoundSource.values())
                .filter(source -> source.getName().equals(name))
                .findFirst();
    }

}
