// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.config;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import top.likoslupus.chromaticsubtitles.ChromaticSubtitles;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class ConfigManager {

    private static final String
            CONFIG_EXTENSION = ".toml",
            LEGACY_JSON_EXTENSION = ".json";

    private ConfigManager() {
    }

    public static ChromaticSubtitlesConfig load() {
        var configPath = ConfigManager.getConfigPath();

        if (Files.exists(configPath)) {
            return ConfigManager.readTomlOrDefault(configPath);
        }

        var migratedConfig = ConfigManager.migrateLegacyJsonConfig();
        if (migratedConfig.isPresent()) {
            var candidate = migratedConfig.get();

            if (ConfigManager.writeTomlOrWarn(configPath, candidate.config())) {
                ChromaticSubtitles.LOGGER.info("Migrated {} to Chromatic Subtitles TOML config", candidate.sourceDescription());
            }

            return candidate.config();
        }

        if (ConfigManager.writeTomlOrWarn(configPath, ChromaticSubtitlesConfig.DEFAULT)) {
            ChromaticSubtitles.LOGGER.warn("Could not find Chromatic Subtitles config; wrote default TOML config to file");
        }

        return ChromaticSubtitlesConfig.DEFAULT;
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance()
                .getConfigDir()
                .resolve(ChromaticSubtitles.MOD_ID + CONFIG_EXTENSION);
    }

    private static ChromaticSubtitlesConfig readTomlOrDefault(Path configPath) {
        try {
            return ChromaticSubtitlesToml.read(configPath);
        } catch (Exception exception) {
            ChromaticSubtitles.LOGGER.warn("Failed to read Chromatic Subtitles TOML config; falling back to default without overwriting the file", exception);
            return ChromaticSubtitlesConfig.DEFAULT;
        }
    }

    private static Optional<MigrationCandidate> migrateLegacyJsonConfig() {
        var legacyColorfulConfig = ConfigManager.readLegacyJsonConfig(
                ConfigManager.getLegacyColorfulJsonPath(),
                "legacy Colorful Subtitles JSON config"
        );

        if (legacyColorfulConfig.isPresent()) {
            return legacyColorfulConfig.map(config -> new MigrationCandidate(config, "legacy Colorful Subtitles JSON config"));
        }

        var devConfig = ConfigManager.readLegacyJsonConfig(
                ConfigManager.getDevJsonPath(),
                "development Chromatic Subtitles JSON config"
        );

        return devConfig.map(config -> new MigrationCandidate(config, "development Chromatic Subtitles JSON config"));
    }

    private static boolean writeTomlOrWarn(Path configPath, ChromaticSubtitlesConfig config) {
        try {
            ChromaticSubtitlesToml.write(configPath, config);
            return true;
        } catch (Exception exception) {
            ChromaticSubtitles.LOGGER.warn("Failed to write Chromatic Subtitles TOML config", exception);
            return false;
        }
    }

    private static Optional<ChromaticSubtitlesConfig> readLegacyJsonConfig(Path configPath, String sourceDescription) {
        if (Files.notExists(configPath)) {
            return Optional.empty();
        }

        try (var reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            var json = JsonParser.parseReader(reader);
            var config = ChromaticSubtitlesConfig.CODEC.decode(JsonOps.INSTANCE, json)
                    .getOrThrow()
                    .getFirst();

            return Optional.of(config);
        } catch (Exception exception) {
            ChromaticSubtitles.LOGGER.warn("Failed to read {}; skipping legacy config migration", sourceDescription, exception);
            return Optional.empty();
        }
    }

    private static Path getLegacyColorfulJsonPath() {
        return FabricLoader.getInstance()
                .getConfigDir()
                .resolve(ChromaticSubtitles.LEGACY_MOD_ID + LEGACY_JSON_EXTENSION);
    }

    private static Path getDevJsonPath() {
        return FabricLoader.getInstance()
                .getConfigDir()
                .resolve(ChromaticSubtitles.MOD_ID + LEGACY_JSON_EXTENSION);
    }

    private record MigrationCandidate(
            ChromaticSubtitlesConfig config,
            String sourceDescription
    ) {

    }

}
