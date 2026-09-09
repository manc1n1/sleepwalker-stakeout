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
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;

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

    private final JButton deleteButton =
            new JButton("Delete");

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

        final BufferedImage addImage =
                ImageUtil.loadImageResource(
                        getClass(),
                        "/com/sleepwalkerstakeout/icons/add_icon.png"
                );

        final JButton reloadButton =
                createIconButton(
                        refreshImage,
                        "Reload sounds from disk"
                );

        reloadButton.addActionListener(
                event -> reloadSounds()
        );

        final JButton addButton =
                createIconButton(
                        addImage,
                        "Add sound"
                );

        addButton.addActionListener(
                event -> addSound()
        );

        headerButtons.add(reloadButton);
        headerButtons.add(addButton);

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
        final JTextArea instructions = createInstructions();

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
            updateDeleteButton();

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

        deleteButton.setAlignmentX(LEFT_ALIGNMENT);

        deleteButton.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        30
                )
        );

        deleteButton.addActionListener(
                event -> deleteSelectedSound()
        );

        content.add(deleteButton);

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
    private static JTextArea createInstructions() {
        final JTextArea instructions =
                new JTextArea(
                        "Add .wav files to directory:\n"
                                + ".runelite/\n"
                                + "sleepwalker-stakeout/\n"
                                + "sounds\n\n"
                                + "Use reload after adding or removing files.\n\n"
                                + "Your selected sound is saved automatically."
                );

        instructions.setEditable(false);
        instructions.setFocusable(false);
        instructions.setOpaque(false);
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);

        instructions.setForeground(
                ColorScheme.LIGHT_GRAY_COLOR
        );

        instructions.setAlignmentX(LEFT_ALIGNMENT);

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
                deleteButton.setEnabled(false);

                return;
            }

            for (String sound : sounds) {
                soundComboBox.addItem(sound);
            }

            soundComboBox.setEnabled(true);
            previewButton.setEnabled(true);
            deleteButton.setEnabled(true);

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

            updateDeleteButton();
        } finally {
            reloading = false;
        }
    }

    private void addSound() {
        final JFileChooser fileChooser =
                new JFileChooser();

        fileChooser.setDialogTitle("Add Sound");

        fileChooser.setFileSelectionMode(
                JFileChooser.FILES_ONLY
        );

        fileChooser.setAcceptAllFileFilterUsed(false);

        fileChooser.setFileFilter(
                new FileNameExtensionFilter(
                        "WAV files (*.wav)",
                        "wav"
                )
        );

        final int result =
                fileChooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        final Path source =
                fileChooser
                        .getSelectedFile()
                        .toPath();

        final String fileName =
                source
                        .getFileName()
                        .toString();

        if (!fileName
                .toLowerCase(Locale.ROOT)
                .endsWith(".wav")) {
            showError(
                    "Selected file must be a .wav file"
            );
            return;
        }

        final Path destination =
                SleepwalkerStakeoutSoundPlayer
                        .SOUND_DIR
                        .resolve(fileName)
                        .normalize();

        try {
            Files.copy(
                    source,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

            clearStatus();
            reloadSounds();

            soundComboBox.setSelectedItem(
                    fileName
            );

            configManager.setConfiguration(
                    SleepwalkerStakeoutConfig.GROUP,
                    SleepwalkerStakeoutConfig.SELECTED_SOUND_KEY,
                    fileName
            );
        } catch (IOException ex) {
            log.warn(
                    "Unable to add sound: {}",
                    source,
                    ex
            );

            showError(
                    "Unable to add sound file"
            );
        }
    }

    private void deleteSelectedSound() {
        final String selected =
                (String) soundComboBox.getSelectedItem();

        if (selected == null
                || NO_SOUNDS.equals(selected)) {
            return;
        }

        if ("sleepwalker.wav".equals(selected)) {
            showError(
                    "The default sound cannot be deleted"
            );
            return;
        }

        final int result =
                JOptionPane.showConfirmDialog(
                        this,
                        "Delete " + selected + "?",
                        "Delete Sound",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        final Path soundPath =
                SleepwalkerStakeoutSoundPlayer
                        .SOUND_DIR
                        .resolve(selected)
                        .normalize();

        if (!soundPath.getParent().equals(
                SleepwalkerStakeoutSoundPlayer.SOUND_DIR
        )) {
            showError(
                    "Unable to delete sound file"
            );
            return;
        }

        try {
            if (!Files.deleteIfExists(soundPath)) {
                showError(
                        "Sound file does not exist"
                );
                return;
            }

            clearStatus();
            reloadSounds();
        } catch (IOException ex) {
            log.warn(
                    "Unable to delete sound: {}",
                    soundPath,
                    ex
            );

            showError(
                    "Unable to delete sound file"
            );
        }
    }

    private void updateDeleteButton() {
        final String selected =
                (String) soundComboBox.getSelectedItem();

        deleteButton.setEnabled(
                selected != null
                        && !NO_SOUNDS.equals(selected)
                        && !"sleepwalker.wav".equals(selected)
        );
    }
}