package reddragon.marvelousmachines.content;

import static reddragon.marvelousmachines.MarvelousMachinesMod.ITEMGROUP;
import static reddragon.marvelousmachines.MarvelousMachinesMod.NAMESPACE;

import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemGroup;
import reddragon.api.configs.ModFluidConfig;
import reddragon.api.configs.RegisterableFluid;
import reddragon.api.content.BlockHolder;
import reddragon.api.content.fluids.VaporizingFluidBlock;

public enum MarvelousMachinesFluid implements BlockHolder, RegisterableFluid {
	SLURRY(new ModFluidConfig().color(0x906D67)
			.ticksRandomly()
			.levelDecreasePerBlock(1)
			.flowSpeed(8)
			.vaporizesTo(Blocks.SLIME_BLOCK, 1)
			.vaporizesTo(Blocks.BONE_BLOCK, 10)
			.vaporizesTo(Blocks.WATER, 80)),

	SLUDGE(new ModFluidConfig().color(0x271d12)
			.ticksRandomly()
			.levelDecreasePerBlock(2)
			.flowSpeed(16)
			.vaporizesTo(MarvelousMachinesBlock.MUD_BLOCK, 3)
			.vaporizesTo(Blocks.BONE_BLOCK, 1)),

	SEWAGE(new ModFluidConfig().color(0x53410b)
			.ticksRandomly()
			.levelDecreasePerBlock(3)
			.flowSpeed(24)
			.vaporizesTo(MarvelousMachinesBlock.MUD_BLOCK, 1)
			.vaporizesTo(MarvelousMachinesFluid.SLUDGE, 1));

	private final ModFluidConfig config;

	private MarvelousMachinesFluid(final ModFluidConfig config) {
		this.config = config;
	}

	@Override
	public VaporizingFluidBlock getBlock() {
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
