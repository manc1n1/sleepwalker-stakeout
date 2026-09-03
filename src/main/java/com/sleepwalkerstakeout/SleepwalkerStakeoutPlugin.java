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

import com.google.inject.Provides;

import java.util.Set;
import javax.inject.Inject;

import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "Sleepwalker Stakeout",
        description = "Weapon sprites as fake XP drops",
        tags = {
                "phosani",
                "nightmare",
                "pnm",
                "sleepwalker",
                "blisterwood",
                "stake",
                "ayak",
                "blowpipe",
                "bow",
                "dart",
                "boss",
                "fake",
                "xp",
                "drop",
                "tag",
                "npc"
        }
)

public class SleepwalkerStakeoutPlugin extends Plugin {
    private static final String CONFIG_LAST_SEEN_VERSION = "lastSeenVersion";

    private static final String PLUGIN_VERSION = "1.2.0";

    private static final String UPDATE_MESSAGE =
            "<colHIGHLIGHT>Sleepwalker Stakeout v" + PLUGIN_VERSION + ":<br>"
                    + "<colHIGHLIGHT>* Added a setting to toggle fake XP drops for all targets.<br>"
                    + "<colHIGHLIGHT>* Sleepwalker-only targeting remains enabled by default.<br>"
                    + "<colHIGHLIGHT>* Added an option to disable future plugin update messages.";

    private static final int TARGET_NPC_ID = 9470; // Sleepwalker (Phosani's Nightmare)

    private static final int BLISTERWOOD_STAKE_ANIMATION = 7617;
    private static final int EYE_OF_AYAK_ANIMATION = 12397;
    private static final int BLOWPIPE_ANIMATION = 5061;
    private static final int BLAZING_BLOWPIPE_ANIMATION = 10656;
    private static final int BOW_ANIMATION = 426;
    private static final int DART_ANIMATION = 7554;

    private static final Set<Integer> SUPPORTED_ANIMATIONS = Set.of(
            BLISTERWOOD_STAKE_ANIMATION,
            EYE_OF_AYAK_ANIMATION,
            BLOWPIPE_ANIMATION,
            BLAZING_BLOWPIPE_ANIMATION,
            BOW_ANIMATION,
            DART_ANIMATION
    );

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private SleepwalkerStakeoutOverlay overlay;

    @Inject
    private SleepwalkerStakeoutConfig config;

    @Provides
    SleepwalkerStakeoutConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(
                SleepwalkerStakeoutConfig.class
        );
    }

    @Override
    protected void startUp() {
        overlayManager.add(overlay);

        clientThread.invokeLater(this::showUpdateMessageIfNeeded);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
        overlay.clear();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN) {
            showUpdateMessageIfNeeded();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        final Player localPlayer = client.getLocalPlayer();

        if (localPlayer == null || event.getActor() != localPlayer) {
            return;
        }

        final int animation = localPlayer.getAnimation();

        if (!SUPPORTED_ANIMATIONS.contains(animation)) {
            return;
        }

        if (!config.showForAllTargets()
                && !isAttackingTargetNpc(localPlayer)) {
            return;
        }

        final int weaponId = getEquippedWeaponId();

        if (weaponId == -1) {
            return;
        }

        overlay.addDrop(weaponId);
    }

    private void showUpdateMessageIfNeeded() {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        final String lastSeenVersion = configManager.getConfiguration(
                SleepwalkerStakeoutConfig.GROUP,
                CONFIG_LAST_SEEN_VERSION
        );

        if (lastSeenVersion == null) {
            configManager.setConfiguration(
                    SleepwalkerStakeoutConfig.GROUP,
                    CONFIG_LAST_SEEN_VERSION,
                    PLUGIN_VERSION
            );
            return;
        }

        if (PLUGIN_VERSION.equals(lastSeenVersion)) {
            return;
        }

        configManager.setConfiguration(
                SleepwalkerStakeoutConfig.GROUP,
                CONFIG_LAST_SEEN_VERSION,
                PLUGIN_VERSION
        );

        if (!config.showUpdateMessages()) {
            return;
        }

        chatMessageManager.queue(
                QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(UPDATE_MESSAGE)
                        .build()
        );
    }

    private boolean isAttackingTargetNpc(Player localPlayer) {
        final Actor interacting = localPlayer.getInteracting();

        if (!(interacting instanceof NPC)) {
            return false;
        }

        final NPC npc = (NPC) interacting;

        return npc.getId() == TARGET_NPC_ID;
    }

    private int getEquippedWeaponId() {
        final ItemContainer equipment =
                client.getItemContainer(InventoryID.WORN);

        if (equipment == null) {
            return -1;
        }

        final Item weapon = equipment.getItem(
                EquipmentInventorySlot.WEAPON.getSlotIdx()
        );

        return weapon == null ? -1 : weapon.getId();
    }
}