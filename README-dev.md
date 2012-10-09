## Build instructions

If you just want to install the mod, skip this section and refer to the
instructions in the main `README.md` document.

1.  Install and configure [MCP](http://mcp.ocean-labs.de/index.php/MCP_Releases).
    Your `minecraft.jar` should have ModLoader and LibShapeDraw patched in.
    Make sure you're able to recompile Minecraft before the next step.
2.  Copy everything from this repo's `src/` directory to
    `src/minecraft/`.
3.  Recompile, reobfuscate, and package the jar/zip.
    If you're on a Unix-based system, you can use the `build-buildregion.sh`
    script for this.

Instead of including LibShapeDraw to be decompiled in step 1, you can add it to
the build path later (edit `conf/mcp.cfg`). However you won't be able to run
Minecraft for testing without doing a full recompile/reobfuscate, as the
LibShapeDraw jar includes references the obfuscated Minecraft classes.

This is of course somewhat clunky... we're all waiting on the Minecraft API!

## Planned features

 *  More region types: cylinders, spheroids, cuboids, schematics.
 *  Pressing `shift-B` will open up a movable, semi-transparent, non-modal GUI
    window. This will be used for changing general settings and for adjusting
    region parameters (e.g., the sphere radius).
