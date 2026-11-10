// SPDX-License-Identifier: MIT
// Copyright (c) 2021 haykam821
// Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
package top.likoslupus.chromaticsubtitles.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundSource;
import top.likoslupus.chromaticsubtitles.ChromaticSubtitles;
import top.likoslupus.chromaticsubtitles.config.ChromaticSubtitlesConfig;
import top.likoslupus.chromaticsubtitles.config.ConfigManager;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Builds the Cloth Config screen. This class must only be loaded after confirming that Cloth Config
 * is installed; see the Fabric and NeoForge entrypoints.
 */
public final class ChromaticSubtitlesClothScreen {

    private static final String PROJECT_URL =
            "https://github.com/Likos-Lupus/Chromatic-Subtitles";

    private static final List<SoundSource> CONFIGURABLE_SOURCES = List.of(
            SoundSource.MUSIC,
            SoundSource.RECORDS,
            SoundSource.WEATHER,
            SoundSource.BLOCKS,
            SoundSource.HOSTILE,
            SoundSource.NEUTRAL,
            SoundSource.PLAYERS,
            SoundSource.AMBIENT,
            SoundSource.VOICE,
            SoundSource.UI
    );

    private ChromaticSubtitlesClothScreen() {
    }

    public static Screen create(Screen parent) {
        var draft = new ConfigDraft(ChromaticSubtitles.getConfig());
        var builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("chromaticsubtitles.config.title"))
                .setSavingRunnable(() -> {
                    if (ConfigManager.save(draft.toConfig())) {
                        ChromaticSubtitles.reloadConfig();
                    }
                });
        var entryBuilder = builder.entryBuilder();

        addGeneral(builder, entryBuilder, draft);
        addColors(builder, entryBuilder, draft);
        addAdvanced(builder, entryBuilder, draft);
        addAbout(builder, entryBuilder);

