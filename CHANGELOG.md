## partially implemented features to be released in 2.1
- added cylinder and sphere region types
- added options file and GUI screen

## partially implemented features to be released in 2.0
- updated for Minecraft 1.4.4
- added cuboid region type
- added a GUI screen to set region parameters, accessible by pressing `ctrl-B`,
  `shift-B`, or `ctrl-middle-click` (or `cmd-middle-click` on Mac)
- moved usage text to a GUI help screen
- added new keybinds to move and resize regions
- added localization support; translators needed!

## 1.1.1
- updated for Minecraft 1.4.2

## 1.1
- updated for Minecraft 1.4
- added update check; can be disabled by creating a file named
  `(minecraft dir)/mods/BuildRegion/noupdatecheck.txt`
- no longer auto-clear when the player gets too far from from the region
- ambiguous direction detection even less picky: only used when shifting regions

## 1.0.3
- adjusted the ambiguous direction detection method to make it less picky
- no longer directly modify any vanilla classes, increasing compatibility
- added mcmod.info files for improved ForgeModLoader integration (Forge is
  supported but *not* required)

## 1.0.2
- double slabs now work properly

## 1.0.1
- added "display" build region mode
- added keybinds making mouse use optional

## 1.0
- initial release for Minecraft 1.3.2
