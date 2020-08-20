package reddragon.marvelousmachines.content.machines.blockbreaker.stone;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import reddragon.marvelousmachines.content.MarvelousMachinesMachine;
import reddragon.marvelousmachines.content.machines.blockbreaker.AbstractBlockBreakerBlockEntity;

public class StoneBreakerBlockEntity extends AbstractBlockBreakerBlockEntity {
	private static final double ENERGY_PER_OPERATION = 100;
	private static final int TICKS_PER_OPERATION = 100;

	private static final List<Block> ALLOWED_BLOCKS = Arrays.asList(
			Blocks.COBBLESTONE,
			Blocks.STONE);

	public StoneBreakerBlockEntity(final MarvelousMachinesMachine machineType) {
		super(machineType);
	}

	@Override
	protected boolean isBlockAllowed(final Block block) {
		return ALLOWED_BLOCKS.contains(block);
	}

	@Override
	protected int getTicksPerOperation() {
		return TICKS_PER_OPERATION;
	}

	@Override
	protected double getEnergyPerOperation() {
		return ENERGY_PER_OPERATION;
	}
}
