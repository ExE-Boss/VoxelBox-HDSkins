package com.minelittlepony.hdskins.gui;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;

public class DummyWorld extends World {

    public static final World INSTANCE = new DummyWorld();

    private final Chunk chunk = new Chunk(this, 0, 0);

    private DummyWorld() {
        super(null,
                new WorldInfo(new WorldSettings(0, GameType.NOT_SET, false, false, WorldType.DEFAULT), "MpServer"),
                new WorldProviderSurface(),
                null,
                true);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return null;
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return true;
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return chunk;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public float getLightBrightness(BlockPos pos) {
        return 1;
    }

    @Override
    public BlockPos getSpawnPoint() {
        return BlockPos.ORIGIN;
    }
}
