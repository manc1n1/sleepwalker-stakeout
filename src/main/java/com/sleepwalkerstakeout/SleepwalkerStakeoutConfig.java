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

import net.runelite.client.config.*;

@ConfigGroup(SleepwalkerStakeoutConfig.GROUP)
public interface SleepwalkerStakeoutConfig extends Config {
    String GROUP = "sleepwalkerstakeout";

    String SELECTED_SOUND_KEY = "selectedSound";

    @ConfigItem(
            keyName = "showForAllTargets",
            name = "Show for all targets",
            description = "Bypass the Sleepwalker-only restriction for supported attacks",
            position = 0
    )
    default boolean showForAllTargets() {
        return false;
    }

    @ConfigItem(
            keyName = "playSound",
            name = "Play sound",
            description = "Play the selected sound when a fake XP drop appears",
            position = 1
    )
    default boolean playSound() {
        return false;
    }

    @Range(
            min = 0,
            max = 100
    )
    @Units(Units.PERCENT)
    @ConfigItem(
            keyName = "soundVolume",
            name = "Sound volume",
            description = "Adjust the volume of the sound",
            position = 2
    )
    default int soundVolume() {
        return 50;
    }

    @ConfigItem(
            keyName = "showPluginPanel",
            name = "Show plugin panel",
            description = "Show the plugin panel in the side nav. The entire plugin will still operate if the panel is hidden",
            position = 3
    )
    default boolean showPluginPanel() {
        return true;
    }

    @ConfigItem(
            keyName = SELECTED_SOUND_KEY,
            name = "Selected sound",
            description = "Currently selected sound",
            hidden = true
    )
    default String selectedSound() {
        return "sleepwalker.wav";
    }

    @ConfigItem(
            keyName = "showUpdateMessages",
            name = "Show update messages",
            description = "Show a one-time chat message when Sleepwalker Stakeout is updated",
            position = 4
    )
    default boolean showUpdateMessages() {
        return true;
    }
}