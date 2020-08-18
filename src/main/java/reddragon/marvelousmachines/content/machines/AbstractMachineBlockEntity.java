package reddragon.marvelousmachines.content.machines;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.SidedInventory;
import reborncore.api.IListInfoProvider;
import reborncore.api.blockentity.InventoryProvider;
import reborncore.client.screen.BuiltScreenHandlerProvider;
import reborncore.common.blocks.BlockMachineBase;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import reddragon.marvelousmachines.content.MarvelousMachinesMachine;

public abstract class AbstractMachineBlockEntity extends PowerAcceptorBlockEntity
		implements InventoryProvider, SidedInventory, IListInfoProvider, BuiltScreenHandlerProvider {

	public AbstractMachineBlockEntity(final MarvelousMachinesMachine machineType) {
		super(machineType.getEntityType());
	}

	protected abstract int getEnergySlot();

	@Override
	public abstract double getBaseMaxInput();

	@Override
	public double getBaseMaxPower() {
		return 10_000;
	}

	@Override
	public double getBaseMaxOutput() {
		return 0;
	}

	@Override
	public void tick() {
		super.tick();

		charge(getEnergySlot());
	}

	/**
	 * Updates the ACTIVE property of the the block state related to this entity.
	 * <p>
	 * The property is only updated if its current state differs from the new state.
	 */
	public void setIsActive(final boolean isActive) {
		final BlockState state = world.getBlockState(pos);
		final Block block = state.getBlock();

		if (block instanceof BlockMachineBase) {
			final BlockMachineBase blockMachineBase = (BlockMachineBase) block;

			if (blockMachineBase.isActive(state) != isActive) {
				blockMachineBase.setActive(isActive, world, pos);
				world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			}
		}
	}
}
