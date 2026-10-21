// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import top.likoslupus.chromaticsubtitles.ChromaticSubtitles;

@Mod(
        value = ChromaticSubtitles.MOD_ID,
        dist = Dist.CLIENT
)
public final class ChromaticSubtitlesNeoForge {

    public ChromaticSubtitlesNeoForge() {
        ChromaticSubtitles.initClient(FMLPaths.CONFIGDIR.get());
    }

}
