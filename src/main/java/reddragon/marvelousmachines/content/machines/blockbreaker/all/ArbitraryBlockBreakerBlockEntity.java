package reddragon.marvelousmachines.content.machines.blockbreaker.all;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import reddragon.marvelousmachines.content.MarvelousMachinesMachine;
import reddragon.marvelousmachines.content.machines.blockbreaker.AbstractBlockBreakerBlockEntity;

public class ArbitraryBlockBreakerBlockEntity extends AbstractBlockBreakerBlockEntity {
	private static final int ENERGY_PER_OPERATION_PER_HARDNESS = 100;
	private static final int TICKS_PER_HARDNESS = 20;

	public ArbitraryBlockBreakerBlockEntity(final MarvelousMachinesMachine machineType) {
		super(machineType);
	}

	@Override
	protected boolean isBlockAllowed(final Block block) {
		if (block == Blocks.AIR) {
			return false;
		}

		if (block instanceof FluidBlock) {
			return false;
		}

		return block.getDefaultState().getHardness(null, null) >= 0;
	}

	@Override
	protected int getTicksPerOperation() {
		return Math.max(10, Math.round(getBlockHardness() * TICKS_PER_HARDNESS));
	}

	@Override
	protected double getEnergyPerOperation() {
		return Math.max(10, Math.round(getBlockHardness() * ENERGY_PER_OPERATION_PER_HARDNESS));
	}

	private float getBlockHardness() {
		final BlockPos blockPos = pos.offset(getFacing());
		final BlockState state = world.getBlockState(blockPos);
		return state.getHardness(world, blockPos);
	}
}
