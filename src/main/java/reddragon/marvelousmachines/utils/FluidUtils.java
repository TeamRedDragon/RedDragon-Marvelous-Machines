package reddragon.marvelousmachines.utils;

import java.util.function.Function;

import org.apache.logging.log4j.Logger;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;
import reddragon.marvelousmachines.MarvelousMachinesMod;

public class FluidUtils {

	private static final Logger LOG = MarvelousMachinesMod.LOG;

	public static void setupFluidRendering(final Fluid stillFluid, final Fluid flowingFluid, final Identifier fluidIdentifier,
			final int color) {

		final Identifier stillSpriteId = new Identifier(fluidIdentifier.getNamespace(),
				"block/fluids/" + fluidIdentifier.getPath());

		final Identifier flowingSpriteId = new Identifier(fluidIdentifier.getNamespace(),
				"block/fluids/" + fluidIdentifier.getPath() + "_flowing");

		// If they're not already present, add the sprites to the block atlas.

		ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEX).register((atlasTexture, registry) -> {
			registry.register(stillSpriteId);
			registry.register(flowingSpriteId);
		});

		final Identifier fluidId = Registry.FLUID.getId(stillFluid);
		final Identifier listenerId = new Identifier(fluidId.getNamespace(), fluidId.getPath() + "_reload_listener");

		final Sprite[] fluidSprites = { null, null };

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
				.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
					@Override
					public Identifier getFabricId() {
						return listenerId;
					}

					/**
					 * Get the sprites from the block atlas when resources are reloaded
					 */
					@Override
					public void apply(final ResourceManager resourceManager) {
						final Function<Identifier, Sprite> atlas = MinecraftClient.getInstance()
								.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX);

						fluidSprites[0] = atlas.apply(stillSpriteId);
						fluidSprites[1] = atlas.apply(flowingSpriteId);
					}
				});

		// The FluidRenderer gets the sprites and color from a FluidRenderHandler during
		// rendering
		final FluidRenderHandler renderHandler = new FluidRenderHandler() {
			@Override
			public Sprite[] getFluidSprites(final BlockRenderView view, final BlockPos pos, final FluidState state) {
				return fluidSprites;
			}

			@Override
			public int getFluidColor(final BlockRenderView view, final BlockPos pos, final FluidState state) {
				return color;
			}
		};

		FluidRenderHandlerRegistry.INSTANCE.register(stillFluid, renderHandler);
		FluidRenderHandlerRegistry.INSTANCE.register(flowingFluid, renderHandler);

		BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), stillFluid, flowingFluid);
	}
}
