<div align="center">
  <img width="300" alt="icon" src="https://github.com/user-attachments/assets/246b65b2-2360-4ed0-a047-64d91ad7d81d" />
</div>

<br>

# Sleepwalker Stakeout

[![Total installs](http://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/installs/plugin/sleepwalker-stakeout)](https://runelite.net/plugin-hub/show/sleepwalker-stakeout)
[![Plugin rank](http://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/rank/plugin/sleepwalker-stakeout)](https://runelite.net/plugin-hub/show/sleepwalker-stakeout)
[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/manc1n1/sleepwalker-stakeout/build.yml?branch=master)](https://github.com/manc1n1/sleepwalker-stakeout)
[![GitHub Tag](https://img.shields.io/github/v/tag/manc1n1/sleepwalker-stakeout?label=Latest%20release)](https://github.com/manc1n1/sleepwalker-stakeout/tags)
![Dynamic Regex Badge](https://img.shields.io/badge/dynamic/regex?url=https%3A%2F%2Fraw.githubusercontent.com%2Fmanc1n1%2Fsleepwalker-stakeout%2Frefs%2Fheads%2Fmaster%2Fgradle.properties&search=plugin_version%3D(.*)&replace=v%241&label=Git%20version)

A [RuneLite](https://runelite.net/) plugin that displays your equipped weapon's sprite as a moveable (<kbd>Alt</kbd> +
Drag) fake XP drop when using a supported weapon
against [Sleepwalkers](https://oldschool.runescape.wiki/w/Sleepwalker_(Phosani%27s_Nightmare))
during [Phosani's Nightmare](https://oldschool.runescape.wiki/w/Phosani%27s_Nightmare)
in [Old School RuneScape](https://oldschool.runescape.com/).

Sleepwalker Stakeout can also play a configurable sound alongside the fake XP drop. Sounds can be imported, selected,
previewed, and managed directly through the plugin's RuneLite plugin panel.

By default, fake XP drops are restricted to Sleepwalker targets. This restriction can optionally be disabled in the
plugin settings to display fake XP drops for supported attacks against any target.

## Demo

<div>
  <img width="518" height="524" alt="sleepwalker-stakeout-demo" src="https://github.com/user-attachments/assets/cbe29dae-c689-4b02-9c47-93e39aa6ee22" />
</div>

<br>

## Features

- Displays the equipped weapon's sprite as a fake XP drop when a supported attack animation is detected
- Mimics the movement and fade behavior of a RuneLite XP drop
- By default, only activates when:
    - The animation belongs to your local player
    - A supported attack animation is detected
    - The local player is attacking a Sleepwalker
    - A supported weapon is equipped
- Automatically displays the correct sprite for the currently equipped weapon
- Positions the overlay directly above the local player by default
- Automatically follows the player's position until manually moved
- Can be manually repositioned with <kbd>Alt</kbd> + Drag
- Supports RuneLite's built-in overlay anchor snapping
- Renders above other overlays for improved visibility
- Can play a sound whenever a fake XP drop appears
- Includes a default `sleepwalker.wav` sound
- Supports importing additional `.wav` sound files directly from the plugin panel
- Includes a RuneLite plugin panel for managing sounds
- Allows sounds to be selected and previewed directly from the plugin panel
- Allows user-added sounds to be deleted from the plugin panel
- Provides configurable sound volume from 0–100%
- Saves the selected sound between RuneLite sessions
- Allows the sound plugin panel button to be shown or hidden
- Displays optional one-time update messages when new plugin versions are released

## Supported Weapons

Sleepwalker Stakeout currently supports attack animations used by:

- [Blisterwood stake](https://oldschool.runescape.wiki/w/Blisterwood_stake)
- [Eye of Ayak](https://oldschool.runescape.wiki/w/Eye_of_Ayak#Charged)
- [Toxic blowpipe](https://oldschool.runescape.wiki/w/Toxic_blowpipe#Charged)
- [Blazing blowpipe](https://oldschool.runescape.wiki/w/Blazing_blowpipe#Charged)
- [Craw's bow](https://oldschool.runescape.wiki/w/Craw%27s_bow#Charged)
- [Webweaver bow](https://oldschool.runescape.wiki/w/Webweaver_bow#Charged)
- [Shortbows](https://oldschool.runescape.wiki/w/Shortbow_(weapon))
- [Longbows](https://oldschool.runescape.wiki/w/Longbow_(weapon))
- [Darts](https://oldschool.runescape.wiki/w/Dart)

The fake XP drop uses the sprite of the weapon currently equipped, so different weapon variants and dart types display
their corresponding item sprite automatically.

## Use Cases

### [Phosani's Nightmare](https://oldschool.runescape.wiki/w/Phosani%27s_Nightmare)

The primary use case is during Phosani's Nightmare, where players must quickly attack Sleepwalkers before they reach the
Nightmare.

This plugin was created because XP drops when attacking Sleepwalkers can be unreliable. Sometimes, a successful attack
may not produce a visible XP drop even though the attack still registers.

Sleepwalker Stakeout provides an additional visual indicator whenever a supported weapon attack is performed against a
Sleepwalker.

An optional sound can also be played alongside the fake XP drop, providing an additional audio cue when a supported
attack animation is detected.

This provides a more consistent way to track Sleepwalker attacks without relying exclusively on XP drops, character
animations, or other game-world visuals.

### Other Targets

The plugin can also be configured to display fake XP drops for supported attacks against targets other than
Sleepwalkers.

Enabling **Show for all targets** bypasses the default Sleepwalker-only target restriction while keeping all supported
weapon and animation checks in place.

## How It Works

The plugin listens for animation changes on the local player.

When a supported weapon attack animation is detected, the plugin checks the currently equipped weapon and, by default,
verifies that the local player is attacking a Sleepwalker.

If the required conditions are satisfied, the plugin retrieves the currently equipped weapon's item sprite and displays
it as an animated fake XP drop.

If sounds are enabled, the currently selected sound is played at the configured volume at the same time.

When **Show for all targets** is enabled, the Sleepwalker target check is skipped and supported attack animations can
trigger the overlay regardless of the current target.

For example:

- Attacking with a Blisterwood stake displays the Blisterwood stake sprite
- Attacking with an Eye of Ayak displays the Eye of Ayak sprite
- Attacking with a Toxic or Blazing blowpipe displays the corresponding blowpipe sprite
- Attacking with a bow displays the currently equipped bow sprite
- Attacking with a dart displays the currently equipped dart sprite

The fake XP drop:

1. Appears above the local player or at the user's selected overlay position
2. Travels upward similarly to an XP drop
3. Remains fully visible for most of the animation
4. Fades out near the end
5. Is automatically removed after the animation completes

The animation lasts approximately 1.5 seconds.

Supported weapon attack animations performed against other NPCs do not trigger the overlay.

## Sounds

Sleepwalker Stakeout includes an optional sound that can play alongside fake XP drops.

A default sound named `sleepwalker.wav` is included with the plugin and automatically placed in the plugin's sounds
directory if it does not already exist.

Additional `.wav` files can be imported directly from the plugin panel using the **+** button.

Imported sounds are stored in:

```text
.runelite/sleepwalker-stakeout/sounds
```

The plugin panel can be used to:

- Import additional `.wav` files
- View available sounds
- Select the active sound
- Preview the selected sound
- Delete user-added sounds
- Reload the sound list after files are added or removed

When a new sound is imported, it is added to the sound list and can be selected for playback.

The currently selected sound is saved automatically and restored the next time RuneLite is opened.

The bundled `sleepwalker.wav` sound cannot be deleted from the plugin panel. User-added sounds can be removed using the
**Delete** button.

> [!NOTE]
> WAV files must be compatible with RuneLite's audio player. Unsupported or unreadable files will display an error in
the plugin panel.

## Plugin Panel

When enabled, Sleepwalker Stakeout adds an icon to the RuneLite side nav.

The panel provides controls for managing available sounds without requiring filenames or directories to be entered
manually.

To add a sound:

1. Open the Sleepwalker Stakeout plugin panel
2. Click the **+** icon
3. Select a `.wav` file
4. Select the sound from the dropdown
5. Click **Preview** to test it

The reload icon can be used to refresh the sound list after files are added or removed outside the plugin.

To remove a user-added sound:

1. Select the sound from the dropdown
2. Click **Delete**
3. Confirm the deletion

The bundled `sleepwalker.wav` sound cannot be deleted.

The selected sound is saved automatically between RuneLite sessions.

The plugin panel button can be disabled with **Show plugin panel** without disabling sound playback itself.

## Configuration

Sleepwalker Stakeout includes a small set of optional settings.

### Show for all targets

Disabled by default.

When enabled, this setting bypasses the Sleepwalker-only target restriction and allows fake XP drops to appear for
supported attacks against any target.

Supported weapon and attack animation checks still apply.

### Play sound

Disabled by default.

When enabled, the selected sound is played whenever a fake XP drop is triggered.

The sound is played in addition to the visual fake XP drop and does not replace it.

### Sound volume

Defaults to 50%.

Controls the playback volume of the selected sound from 0–100%.

The same volume setting is used for both normal sound playback and the **Preview** button in the plugin panel.

### Show plugin panel

Enabled by default.

Controls whether the Sleepwalker Stakeout plugin panel appears in the RuneLite side nav.

Disabling this setting hides the side nav button but does not remove saved sounds or change the currently selected
sound.

### Show update messages

Enabled by default.

When enabled, Sleepwalker Stakeout may display a one-time message in the RuneLite chatbox when a new plugin version
introduces notable changes.

Disabling this setting prevents future update messages from being displayed.

## Overlay Position

By default, the fake XP drop indicator is positioned directly above the local player and follows the player's position
on the game canvas.

The overlay can be moved to any preferred location:

- Hold the <kbd>Alt</kbd> key
- Click and drag the overlay with your mouse
- Release it at the desired position

Once manually moved, the plugin stops following the local player and continues using the selected position.

The overlay also supports RuneLite's built-in anchor points. While moving the overlay, it can be snapped to supported
positions around the RuneLite client and will remain anchored there.

Resetting the overlay with <kbd>Alt</kbd> + Right Click restores the default behavior, causing the indicator to follow
the local player again.

## Update Messages

When an update introduces notable changes, Sleepwalker Stakeout may display a one-time message in the RuneLite chatbox.

Each update message is shown at most once per plugin version.

Update messages can be disabled at any time using the **Show update messages** setting.

## Requirements

- RuneLite
- A supported weapon equipped
- The local player must be attacking a Sleepwalker
- A Sleepwalker target when **Show for all targets** is disabled
- Sounds require **Play sound** to be enabled

The plugin does not modify game behavior, interact with NPCs, automate any actions, or determine whether an attack
successfully dealt damage.

It only provides client-side visual and optional audio indicators when a supported attack animation is detected and the
configured target requirements are satisfied.

## Installation

- Download RuneLite from their website: https://runelite.net
- Launch RuneLite
- Click the Wrench icon on the top right of the RuneLite window
- Click the Plugin Hub button on the right side near the top
- Search for "Sleepwalker Stakeout"
- Click Install
- Enable it if necessary

The default settings are designed for use during Phosani's Nightmare and can be adjusted from the RuneLite plugin
configuration panel.

Sound importing, selection, preview, and management controls are available from the Sleepwalker Stakeout plugin panel.

## License

Copyright &copy; 2026, [manc1n1](https://github.com/manc1n1)

This project is distributed under the BSD 2-Clause License. See the source files or included [`LICENSE`][license-url]
for the full license text.

[license-url]: https://github.com/manc1n1/sleepwalker-stakeout/blob/master/LICENSE
