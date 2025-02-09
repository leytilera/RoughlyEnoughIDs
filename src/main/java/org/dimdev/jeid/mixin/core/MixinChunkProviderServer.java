package org.dimdev.jeid.mixin.core;

import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.Loader;

import org.dimdev.jeid.INewChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import rtg.world.biome.BiomeProviderRTG;

@Mixin(ChunkProviderServer.class)
public class MixinChunkProviderServer {

    @Shadow @Final public WorldServer world;
    @Shadow @Final @Mutable public final IChunkGenerator chunkGenerator;
    private final Biome[] reusableBiomeList = new Biome[256];

    protected MixinChunkProviderServer(IChunkGenerator chunkGenerator) {
        this.chunkGenerator = chunkGenerator;
    }

    /**
     * @reason Return an empty biome byte array if the chunk is using an int biome array.
     */
    @Inject(method = "provideChunk", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/gen/IChunkGenerator;generateChunk(II)Lnet/minecraft/world/chunk/Chunk;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void initializeBiomeArray(int x, int z, CallbackInfoReturnable<Chunk> cir, Chunk chunk, long chunkPos) {
        if (!Loader.isModLoaded("rtg") || !(world.getBiomeProvider() instanceof BiomeProviderRTG)) {
            Biome[] biomes = world.getBiomeProvider().getBiomes(reusableBiomeList, x * 16, z * 16, 16, 16);

            INewChunk newChunk = (INewChunk) chunk;
            int[] intBiomeArray = newChunk.getIntBiomeArray();
            for (int i = 0; i < intBiomeArray.length; ++i) {
                intBiomeArray[i] = Biome.getIdForBiome(biomes[i]);
            }
        }
    }
}
