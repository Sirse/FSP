package flaxbeard.steamcraft.world;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.IWorldGenerator;
import flaxbeard.steamcraft.Config;
import flaxbeard.steamcraft.SteamcraftBlocks;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;

import java.util.Random;

public class SteamcraftOreGen implements IWorldGenerator {

//Support for World Generation Manager
    public String getName() {
       return "FlaxbeardsSteamPower";
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (Config.copperDims != "")
            try {
                String copperDims = Config.copperDims.replaceAll("[a-zA-Z\\s]", "");
                String[] copperArray = copperDims.split(";");
                final int[] copperIDs = new int[copperArray.length];
                for (int i = 0; i < copperArray.length; i++) {
                    copperIDs[i] = Integer.parseInt(copperArray[i]);
                }
                for (int id : copperIDs) {
                    if (id == world.provider.dimensionId) {
                        generateExtra(world, random, chunkX * 16, chunkZ * 16, id);
                    }
                }
                //This exception needs to be ignored for when the value is empty/null/nada.
            } catch (NumberFormatException exception) {}

        if (Config.zincDims != "") {
            try {
                String zincDims = Config.zincDims.replaceAll("[a-zA-Z\\s]", "");
                String[] zincArray = zincDims.split(";");
                final int[] zincIDs = new int[zincArray.length];
                for (int i = 0; i < zincArray.length; i++) {
                    zincIDs[i] = Integer.parseInt(zincArray[i]);
                }
                for (int id : zincIDs) {
                    if (id == world.provider.dimensionId) {
                        generateExtra(world, random, chunkX * 16, chunkZ * 16, id);
                    }
                }
                //This exception needs to be ignored for when the value is empty/null/nada.
            } catch (NumberFormatException exception) {
            }
        }

        switch (world.provider.dimensionId) {
            case -1: {
                generateNether(world, random, chunkX * 16, chunkZ * 16);
                break;
            }
            case 0: {
                generateSurface(world, random, chunkX * 16, chunkZ * 16);
                break;
            }
            case 1: {
                generateEnd(world, random, chunkX * 16, chunkZ * 16);
                break;
            }
        }
    }

    private void generateEnd(World world, Random random, int i, int j) {
        if (Config.genCopperEnd) {
            WorldGenMinable copper = new WorldGenMinable(SteamcraftBlocks.steamcraftOre, 0, 10, Blocks.end_stone);
            for (int k = 0; k < 10; k++) {
                int x = i + random.nextInt(16);
                int y = random.nextInt(128);
                int z = j + random.nextInt(16);
                copper.generate(world, random, x, y, z);
            }
        }

        if (Config.genZincEnd) {
            WorldGenMinable zinc = new WorldGenMinable(SteamcraftBlocks.steamcraftOre, 1, 7, Blocks.end_stone);
            for (int k = 0; k < 10; k++) {
                int x = i + random.nextInt(16);
                int y = random.nextInt(128);
                int z = j + random.nextInt(16);
                zinc.generate(world, random, x, y, z);
            }
        }
    }

    private void generateSurface(World world, Random random, int i, int j) {
        if (Config.genCopperOverworld) {
            WorldGenMinable copper = new WorldGenMinable(SteamcraftBlocks.steamcraftOre, 0, 10, Blocks.stone);
            for (int k = 0; k < 10; k++) {
                int x = i + random.nextInt(16);
                int y = random.nextInt(80);
                int z = j + random.nextInt(16);
                copper.generate(world, random, x, y, z);
            }
        }

        if (Config.genZincOverworld) {
            WorldGenMinable zinc = new WorldGenMinable(SteamcraftBlocks.steamcraftOre, 1, 7, Blocks.stone);
            for (int k = 0; k < 10; k++) {
                int x = i + random.nextInt(16);
                int y = random.nextInt(75);
                int z = j + random.nextInt(16);
                zinc.generate(world, random, x, y, z);
            }
        }
    }

    private void generateNether(World world, Random random, int i, int j) {
        if (Config.genCopperNether) {
            WorldGenMinable copper = new WorldGenMinable(SteamcraftBlocks.steamcraftOre, 0, 10, Blocks.netherrack);
            for (int k = 0; k < 10; k++) {
                int x = i + random.nextInt(16);
                int y = random.nextInt(128);
                int z = j + random.nextInt(16);
                copper.generate(world, random, x, y, z);
            }
        }

        if (Config.genZincNether) {
            WorldGenMinable zinc = new WorldGenMinable(SteamcraftBlocks.steamcraftOre, 1, 7, Blocks.netherrack);
            for (int k = 0; k < 10; k++) {
                int x = i + random.nextInt(16);
                int y = random.nextInt(128);
                int z = j + random.nextInt(16);
                zinc.generate(world, random, x, y, z);
            }
        }
    }

    private void generateExtra(World world, Random random, int i, int j, int id) {
        if (checkConfigForInvalidIntegers(id)) {
            if (Config.genCopperExtras) {
                WorldGenMinable copper = new WorldGenMinable(SteamcraftBlocks.steamcraftOre, 0, 7, Blocks.stone);
                for (int k = 0; k < 10; k++) {
                    int x = i + random.nextInt(16);
                    int y = random.nextInt(128);
                    int z = j + random.nextInt(16);
                    copper.generate(world, random, x, y, z);
                }
            }

            if (Config.genZincExtras) {
                WorldGenMinable zinc = new WorldGenMinable(SteamcraftBlocks.steamcraftOre, 1, 7, Blocks.stone);
                for (int k = 0; k < 10; k++) {
                    int x = i + random.nextInt(16);
                    int y = random.nextInt(128);
                    int z = j + random.nextInt(16);
                    zinc.generate(world, random, x, y, z);
                }
            }
        /*
        } else {
            String error = "THE FLAXBEARD'S STEAM POWER CONFIG OPTION FOR ADDITIONAL DIMENSIONS TO GENERATE ZINC AND COPPER CANNOT HAVE 0, -1, OR 1. PLEASE FIX THIS, THEN CONTINUE. THANK YOU.";
            FMLLog.bigWarning(error);
        */
        }

    }

    public boolean checkConfigForInvalidIntegers(int id) {
        return id != 0 && id != -1 && id != 1;
    }
}
