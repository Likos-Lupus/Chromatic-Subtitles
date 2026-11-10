// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import top.likoslupus.chromaticsubtitles.ChromaticSubtitles;
import top.likoslupus.chromaticsubtitles.client.ChromaticSubtitlesClothScreen;

import java.util.function.Supplier;

@Mod(
        value = ChromaticSubtitles.MOD_ID,
        dist = Dist.CLIENT
)
public final class ChromaticSubtitlesNeoForge {

    public ChromaticSubtitlesNeoForge(ModContainer container) {
        ChromaticSubtitles.initClient(FMLPaths.CONFIGDIR.get());

        if (ModList.get().isLoaded("cloth_config")) {
            Supplier<IConfigScreenFactory> factory = () -> (_, parent) -> {
                try {
                    return ChromaticSubtitlesClothScreen.create(parent);
                } catch (LinkageError | RuntimeException exception) {
                    ChromaticSubtitles.LOGGER.error(
                            "Failed to open the Cloth Config screen",
                            exception
                    );
                    return parent;
                }
            };
            container.registerExtensionPoint(IConfigScreenFactory.class, factory);
        }
    }

}
