package reddragon.marvelousmachines.content;

import java.util.Locale;

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
import reddragon.marvelousmachines.MarvelousMachinesMod;
import reddragon.marvelousmachines.content.fluids.AbstractFluid;
import reddragon.marvelousmachines.content.fluids.VaporizingFluidBlock;
import reddragon.marvelousmachines.utils.FluidUtils;

public enum ModFluid implements BlockHolder {
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

		SEWAGE.addVaporizedResultChance(ModBlock.MUD_BLOCK, 1);
		SEWAGE.addVaporizedResultChance(SLUDGE, 1);

		SLUDGE.addVaporizedResultChance(ModBlock.MUD_BLOCK, 3);
		SLUDGE.addVaporizedResultChance(Blocks.BONE_BLOCK, 1);

		SLURRY.addVaporizedResultChance(Blocks.SLIME_BLOCK, 1);
		SLURRY.addVaporizedResultChance(Blocks.BONE_BLOCK, 10);
		SLURRY.addVaporizedResultChance(Blocks.WATER, 80);
	}

	private ModFluid(final int color, final boolean ticksRandomly, final int levelDecreasePerBlock, final int tickRate) {
		this.color = color;

		stillFluid = new AbstractFluid.Still(
				() -> stillFluid,
				() -> flowingFluid,
				() -> fluidBlock,
				() -> bucketItem,
				levelDecreasePerBlock,
				tickRate);

		flowingFluid = new AbstractFluid.Flowing(
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

		bucketItem = new BucketItem(stillFluid, new Item.Settings().group(MarvelousMachinesMod.ITEMGROUP).recipeRemainder(Items.BUCKET).maxCount(1));
	}

	public void register() {
		final Identifier identifier = new Identifier(MarvelousMachinesMod.NAMESPACE, name().toLowerCase(Locale.ROOT));

		Registry.register(Registry.FLUID, identifier, stillFluid);
		Registry.register(Registry.FLUID, new Identifier(MarvelousMachinesMod.NAMESPACE, identifier.getPath() + "_flowing"), flowingFluid);

		Registry.register(Registry.BLOCK, identifier, fluidBlock);
		Registry.register(Registry.ITEM,
				new Identifier(MarvelousMachinesMod.NAMESPACE, identifier.getPath() + "_bucket"), bucketItem);

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
