<div align="center">
  <img width="300" alt="icon" src="https://github.com/user-attachments/assets/246b65b2-2360-4ed0-a047-64d91ad7d81d" />
</div>

<br/>

# Sleepwalker Stakeout

[![Total installs](http://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/installs/plugin/sleepwalker-stakeout)](https://runelite.net/plugin-hub/show/sleepwalker-stakeout)
[![Plugin rank](http://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/rank/plugin/sleepwalker-stakeout)](https://runelite.net/plugin-hub/show/sleepwalker-stakeout)
[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/manc1n1/sleepwalker-stakeout/build.yml?branch=master)](https://github.com/manc1n1/sleepwalker-stakeout)
[![GitHub Tag](https://img.shields.io/github/v/tag/manc1n1/sleepwalker-stakeout?label=Latest%20release)](https://github.com/manc1n1/sleepwalker-stakeout/tags)
![Dynamic Regex Badge](https://img.shields.io/badge/dynamic/regex?url=https%3A%2F%2Fraw.githubusercontent.com%2Fmanc1n1%2Fsleepwalker-stakeout%2Frefs%2Fheads%2Fmaster%2Frunelite-plugin.properties&search=version%3D(.*)&replace=v%241&label=Git%20version)

A [RuneLite](https://runelite.net/) plugin that displays a movable (<kbd>Alt</kbd> +
Drag) [Blisterwood stake sprite](https://oldschool.runescape.wiki/w/Blisterwood_stake#/media/File:Blisterwood_stake.png)
as a fake XP drop whenever your character throws
a [Blisterwood stake](https://oldschool.runescape.wiki/w/Blisterwood_stake) in [Old School RuneScape](https://oldschool.runescape.com/).

## Features

- Displays a Blisterwood stake sprite when the stake-throw animation is detected
- Mimics the movement and fade behavior of a RuneLite XP drop
- Only activates when:
    - The animation belongs to your local player
    - The Blisterwood stake throw animation is detected
    - A Blisterwood stake is equipped
- Automatically positions the overlay near the standard XP drop area
- Overlay can be manually repositioned
- Renders above other overlays for improved visibility
- No configuration required

## Use Cases

### [Phosani's Nightmare](https://oldschool.runescape.wiki/w/Phosani%27s_Nightmare)

The primary use case is during Phosani's Nightmare, where Blisterwood stakes are used against Sleepwalkers.

This plugin was created because the XP drops displayed when attacking Sleepwalkers with a Blisterwood stake can be
unreliable. Sometimes, a successful attack may not produce a visible XP drop, even though the attack still registers.
The plugin provides an additional visual indicator to confirm that you attacked the Sleepwalker.

The additional visual indicator provides a more consistent way to track stake throws without relying exclusively on XP
drops, character animations, or game-world visuals.

## How It Works

The plugin listens for animation changes on the local player.

When the Blisterwood stake throw animation is detected, it verifies that a Blisterwood stake is currently equipped. If
both conditions are satisfied, an animated stake sprite is added to the overlay.

The sprite:
1. Begins near the bottom of the overlay
2. Travels upward similarly to an XP drop
3. Remains fully visible for most of the animation
4. Fades out near the end
5. Is automatically removed after the animation completes

The animation lasts approximately 1.5 seconds.

## Overlay Position

By default, the plugin automatically positions the Blisterwood stake indicator near RuneLite's standard XP drop area.

The overlay can be moved to any preferred location:

- Hold the <kbd>Alt</kbd> key
- Click and drag the overlay with your mouse
- Release it at the desired position

Once manually moved, the plugin stops automatically repositioning the overlay and continues using your custom location.

## Requirements

- RuneLite
- A Blisterwood stake equipped when performing the throw animation

The plugin does not modify game behavior, interact with NPCs, or automate any actions. It only provides a client-side
visual indicator based on information already available to RuneLite.

## Installation

- Download RuneLite from their website: https://runelite.net
- Launch RuneLite
- Click the Wrench icon on the top right of the RuneLite window
- Click the Plugin-Hub button on the right side near the top
- Search for "Sleepwalker Stakeout"
- Click Install
- Enable it if necessary

No additional configuration is required.

## Technical Details

The plugin detects the Blisterwood stake throw animation and validates the currently equipped weapon before displaying
the overlay.

The overlay uses RuneLite's `ItemManager` to retrieve the official Blisterwood stake item sprite and renders it using a
short upward movement and fade animation.

This prevents unrelated animations from triggering the indicator and ensures the visual is only displayed when the
player is actually using a Blisterwood stake.

## Development

The plugin is written in Java and built using the RuneLite plugin API.

Relevant RuneLite APIs include:

- `AnimationChanged`
- `ItemContainer`
- `EquipmentInventorySlot`
- `ItemManager`
- `Overlay`
- `OverlayManager`

## License

Copyright &copy; 2026 [manc1n1](https://github.com/manc1n1)

This project is distributed under the BSD 2-Clause License. See the source files or included [`LICENSE`][license-url] for the full
license text.

[license-url]: https://github.com/manc1n1/sleepwalker-stakeout/blob/master/LICENSE
