package de.jilocasin.dragoncraftmachines.content;

import java.util.Locale;

import de.jilocasin.dragoncraftmachines.DragoncraftMachinesMod;
import de.jilocasin.dragoncraftmachines.content.fluids.DragoncraftAbstractFluid;
import de.jilocasin.dragoncraftmachines.content.fluids.VaporizingFluidBlock;
import de.jilocasin.dragoncraftmachines.utils.FluidUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public enum DragoncraftFluid implements BlockHolder {
	SLURRY(0x906D67, true, 1, 8),
	SLUDGE(0x271d12, true, 2, 16),
	SEWAGE(0x53410b, true, 3, 24);

	private FlowableFluid stillFluid;
	private FlowableFluid flowingFluid;
	private VaporizingFluidBlock fluidBlock;
	private BucketItem bucketItem;

	private final int color;

	static {
		// Initialize vaporizing results after enum instances have been created.
		// This is required because we may have to access fluid cases and we can't put
		// them in the constructor.

		SEWAGE.addVaporizedResultChance(DragoncraftBlock.MUD_BLOCK, 1);
		SEWAGE.addVaporizedResultChance(SLUDGE, 1);

		SLUDGE.addVaporizedResultChance(DragoncraftBlock.MUD_BLOCK, 3);
		SLUDGE.addVaporizedResultChance(Blocks.BONE_BLOCK, 1);

		SLURRY.addVaporizedResultChance(Blocks.SLIME_BLOCK, 1);
		SLURRY.addVaporizedResultChance(Blocks.BONE_BLOCK, 10);
		SLURRY.addVaporizedResultChance(Blocks.WATER, 80);
	}

	private DragoncraftFluid(final int color, final boolean ticksRandomly, final int levelDecreasePerBlock, final int tickRate) {
		this.color = color;

		stillFluid = new DragoncraftAbstractFluid.Still(
				() -> stillFluid,
				() -> flowingFluid,
				() -> fluidBlock,
				() -> bucketItem,
				levelDecreasePerBlock,
				tickRate);

		flowingFluid = new DragoncraftAbstractFluid.Flowing(
				() -> stillFluid,
				() -> flowingFluid,
				() -> fluidBlock,
				() -> bucketItem,
				levelDecreasePerBlock,
				tickRate);

		final Settings blockSettings = FabricBlockSettings.copy(Blocks.WATER);
		if (ticksRandomly) {
			blockSettings.ticksRandomly();
		}
		fluidBlock = new VaporizingFluidBlock(stillFluid, blockSettings);

		bucketItem = new BucketItem(stillFluid, new Item.Settings().group(DragoncraftMachinesMod.ITEMGROUP).recipeRemainder(Items.BUCKET).maxCount(1));
	}

	public void register() {
		final Identifier identifier = new Identifier(DragoncraftMachinesMod.NAMESPACE, name().toLowerCase(Locale.ROOT));

		Registry.register(Registry.FLUID, identifier, stillFluid);
		Registry.register(Registry.FLUID, new Identifier(DragoncraftMachinesMod.NAMESPACE, identifier.getPath() + "_flowing"), flowingFluid);

		Registry.register(Registry.BLOCK, identifier, fluidBlock);
		Registry.register(Registry.ITEM,
				new Identifier(DragoncraftMachinesMod.NAMESPACE, identifier.getPath() + "_bucket"), bucketItem);

		FluidUtils.setupFluidRendering(stillFluid, flowingFluid, identifier, color);
	}

	public void addVaporizedResultChance(final Block block, final float weight) {
		fluidBlock.addVaporizedResultChance(block, weight);
	}

	public void addVaporizedResultChance(final BlockHolder blockHolder, final float weight) {
		fluidBlock.addVaporizedResultChance(blockHolder.getBlock(), weight);
	}

	@Override
	public VaporizingFluidBlock getBlock() {
		return fluidBlock;
	}
}
