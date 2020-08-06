package reddragon.marvelousmachines.content.machines;

import reddragon.marvelousmachines.content.MarvelousMachinesMachine;

public interface BlockSupplier {
	AbstractMachineBlock create(MarvelousMachinesMachine machineType, BlockEntitySupplier blockEntity);
}