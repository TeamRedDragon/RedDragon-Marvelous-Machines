package reddragon.marvelousmachines.content.machines;

import reddragon.marvelousmachines.content.ModMachine;

public interface BlockEntitySupplier {
	AbstractMachineBlockEntity create(ModMachine machineType);
}