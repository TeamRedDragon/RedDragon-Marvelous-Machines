package reddragon.marvelousmachines.content.machines;

import net.minecraft.inventory.SidedInventory;
import reborncore.api.IListInfoProvider;
import reborncore.api.blockentity.InventoryProvider;
import reborncore.client.screen.BuiltScreenHandlerProvider;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import reddragon.marvelousmachines.content.ModMachine;

public abstract class AbstractMachineBlockEntity extends PowerAcceptorBlockEntity
		implements InventoryProvider, SidedInventory, IListInfoProvider, BuiltScreenHandlerProvider {

	public AbstractMachineBlockEntity(final ModMachine machineType) {
		super(machineType.getEntityType());
	}

	protected abstract int getEnergySlot();

	@Override
	public abstract double getBaseMaxInput();

	@Override
	public boolean isActive() {
		return true;
	}

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

		if (world.isClient) {
			return;
		}

		if (!isActive()) {
			return;
		}

		charge(getEnergySlot());
	}
}
