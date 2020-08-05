package reddragon.marvelousmachines.content;

import static reddragon.marvelousmachines.MarvelousMachinesMod.ITEMGROUP;

import net.minecraft.block.Blocks;
import reddragon.api.configs.ModFluidConfig;
import reddragon.api.content.BlockHolder;
import reddragon.api.content.fluids.VaporizingFluidBlock;

public enum MarvelousMachinesFluid implements BlockHolder {
	SLURRY(new ModFluidConfig(0x906D67, true, 1, 8, ITEMGROUP)),
	SLUDGE(new ModFluidConfig(0x271d12, true, 2, 16, ITEMGROUP)),
	SEWAGE(new ModFluidConfig(0x53410b, true, 3, 24, ITEMGROUP));

	static {
		// Initialize vaporizing results after enum instances have been created.
		// This is required because we may have to access fluid cases and we can't put
		// them in the constructor.

		SEWAGE.config.addVaporizedResultChance(MarvelousMachinesBlock.MUD_BLOCK, 1);
		SEWAGE.config.addVaporizedResultChance(SLUDGE, 1);

		SLUDGE.config.addVaporizedResultChance(MarvelousMachinesBlock.MUD_BLOCK, 3);
		SLUDGE.config.addVaporizedResultChance(Blocks.BONE_BLOCK, 1);

		SLURRY.config.addVaporizedResultChance(Blocks.SLIME_BLOCK, 1);
		SLURRY.config.addVaporizedResultChance(Blocks.BONE_BLOCK, 10);
		SLURRY.config.addVaporizedResultChance(Blocks.WATER, 80);
	}

	private final ModFluidConfig config;

	private MarvelousMachinesFluid(final ModFluidConfig config) {
		this.config = config;
	}

	@Override
	public VaporizingFluidBlock getBlock() {
		return config.getBlock();
	}

	public ModFluidConfig getConfig() {
		return config;
	}

}
