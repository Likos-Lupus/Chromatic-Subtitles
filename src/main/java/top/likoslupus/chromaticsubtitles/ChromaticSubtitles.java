// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.likoslupus.chromaticsubtitles.config.ChromaticSubtitlesConfig;
import top.likoslupus.chromaticsubtitles.config.ConfigManager;

public final class ChromaticSubtitles {

    public static final String MOD_ID = "chromaticsubtitles";
    public static final String LEGACY_MOD_ID = "colorfulsubtitles";
    public static final String NAME = "Chromatic Subtitles";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    private static ChromaticSubtitlesConfig config;

    private ChromaticSubtitles() {
    }

    public static synchronized ChromaticSubtitlesConfig getConfig() {
        return config == null
                ? loadConfig()
                : config;
    }

    public static synchronized ChromaticSubtitlesConfig loadConfig() {
        config = ConfigManager.load();
        return config;
    }

    public static synchronized ChromaticSubtitlesConfig reloadConfig() {
        var reloadedConfig = loadConfig();
        LOGGER.info("Reloaded Chromatic Subtitles config");
        return reloadedConfig;
    }

}
