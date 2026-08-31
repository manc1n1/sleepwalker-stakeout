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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class SleepwalkerStakeoutOverlay extends Overlay {
    private static final long DROP_DURATION_NANOS = 1_500_000_000L;

    private static final double SPRITE_SCALE = 0.85;
    private static final double FADE_START = 0.75;

    private static final int TRAVEL_Y = 65;
    private static final int SPRITE_AREA_SIZE = 64;
    private static final int PLAYER_HEIGHT_OFFSET = 20;

    private static final BufferedImage POSITIONING_IMAGE =
            new BufferedImage(
                    SPRITE_AREA_SIZE,
                    SPRITE_AREA_SIZE,
                    BufferedImage.TYPE_INT_ARGB
            );

    private final Client client;
    private final ItemManager itemManager;

    private final List<FakeDrop> drops =
            new CopyOnWriteArrayList<>();

    private final Map<Integer, BufferedImage> sprites =
            new HashMap<>();

    private boolean usingAutomaticLocation;
    private Point lastAutomaticLocation;

    @Inject
    SleepwalkerStakeoutOverlay(
            Client client,
            ItemManager itemManager
    ) {
        this.client = client;
        this.itemManager = itemManager;

        setPosition(OverlayPosition.DYNAMIC);
        setMovable(true);
        setSnappable(true);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        setPriority(PRIORITY_HIGHEST);
    }

    void addDrop(int itemId) {
        final BufferedImage sprite = getSprite(itemId);

        if (sprite == null) {
            return;
        }

        drops.add(
                new FakeDrop(
                        System.nanoTime(),
                        sprite
                )
        );
    }

    void clear() {
        drops.clear();
        sprites.clear();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        final Dimension size = new Dimension(
                SPRITE_AREA_SIZE,
                SPRITE_AREA_SIZE + TRAVEL_Y
        );

        updateAutomaticLocation();

        if (drops.isEmpty()) {
            return size;
        }

        final long now = System.nanoTime();

        drops.removeIf(
                drop ->
                        now - drop.startTime
                                >= DROP_DURATION_NANOS
        );

        if (drops.isEmpty()) {
            return size;
        }

        final Composite oldComposite = graphics.getComposite();

        try {
            for (FakeDrop drop : drops) {
                renderDrop(
                        graphics,
                        drop,
                        now
                );
            }
        } finally {
            graphics.setComposite(oldComposite);
        }

        return size;
    }

    private BufferedImage getSprite(int itemId) {
        return sprites.computeIfAbsent(
                itemId,
                itemManager::getImage
        );
    }

    private void renderDrop(
            Graphics2D graphics,
            FakeDrop drop,
            long now
    ) {
        final double rawProgress =
                (double) (now - drop.startTime)
                        / DROP_DURATION_NANOS;

        final double progress = Math.max(
                0.0,
                Math.min(1.0, rawProgress)
        );

        final float alpha = progress < FADE_START
                ? 1.0f
                : (float) (
                (1.0 - progress)
                        / (1.0 - FADE_START)
        );

        final int width = (int) Math.round(
                drop.sprite.getWidth() * SPRITE_SCALE
        );

        final int height = (int) Math.round(
                drop.sprite.getHeight() * SPRITE_SCALE
        );

        final int x =
                (SPRITE_AREA_SIZE - width) / 2;

        final int startY =
                TRAVEL_Y
                        + (SPRITE_AREA_SIZE - height) / 2;

        final int y =
                startY
                        - (int) Math.round(
                        progress * TRAVEL_Y
                );

        graphics.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        alpha
                )
        );

        graphics.drawImage(
                drop.sprite,
                x,
                y,
                width,
                height,
                null
        );
    }

    private void updateAutomaticLocation() {
        if (getPreferredPosition() != null) {
            usingAutomaticLocation = false;
            lastAutomaticLocation = null;
            return;
        }

        final Point preferredLocation =
                getPreferredLocation();

        final Point automaticLocation =
                getAutomaticLocation();

        if (automaticLocation == null) {
            return;
        }

        if (preferredLocation == null) {
            setPreferredLocation(automaticLocation);

            usingAutomaticLocation = true;
            lastAutomaticLocation = automaticLocation;

            return;
        }

        if (!usingAutomaticLocation) {
            return;
        }

        if (lastAutomaticLocation != null
                && !preferredLocation.equals(lastAutomaticLocation)) {
            usingAutomaticLocation = false;
            lastAutomaticLocation = null;
            return;
        }

        if (!preferredLocation.equals(automaticLocation)) {
            setPreferredLocation(automaticLocation);
        }

        lastAutomaticLocation = automaticLocation;
    }

    private Point getAutomaticLocation() {
        final Player localPlayer = client.getLocalPlayer();

        if (localPlayer == null) {
            return null;
        }

        final int heightOffset =
                localPlayer.getLogicalHeight()
                        + PLAYER_HEIGHT_OFFSET;

        final net.runelite.api.Point imageLocation =
                localPlayer.getCanvasImageLocation(
                        POSITIONING_IMAGE,
                        heightOffset
                );

        if (imageLocation == null) {
            return null;
        }

        return new Point(
                imageLocation.getX(),
                imageLocation.getY() - TRAVEL_Y
        );
    }

    private static class FakeDrop {
        private final long startTime;
        private final BufferedImage sprite;

        private FakeDrop(
                long startTime,
                BufferedImage sprite
        ) {
            this.startTime = startTime;
            this.sprite = sprite;
        }
    }
}