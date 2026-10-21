// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import top.likoslupus.chromaticsubtitles.ChromaticSubtitles;

public final class ChromaticSubtitlesFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ChromaticSubtitles.initClient(FabricLoader.getInstance().getConfigDir());
    }

}
