## partially implemented features to be released in 1.2
- added new region types: cuboid, cylinder, sphere
- added a GUI screen to set region parameters, accessible by pressing `B`
- changed the build mode keybind to `shift-B` (was: `B`)
- removed the usage keybind (was: `shift-B`); now accessible via the GUI

## 1.1
- updated for Minecraft 1.4
- added update check; can be disabled by creating a file named
  `(minecraft dir)/mods/BuildRegion/noupdatecheck.txt`
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
- initial release
