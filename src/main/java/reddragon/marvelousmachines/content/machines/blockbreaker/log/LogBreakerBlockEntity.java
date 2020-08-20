package reddragon.marvelousmachines.content.machines.blockbreaker.log;

import net.minecraft.block.Block;
import net.minecraft.tag.BlockTags;
import reddragon.marvelousmachines.content.MarvelousMachinesMachine;
import reddragon.marvelousmachines.content.machines.blockbreaker.AbstractBlockBreakerBlockEntity;

public class LogBreakerBlockEntity extends AbstractBlockBreakerBlockEntity {
	private static final double ENERGY_PER_OPERATION = 50;
	private static final int TICKS_PER_OPERATION = 50;

	public LogBreakerBlockEntity(final MarvelousMachinesMachine machineType) {
		super(machineType);
	}

	@Override
	protected boolean isBlockAllowed(final Block block) {
		return block.isIn(BlockTags.LOGS);
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
