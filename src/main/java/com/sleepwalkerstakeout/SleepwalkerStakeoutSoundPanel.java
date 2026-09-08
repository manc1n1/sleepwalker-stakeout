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
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

@Slf4j
public class SleepwalkerStakeoutSoundPanel extends PluginPanel {
    private static final String NO_SOUNDS = "No .wav files found";

    private final ConfigManager configManager;
    private final SleepwalkerStakeoutConfig config;
    private final SleepwalkerStakeoutSoundPlayer soundPlayer;

    private final JComboBox<String> soundComboBox =
            new JComboBox<>();

    private final JButton previewButton =
            new JButton("Preview");

    private final JLabel statusLabel =
            new JLabel();

    private boolean reloading;

    @Inject
    SleepwalkerStakeoutSoundPanel(
            ConfigManager configManager,
            SleepwalkerStakeoutConfig config,
            SleepwalkerStakeoutSoundPlayer soundPlayer
    ) {
        super();

        this.configManager = configManager;
        this.config = config;
        this.soundPlayer = soundPlayer;

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        final JPanel content =
                new JPanel();

        content.setLayout(
                new BoxLayout(content, BoxLayout.Y_AXIS)
        );

        content.setBorder(
                new EmptyBorder(10, 10, 10, 10)
        );

        /*
         * Header
         */
        final JPanel headerPanel =
                new JPanel(
                        new BorderLayout()
                );

        headerPanel.setAlignmentX(LEFT_ALIGNMENT);
        headerPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 30)
        );

        final JLabel title =
                new JLabel("Sleepwalker Stakeout");

        title.setFont(
                title.getFont().deriveFont(Font.BOLD)
        );

        headerPanel.add(
                title,
                BorderLayout.WEST
        );

        /*
         * Header buttons
         */
        final JPanel headerButtons =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                4,
                                0
                        )
                );

        final BufferedImage refreshImage =
                ImageUtil.loadImageResource(
                        getClass(),
                        "/com/sleepwalkerstakeout/icons/reload_icon.png"
                );

        final BufferedImage folderImage =
                ImageUtil.loadImageResource(
                        getClass(),
                        "/com/sleepwalkerstakeout/icons/folder_icon.png"
                );

        final JButton reloadButton =
                createIconButton(
                        refreshImage,
                        "Reload sounds from disk"
                );

        reloadButton.addActionListener(
                event -> reloadSounds()
        );

        final JButton openFolderButton =
                createIconButton(
                        folderImage,
                        "View the plugin directory, where sound files should be placed, in the system file browser"
                );

        openFolderButton.addActionListener(
                event -> openSoundDirectory()
        );

        headerButtons.add(reloadButton);
        headerButtons.add(openFolderButton);

        headerPanel.add(
                headerButtons,
                BorderLayout.EAST
        );

        content.add(headerPanel);

        content.add(
                Box.createRigidArea(
                        new Dimension(0, 15)
                )
        );

        /*
         * Instructions
         */
        final JLabel instructions = createInstructions();

        content.add(instructions);

        content.add(
                Box.createRigidArea(
                        new Dimension(0, 15)
                )
        );

        /*
         * Sound selector
         */
        final JLabel soundLabel =
                new JLabel("Active sound:");

        soundLabel.setAlignmentX(LEFT_ALIGNMENT);

        content.add(soundLabel);

        content.add(
                Box.createRigidArea(
                        new Dimension(0, 5)
                )
        );

        soundComboBox.setAlignmentX(LEFT_ALIGNMENT);

        soundComboBox.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        30
                )
        );

        soundComboBox.addActionListener(event -> {
            if (reloading) {
                return;
            }

            final String selected =
                    (String) soundComboBox.getSelectedItem();

            if (selected == null
                    || NO_SOUNDS.equals(selected)) {
                return;
            }

            clearStatus();

            configManager.setConfiguration(
                    SleepwalkerStakeoutConfig.GROUP,
                    SleepwalkerStakeoutConfig.SELECTED_SOUND_KEY,
                    selected
            );
        });

        content.add(soundComboBox);

        content.add(
                Box.createRigidArea(
                        new Dimension(0, 10)
                )
        );

        /*
         * Preview
         */
        previewButton.setAlignmentX(LEFT_ALIGNMENT);

        previewButton.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        30
                )
        );

        previewButton.addActionListener(event -> {
            final String selected =
                    (String) soundComboBox.getSelectedItem();

            if (selected == null
                    || NO_SOUNDS.equals(selected)) {
                return;
            }

            clearStatus();

            soundPlayer.play(
                    selected,
                    config.soundVolume(),
                    message -> SwingUtilities.invokeLater(() ->
                            showError(message)
                    )
            );
        });

        content.add(previewButton);

        content.add(
                Box.createRigidArea(
                        new Dimension(0, 8)
                )
        );

        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        statusLabel.setForeground(
                new Color(255, 0, 0)
        );
        statusLabel.setVisible(false);

        content.add(statusLabel);

        add(
                content,
                BorderLayout.NORTH
        );

        reloadSounds();
    }

    @Nonnull
    private static JLabel createInstructions() {
        final JLabel instructions =
                new JLabel(
                        "<html>"
                                + "Add <b>.wav</b> files using the folder icon.<br>"
                                + "<br>"
                                + "Use reload to refresh the list after adding or removing files.<br>"
                                + "<br>"
                                + "Your selected sound is saved automatically."
                                + "</html>"
                );

        instructions.setAlignmentX(LEFT_ALIGNMENT);
        instructions.setForeground(
                ColorScheme.LIGHT_GRAY_COLOR
        );
        return instructions;
    }

    private JButton createIconButton(
            BufferedImage image,
            String tooltip
    ) {
        final JButton button =
                new JButton(
                        new ImageIcon(image)
                );

        button.setToolTipText(tooltip);

        button.setPreferredSize(
                new Dimension(28, 28)
        );

        button.setMinimumSize(
                new Dimension(28, 28)
        );

        button.setMaximumSize(
                new Dimension(28, 28)
        );

        button.setMargin(
                new Insets(2, 2, 2, 2)
        );

        button.setFocusable(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        return button;
    }

    private void showError(
            String message
    ) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);

        revalidate();
        repaint();
    }

    private void clearStatus() {
        statusLabel.setText("");
        statusLabel.setVisible(false);

        revalidate();
        repaint();
    }

    void reloadSounds() {
        reloading = true;

        try {
            final String selectedSound =
                    config.selectedSound();

            final List<String> sounds =
                    soundPlayer.getAvailableSounds();

            soundComboBox.removeAllItems();

            if (sounds.isEmpty()) {
                soundComboBox.addItem(NO_SOUNDS);

                soundComboBox.setEnabled(false);
                previewButton.setEnabled(false);

                return;
            }

            for (String sound : sounds) {
                soundComboBox.addItem(sound);
            }

            soundComboBox.setEnabled(true);
            previewButton.setEnabled(true);

            if (selectedSound != null
                    && sounds.contains(selectedSound)) {
                soundComboBox.setSelectedItem(
                        selectedSound
                );

                return;
            }

            final String firstSound =
                    sounds.get(0);

            soundComboBox.setSelectedItem(
                    firstSound
            );

            configManager.setConfiguration(
                    SleepwalkerStakeoutConfig.GROUP,
                    SleepwalkerStakeoutConfig.SELECTED_SOUND_KEY,
                    firstSound
            );
        } finally {
            reloading = false;
        }
    }

    private void openSoundDirectory() {
        soundPlayer.initialize();

        if (!Desktop.isDesktopSupported()) {
            log.warn(
                    "Desktop operations are not supported"
            );
            return;
        }

        final Desktop desktop =
                Desktop.getDesktop();

        if (!desktop.isSupported(
                Desktop.Action.OPEN
        )) {
            log.warn(
                    "Opening directories is not supported"
            );
            return;
        }

        try {
            desktop.open(
                    SleepwalkerStakeoutSoundPlayer.SOUND_DIR
            );
        } catch (IOException ex) {
            log.warn(
                    "Unable to open sound directory",
                    ex
            );
        }
    }
}