In Minecraft, having to go back and fix blocks that you accidentally placed or
destroyed is time-consuming and annoying. BuildRegion is a user interface
enhancement mod, designed to help you avoid these little accidents.

BuildRegion can also help you plan structures, giving you a dynamic blueprint
right there in the world.

BuildRegion is *not* an auto-build tool. It will only prevent you from
misclicking; it will not click for you. It's all client-side, meaning you can
use this on multi-player without the server having to run a special plugin.

See the [official minecraftforums.net thread](http://www.minecraftforum.net/topic/1514724-buildregion/)
for screenshots and a demo video.

## Installation

First of all, make sure that
[LibShapeDraw](http://www.minecraftforum.net/topic/1458931-libshapedraw/) is
installed as it is a base requirement. You'll also need
[ModLoader](http://www.minecraftforum.net/topic/75440-modloader/) or
[Forge](http://www.minecraftforge.net/forum/); BuildRegion is compatible with
either.

Next, download the zip and do one of the following, whichever you prefer:

 +  **Use a launcher utility to add the zip (recommended).** There are many
    launchers out there, e.g.
    [Magic Launcher](http://www.minecraftforum.net/topic/939149-magiclauncher/).

OR

 +  **Move the zip file to the `mods` subdirectory.** This subdirectory can
    be found in your
    [Minecraft directory](http://www.minecraftwiki.net/wiki/.minecraft).

OR

 +  **Manually patch the contents of the zip file into your `minecraft.jar`.**
    Old-school modding still works. Be sure to remove the `META-INF` folder.

## Compatibility

BuildRegion does not modify *any* vanilla classes directly and therefore should
be compatible with virtually every mod that works with ModLoader or Forge. If
you find an incompatible mod please post to
[the minecraftforums thread](http://www.minecraftforum.net/topic/1514724-buildregion/)
or [open an issue on github](https://github.com/bencvt/BuildRegion/issues)
and we'll try to sort it out.

## Usage

To get started, hold `control` on your keyboard and `right-click` your mouse.
This will make a grid appear in front of you. As long as the grid is active,
you can only build inside that region. Use `control-left-click` to remove the
build region.

You can easily redefine the region: just move around or face a different
direction, then `control-right-click` again. You can also move the region
around using `[`, `]`, and the arrow keys.

Press `B` to change how the build region works:

 *   *Blue (default)*: you can only place or destroy blocks *inside* the grid.
 *   *Red*: you can only place or destroy blocks *outside* the grid.
 *   *Green and white*: the grid is for *display* only; it won't affect block
     placement.

Finally, press `shift-B` or `control-middle-click` to open up a GUI window to
help you define the region. Most of the things that you can do in the GUI are
already covered by a keyboard or mouse shortcut, listed above. The GUI is
intended as an alternate way to set up your region -- use whichever method you
prefer! Even if the GUI is open, you can still use keyboard/mouse shortcuts, and
you can adjust the camera by moving the mouse while holding `right-click`.

Note: if you're using a Mac, use `command (⌘)` instead of `control`.

## More info

BuildRegion is open source! Visit the official project page at
[github.com/bencvt/BuildRegion](https://github.com/bencvt/BuildRegion).
In addition to the source code, the project documentation also includes the
change log and a list of planned features.
