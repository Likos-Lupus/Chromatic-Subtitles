// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.config;

import net.minecraft.sound.SoundCategory;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Optional;

public final class SoundCategoryNames {

    private SoundCategoryNames() {
    }

    public static @NonNull Optional<SoundCategory> byName(String name) {
        return Arrays.stream(SoundCategory.values())
                .filter(category -> category.getName().equals(name))
                .findFirst();
    }

}
