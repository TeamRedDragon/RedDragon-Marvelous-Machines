package reddragon.marvelousmachines.content;

import static reddragon.marvelousmachines.MarvelousMachinesMod.ITEMGROUP;
import static reddragon.marvelousmachines.MarvelousMachinesMod.NAMESPACE;

import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemGroup;
import reddragon.api.configs.ModFluidConfig;
import reddragon.api.configs.RegisterableFluid;
import reddragon.api.content.BlockHolder;
import reddragon.api.content.fluids.DryingFluidBlock;

public enum MarvelousMachinesFluid implements BlockHolder, RegisterableFluid {
	SLURRY(new ModFluidConfig().color(0x906D67)
			.ticksRandomly()
			.levelDecreasePerBlock(1)
			.flowSpeed(8)
			.driesTo(Blocks.SLIME_BLOCK, 1)
			.driesTo(Blocks.BONE_BLOCK, 10)
			.driesTo(Blocks.WATER, 80)),

	SLUDGE(new ModFluidConfig().color(0x271d12)
			.ticksRandomly()
			.levelDecreasePerBlock(2)
			.flowSpeed(16)
			.driesTo(MarvelousMachinesBlock.MUD_BLOCK, 3)
			.driesTo(Blocks.BONE_BLOCK, 1)),

	SEWAGE(new ModFluidConfig().color(0x53410b)
			.ticksRandomly()
			.levelDecreasePerBlock(3)
			.flowSpeed(24)
			.driesTo(MarvelousMachinesBlock.MUD_BLOCK, 1)
			.driesTo(MarvelousMachinesFluid.SLUDGE, 1));

	private final ModFluidConfig config;

	private MarvelousMachinesFluid(final ModFluidConfig config) {
		this.config = config;
	}

	@Override
	public DryingFluidBlock getBlock() {
		return config.getBlock();
	}

	@Override
	public ModFluidConfig getConfig() {
		return config;
	}

	public Fluid getFluid() {
		return config.getStillFluid();
	}

	@Override
	public ItemGroup getItemGroup() {
		return ITEMGROUP;
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

}
