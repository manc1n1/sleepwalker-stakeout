/*
 * Copyright (c) 2026, manc1n1 https://github.com/manc1n1
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:

 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.

 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sleepwalkerstakeout;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.client.audio.AudioPlayer;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SleepwalkerStakeoutSoundPlayer {
    private static final String DEFAULT_SOUND = "sleepwalker.wav";

    static final Path SOUND_DIR =
            RuneLite.RUNELITE_DIR.toPath()
                    .resolve("sleepwalker-stakeout")
                    .resolve("sounds")
                    .normalize();

    private final AudioPlayer audioPlayer;
    private final ScheduledExecutorService executor;

    @Inject
    SleepwalkerStakeoutSoundPlayer(
            AudioPlayer audioPlayer,
            ScheduledExecutorService executor
    ) {
        this.audioPlayer = audioPlayer;
        this.executor = executor;
    }

    void initialize() {
        try {
            Files.createDirectories(SOUND_DIR);
        } catch (IOException ex) {
            log.warn(
                    "Unable to create sound directory: {}",
                    SOUND_DIR,
                    ex
            );
            return;
        }

        copyDefaultSound();
    }

    private void copyDefaultSound() {
        final Path defaultSound =
                SOUND_DIR.resolve(DEFAULT_SOUND);

        if (Files.exists(defaultSound)) {
            return;
        }

        try (
                InputStream inputStream =
                        SleepwalkerStakeoutSoundPlayer.class
                                .getResourceAsStream(
                                        "/com/sleepwalkerstakeout/sound/"
                                                + DEFAULT_SOUND
                                )
        ) {
            if (inputStream == null) {
                log.warn(
                        "Bundled default sound could not be found: {}",
                        DEFAULT_SOUND
                );
                return;
            }

            Files.copy(
                    inputStream,
                    defaultSound
            );

            log.debug(
                    "Copied bundled default sound to: {}",
                    defaultSound
            );
        } catch (IOException ex) {
            log.warn(
                    "Unable to copy bundled default sound",
                    ex
            );
        }
    }

    List<String> getAvailableSounds() {
        initialize();

        try (
                Stream<Path> paths =
                        Files.list(SOUND_DIR)
        ) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(fileName ->
                            fileName
                                    .toLowerCase(Locale.ROOT)
                                    .endsWith(".wav")
                    )
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            log.warn(
                    "Unable to read sound directory: {}",
                    SOUND_DIR,
                    ex
            );

            return List.of();
        }
    }

    void play(
            String fileName,
            int volume
    ) {
        play(
                fileName,
                volume,
                null
        );
    }

    void play(
            String fileName,
            int volume,
            Consumer<String> onError
    ) {
        if (fileName == null || fileName.isBlank()) {
            reportError(
                    "No sound file selected",
                    onError
            );
            return;
        }

        if (!fileName
                .toLowerCase(Locale.ROOT)
                .endsWith(".wav")) {
            reportError(
                    "Selected file is not a WAV file",
                    onError
            );
            return;
        }

        final Path soundPath =
                SOUND_DIR.resolve(fileName)
                        .normalize();

        if (!SOUND_DIR.equals(soundPath.getParent())) {
            reportError(
                    "Selected sound is outside the sounds folder",
                    onError
            );
            return;
        }

        if (!Files.isRegularFile(soundPath)) {
            reportError(
                    "Sound file does not exist",
                    onError
            );
            return;
        }

        if (!Files.isReadable(soundPath)) {
            reportError(
                    "Sound file cannot be read",
                    onError
            );
            return;
        }

        try {
            final Path realDirectory =
                    SOUND_DIR.toRealPath();

            final Path realSound =
                    soundPath.toRealPath();

            if (!realDirectory.equals(realSound.getParent())) {
                reportError(
                        "Selected sound is outside the sounds folder",
                        onError
                );
                return;
            }
        } catch (IOException ex) {
            log.warn(
                    "Unable to resolve sound: {}",
                    soundPath,
                    ex
            );

            reportError(
                    "Unable to resolve sound file",
                    onError
            );
            return;
        }

        final int clampedVolume =
                Math.max(
                        0,
                        Math.min(volume, 100)
                );

        if (clampedVolume == 0) {
            return;
        }

        final float gain =
                volumeToGain(clampedVolume);

        executor.execute(() -> {
            try {
                audioPlayer.play(
                        soundPath.toFile(),
                        gain
                );
            } catch (Exception ex) {
                log.warn(
                        "Unable to play sound: {}",
                        soundPath,
                        ex
                );

                reportError(
                        "Unsupported or unreadable WAV file",
                        onError
                );
            }
        });
    }

    private float volumeToGain(int volume) {
        if (volume >= 100) {
            return 0.0f;
        }

        final double linearVolume =
                volume / 100.0;

        return (float) (
                20.0 * Math.log10(linearVolume)
        );
    }

    private void reportError(
            String message,
            Consumer<String> onError
    ) {
        log.warn(message);

        if (onError != null) {
            onError.accept(message);
        }
    }
}