        return builder.build();
    }

    private static void addGeneral(
            ConfigBuilder builder,
            ConfigEntryBuilder entryBuilder,
            ConfigDraft draft
    ) {
        var general = builder.getOrCreateCategory(
                Component.translatable("chromaticsubtitles.config.category.general")
        );

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("chromaticsubtitles.config.option.enabled"),
                        draft.enabled()
                )
                .setDefaultValue(true)
                .setTooltip(Component.translatable(
                        "chromaticsubtitles.config.option.enabled.tooltip"
                ))
                .setSaveConsumer(draft::setEnabled)
                .build());

        var defaultTextRgb = draft.defaultColor().text().getValue() & 0xFFFFFF;
        var defaultTextDefault =
                ChromaticSubtitlesConfig.DEFAULT.defaultColor().text().getValue() & 0xFFFFFF;

        general.addEntry(entryBuilder
                .startColorField(
                        Component.translatable("chromaticsubtitles.config.option.default_text_color"),
                        defaultTextRgb
                )
                .setDefaultValue(defaultTextDefault)
                .setTooltip(Component.translatable(
                        "chromaticsubtitles.config.default_value",
                        hex(defaultTextDefault)
                ))
                .setSaveConsumer(rgb -> {
                    if (rgb >= 0) {
                        draft.setDefaultTextColor(rgb);
                    }
                })
                .build());

        var defaultBackgroundRgb = draft.defaultBackgroundRgb();
        var defaultBackgroundDefault = ChromaticSubtitlesConfig.DEFAULT.defaultColor()
                .background()
                .map(TextColor::getValue)
                .orElse(0x000000)
                & 0xFFFFFF;

        var defaultBackgroundToggle = entryBuilder
                .startBooleanToggle(
                        Component.translatable("chromaticsubtitles.config.option.default_background"),
                        draft.defaultHasBackground()
                )
                .setDefaultValue(false)
                .setTooltip(Component.translatable(
                        "chromaticsubtitles.config.option.default_background.tooltip"
                ))
                .setSaveConsumer(enabled -> draft.setDefaultBackground(
                        enabled
                                ? defaultBackgroundRgb
                                : null
                ))
                .build();

        general.addEntry(defaultBackgroundToggle);

        //noinspection UnstableApiUsage
        general.addEntry(entryBuilder
                .startColorField(
                        Component.translatable(
                                "chromaticsubtitles.config.option.default_background_color"
                        ),
                        defaultBackgroundRgb
                )
                .setDefaultValue(defaultBackgroundDefault)
                .setRequirement(defaultBackgroundToggle::getValue)
                .setTooltip(Component.translatable(
                        "chromaticsubtitles.config.default_value",
                        hex(defaultBackgroundDefault)
                ))
                .setSaveConsumer(rgb -> {
                    if (defaultBackgroundToggle.getValue() && rgb >= 0) {
                        draft.setDefaultBackground(rgb);
                    }
                })
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("chromaticsubtitles.config.option.text_shadow"),
                        draft.textShadow()
                )
                .setDefaultValue(true)
                .setTooltip(Component.translatable(
                        "chromaticsubtitles.config.option.text_shadow.tooltip"
                ))
                .setSaveConsumer(draft::setTextShadow)
                .build());

        general.addEntry(entryBuilder
                .startIntSlider(
                        Component.translatable("chromaticsubtitles.config.option.background_opacity"),
                        draft.backgroundOpacity(),
                        -1,
                        100
                )
                .setDefaultValue(-1)
                .setTextGetter(value -> value < 0
                        ? Component.translatable("chromaticsubtitles.config.opacity.vanilla")
                        : Component.literal(value + "%"))
                .setTooltip(Component.translatable(
                        "chromaticsubtitles.config.option.background_opacity.tooltip"
                ))
                .setSaveConsumer(draft::setBackgroundOpacity)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("chromaticsubtitles.config.option.hide_background"),
                        draft.hideBackground()
                )
                .setDefaultValue(false)
                .setTooltip(Component.translatable(
                        "chromaticsubtitles.config.option.hide_background.tooltip"
                ))
                .setSaveConsumer(draft::setHideBackground)
                .build());

        general.addEntry(entryBuilder
                .startTextDescription(Component.translatable(
                        "chromaticsubtitles.config.general.note",
                        ConfigManager.getPath().toString()
                ))
                .build());
    }

    private static void addColors(
            ConfigBuilder builder,
            ConfigEntryBuilder entryBuilder,
            ConfigDraft draft
    ) {
        var colors = builder.getOrCreateCategory(
                Component.translatable("chromaticsubtitles.config.category.colors")
        );

        CONFIGURABLE_SOURCES.forEach(source -> {
            var current = draft.color(source);
            var textRgb = current.text().getValue() & 0xFFFFFF;
            var textDefault = ChromaticSubtitlesConfig.DEFAULT
                    .getColorForSource(source)
                    .text()
                    .getValue()
                    & 0xFFFFFF;
            var backgroundRgb = draft.backgroundRgb(source);
            var backgroundDefault = ChromaticSubtitlesConfig.DEFAULT
                    .getColorForSource(source)
                    .background()
                    .map(TextColor::getValue)
                    .orElse(0x000000)
                    & 0xFFFFFF;
            var subCategory = entryBuilder
                    .startSubCategory(Component.translatable("soundCategory." + source.getName()))
                    .setExpanded(false);
            subCategory.add(entryBuilder
                    .startColorField(
                            Component.translatable("chromaticsubtitles.config.option.text_color"),
                            textRgb
                    )
                    .setDefaultValue(textDefault)
                    .setTooltip(Component.translatable(
                            "chromaticsubtitles.config.default_value",
                            hex(textDefault)
                    ))
                    .setSaveConsumer(rgb -> {
                        if (rgb >= 0) {
                            draft.setTextColor(source, rgb);
                        }
                    })
                    .build());
            var backgroundToggle = entryBuilder
                    .startBooleanToggle(
                            Component.translatable(
                                    "chromaticsubtitles.config.option.custom_background"
                            ),
                            draft.hasCustomBackground(source)
                    )
                    .setDefaultValue(false)
                    .setTooltip(Component.translatable(
                            "chromaticsubtitles.config.option.custom_background.tooltip"
                    ))
                    .setSaveConsumer(enabled -> draft.setBackground(
                            source,
                            enabled
                                    ? backgroundRgb
                                    : null
                    ))
                    .build();
            subCategory.add(backgroundToggle);
            //noinspection UnstableApiUsage
            subCategory.add(entryBuilder
                    .startColorField(
                            Component.translatable(
                                    "chromaticsubtitles.config.option.background_color"
                            ),
                            backgroundRgb
                    )
                    .setDefaultValue(backgroundDefault)
                    .setRequirement(backgroundToggle::getValue)
                    .setTooltip(Component.translatable(
                            "chromaticsubtitles.config.default_value",
                            hex(backgroundDefault)
                    ))
                    .setSaveConsumer(rgb -> {
                        if (backgroundToggle.getValue() && rgb >= 0) {
                            draft.setBackground(source, rgb);
                        }
                    })
                    .build());
            colors.addEntry(subCategory.build());
        });
    }

    private static void addAdvanced(
            ConfigBuilder builder,
            ConfigEntryBuilder entryBuilder,
            ConfigDraft draft
    ) {
        var advanced = builder.getOrCreateCategory(
                Component.translatable("chromaticsubtitles.config.category.advanced")
        );

        var hiddenSources = entryBuilder
                .startSubCategory(Component.translatable(
                        "chromaticsubtitles.config.option.hidden_sources"))
                .setExpanded(false)
                .setTooltip(Component.translatable(
                        "chromaticsubtitles.config.option.hidden_sources.tooltip"
                ));

        CONFIGURABLE_SOURCES.stream()
                .map(source -> entryBuilder
                        .startBooleanToggle(
                                Component.translatable("soundCategory." + source.getName()),
                                draft.isHidden(source)
                        )
                        .setDefaultValue(false)
                        .setTooltip(Component.translatable(
                                "chromaticsubtitles.config.option.hidden_source.tooltip"
                        ))
                        .setSaveConsumer(hidden -> draft.setHidden(source, hidden))
                        .build()
                )
                .forEach(hiddenSources::add);

        advanced.addEntry(hiddenSources.build());

        advanced.addEntry(entryBuilder
                .startStrList(
                        Component.translatable("chromaticsubtitles.config.option.sound_overrides"),
                        SoundOverrideFormat.format(draft.soundOverrides())
                )
                .setDefaultValue(List.of())
                .setCellErrorSupplier(line -> SoundOverrideFormat.isValid(line)
                        ? Optional.empty()
                        : Optional.of(Component.translatable(
                                "chromaticsubtitles.config.option.sound_overrides.invalid"
                        ))
                )
                .setTooltip(Component.translatable(
                        "chromaticsubtitles.config.option.sound_overrides.tooltip"
                ))
                .setSaveConsumer(values -> draft.setSoundOverrides(
                        SoundOverrideFormat.parseAll(values)
                ))
                .build());

        advanced.addEntry(entryBuilder
                .startTextDescription(Component.translatable(
                        "chromaticsubtitles.config.option.sound_overrides.format"
                ))
                .build());
    }

    private static void addAbout(ConfigBuilder builder, ConfigEntryBuilder entryBuilder) {
        var about = builder.getOrCreateCategory(
                Component.translatable("chromaticsubtitles.config.category.about")
        );

        about.addEntry(entryBuilder
                .startTextDescription(Component.translatable(
                        "chromaticsubtitles.config.about.description",
                        ChromaticSubtitles.NAME,
                        ConfigManager.getPath().toString()
                ))
                .build());

        var projectLink = Component.literal(PROJECT_URL)
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create(PROJECT_URL)))
                        .withUnderlined(true)
                        .withColor(ChatFormatting.AQUA));

        about.addEntry(entryBuilder
                .startTextDescription(Component.translatable(
                        "chromaticsubtitles.config.about.links",
                        projectLink
                ))
                .build());
    }

    private static String hex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

}
