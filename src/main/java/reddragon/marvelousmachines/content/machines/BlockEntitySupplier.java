package reddragon.marvelousmachines.content.machines;

import reddragon.marvelousmachines.content.MarvelousMachinesMachine;

public interface BlockEntitySupplier {
	AbstractMachineBlockEntity create(MarvelousMachinesMachine machineType);
}