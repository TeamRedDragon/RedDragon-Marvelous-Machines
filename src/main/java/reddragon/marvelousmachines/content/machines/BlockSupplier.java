package reddragon.marvelousmachines.content.machines;

import reddragon.marvelousmachines.content.ModMachine;

public interface BlockSupplier {
	AbstractMachineBlock create(ModMachine machineType, BlockEntitySupplier blockEntity);
}