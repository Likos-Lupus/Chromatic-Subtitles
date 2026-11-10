// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.util.NullScreenFactory;
import net.fabricmc.loader.api.FabricLoader;
import top.likoslupus.chromaticsubtitles.ChromaticSubtitles;
import top.likoslupus.chromaticsubtitles.client.ChromaticSubtitlesClothScreen;

/**
 * Optional Mod Menu integration. This class is only loaded when Mod Menu is installed; the Cloth
 * screen class is only resolved if Cloth Config is installed as well.
 */
public final class ChromaticSubtitlesModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
            return new NullScreenFactory<>();
        }

        return parent -> {
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
    }

}
