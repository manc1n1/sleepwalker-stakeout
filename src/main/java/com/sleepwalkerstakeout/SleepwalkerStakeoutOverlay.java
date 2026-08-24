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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class SleepwalkerStakeoutOverlay extends Overlay {
    private static final long DROP_DURATION_NANOS = 1_500_000_000L;

    private static final double DEFAULT_X = 0.67;
    private static final double DEFAULT_Y = 0.16;
    private static final double SPRITE_SCALE = 0.85;
    private static final double FADE_START = 0.75;

    private static final int TRAVEL_Y = 65;
    private static final int FALLBACK_SIZE = 64;

    private final Client client;
    private final ItemManager itemManager;
    private final List<Long> drops = new CopyOnWriteArrayList<>();

    private BufferedImage stakeSprite;

    private boolean usingAutomaticLocation;
    private Point lastAutomaticLocation;

    @Inject
    SleepwalkerStakeoutOverlay(Client client, ItemManager itemManager) {
        this.client = client;
        this.itemManager = itemManager;

        setPosition(OverlayPosition.DYNAMIC);
        setMovable(true);
        setSnappable(false);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        setPriority(PRIORITY_HIGHEST);
    }

    void prepareSprite() {
        if (stakeSprite == null) {
            stakeSprite = itemManager.getImage(ItemID.BLISTERWOOD_STAKE);
        }
    }

    void addDrop() {
        prepareSprite();

        if (stakeSprite != null) {
            drops.add(System.nanoTime());
        }
    }

    void clear() {
        drops.clear();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        final int width = stakeSprite == null
                ? FALLBACK_SIZE
                : (int) Math.round(stakeSprite.getWidth() * SPRITE_SCALE);

        final int height = stakeSprite == null
                ? FALLBACK_SIZE
                : (int) Math.round(stakeSprite.getHeight() * SPRITE_SCALE);

        final Dimension size = new Dimension(width, height + TRAVEL_Y);

        updateAutomaticLocation(size);

        if (stakeSprite == null || drops.isEmpty()) {
            return size;
        }

        final long now = System.nanoTime();

        drops.removeIf(start -> now - start >= DROP_DURATION_NANOS);

        if (drops.isEmpty()) {
            return size;
        }

        final Composite oldComposite = graphics.getComposite();

        try {
            for (long start : drops) {
                final double rawProgress =
                        (double) (now - start) / DROP_DURATION_NANOS;

                final double progress = Math.max(
                        0.0,
                        Math.min(1.0, rawProgress)
                );

                final float alpha = progress < FADE_START
                        ? 1.0f
                        : (float) ((1.0 - progress) / (1.0 - FADE_START));

                final int y = TRAVEL_Y
                        - (int) Math.round(progress * TRAVEL_Y);

                graphics.setComposite(
                        AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER,
                                alpha
                        )
                );

                graphics.drawImage(
                        stakeSprite,
                        0,
                        y,
                        width,
                        height,
                        null
                );
            }
        } finally {
            graphics.setComposite(oldComposite);
        }

        return size;
    }

    private void updateAutomaticLocation(Dimension size) {
        final Point preferredLocation = getPreferredLocation();
        final Point automaticLocation = getAutomaticLocation(size);

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
            return;
        }

        if (!preferredLocation.equals(automaticLocation)) {
            setPreferredLocation(automaticLocation);
        }

        lastAutomaticLocation = automaticLocation;
    }

    private Point getAutomaticLocation(Dimension size) {
        final int canvasWidth = client.getCanvasWidth();
        final int canvasHeight = client.getCanvasHeight();

        final int x = (int) Math.round(canvasWidth * DEFAULT_X)
                - size.width / 2;

        final int y = (int) Math.round(canvasHeight * DEFAULT_Y)
                - size.height / 2;

        final int maxX = Math.max(0, canvasWidth - size.width);
        final int maxY = Math.max(0, canvasHeight - size.height);

        return new Point(
                Math.max(0, Math.min(x, maxX)),
                Math.max(0, Math.min(y, maxY))
        );
    }
}