package reddragon.marvelousmachines.content.machines;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;
import reddragon.marvelousmachines.MarvelousMachinesMod;
import reddragon.marvelousmachines.content.MarvelousMachinesMachine;

public abstract class TickingOperationMachineBlockEntity extends AbstractMachineBlockEntity {

	public static final Logger LOG = LogManager.getLogger(MarvelousMachinesMod.NAMESPACE);

	public TickingOperationMachineBlockEntity(final MarvelousMachinesMachine machineType) {
		super(machineType);
	}

	private int completedTicks = 0;

	protected abstract double getEnergyPerOperation();

	protected abstract int getTicksPerOperation();

	protected abstract boolean couldPerformOperation();

	protected abstract void performOperation();

	private int getUpgradedTicksPerOperation() {
		return (int) Math.max(Math.round((1.0 - getSpeedMultiplier()) * getTicksPerOperation()), 1);
	}

	private double getUpgradedEnergyPerOperation() {
		return getEnergyPerOperation() * getPowerMultiplier();
	}

	public void resetOperationProgress() {
		completedTicks = 0;
	}

	public int getProgressScaled(final int scale) {
		return ((completedTicks) * scale) / getUpgradedTicksPerOperation();
	}

	@Override
	public CompoundTag toTag(final CompoundTag tag) {
		super.toTag(tag);
		tag.putInt("completedTicks", completedTicks);
		return tag;
	}

	@Override
	public void fromTag(final BlockState state, final CompoundTag tag) {
		super.fromTag(state, tag);
		completedTicks = tag.getInt("completedTicks");
	}

	@Override
	public void tick() {
		super.tick();

		final double energyPerTick = getUpgradedEnergyPerOperation() / getUpgradedTicksPerOperation();

		if (canUseEnergy(energyPerTick)) {
			if (couldPerformOperation()) {
				setIsActive(true);

				useEnergy(energyPerTick);
				completedTicks = (completedTicks + 1) % getUpgradedTicksPerOperation();

				if (completedTicks == 0 && !world.isClient) {
					performOperation();
				}
			} else {
				setIsActive(false);

				resetOperationProgress();
			}
		} else {
			setIsActive(false);
		}
	}

	@Override
	public boolean canAcceptEnergy(final Direction direction) {
		return true;
	}

	@Override
	public boolean canProvideEnergy(final Direction direction) {
		return false;
	}

}
