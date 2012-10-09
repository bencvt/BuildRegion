## Basic usage

In Minecraft, having to go back and fix blocks that you accidentally placed or
destroyed is time-consuming and annoying. BuildRegion is a user interface
enhancement client mod, designed to help you avoid these little accidents.

BuildRegion is *not* an auto-build tool. It will only prevent you from
misclicking; it will not click for you.

BuildRegion is [open source](https://github.com/bencvt/BuildRegion)!

## Full description

To get started, use your mouse to `control-right-click`. This will make a grid
appear in front of you. As long as the grid is active, you can only build inside
that region. Use `control-left-click` to remove the build region.

Note that if you're using a Mac, use `command (⌘)` instead of `control`.

You can easily redefine the region: just move around or face a different
direction, then press `control-right-click` again. You can also move the region
around using `[` and `]`.

Additionally, pressing `B` will change the build region mode: the grid will
change colors, and you can only build *outside* of the region.

Finally, press `shift-B` to get a list of all commands (i.e., a short version
of everything you just read.)

## Compatibility

BuildRegion should be compatible with every mod that does not modify the
PlayerControllerMP class (`atc.class`, as of Minecraft 1.3.2).

## Installation

Installing this mod works exactly the same as any other Minecraft client mod.

1.  Make sure the following two required mods are installed:  
    [ModLoader](http://www.minecraftforum.net/topic/75440-modloader/)
    [LibShapeDraw](http://www.minecraftforum.net/topic/1458931-libshapedraw/)
2.  Download and extract the zip for the latest release.
3.  Patch the contents of the zip file into your `minecraft.jar`, being sure to
    remove the `META-INF` folder.

Utilities like [Magic Launcher](http://www.minecraftforum.net/topic/939149-/)
can automate this process. Highly recommended! Manually copying `.class` files
is for the birds.
