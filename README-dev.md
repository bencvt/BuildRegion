## Build instructions

If you just want to install the mod, skip this section and refer to the
instructions in the main `README.md` document.

### Stuff you'll need:

 +  The Java Development Kit (JDK)
 +  A copy of `minecraft.jar` from your Minecraft installation
 +  [LibShapeDraw](http://www.minecraftforum.net/topic/1458931-libshapedraw/)
 +  [Minecraft Coder Pack (MCP)](http://mcp.ocean-labs.de/index.php/MCP_Releases)
 +  **Either** [ModLoader](http://www.minecraftforum.net/topic/75440-modloader/)
    or [Minecraft Forge](http://www.minecraftforge.net/forum/)

### Step-by-step

1.  **Option A:** Patch `minecraft.jar` to include ModLoader, then install and
    configure MCP using the modified `minecraft.jar`.  
    **Option B:** Install and configure MCP along with the Forge sources. For
    this option your `minecraft.jar` needs to be unmodified.
2.  Make sure you're able to recompile Minecraft before the next step.
3.  Add the LibShapeDraw jar to `jars/bin` and to the classpath, following the
    instructions from LibShapeDraw's main `README.md`.
4.  Copy everything from this repo's `src/` directory to `src/minecraft/`.
5.  Recompile, reobfuscate, and package the jar/zip. If you're on a Unix-based
    system, you can use the `build-buildregion.sh` script for this.

## Planned features

Planned for BuildRegion 1.2, partially implemented:

 +  More region types: cuboids, cylinders, spheres
 +  A GUI window for changing general settings and for adjusting complex region
    parameters (e.g., the origin and radii for spheres)

Tentative future plans:

 +  Allow half-height horizontal regions for slab and step placement
 +  A [schematic](http://www.minecraftwiki.net/wiki/Schematic) region type
 