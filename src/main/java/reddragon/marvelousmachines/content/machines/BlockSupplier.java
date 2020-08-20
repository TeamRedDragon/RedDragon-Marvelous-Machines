package reddragon.marvelousmachines.content.machines;

import reddragon.marvelousmachines.content.MarvelousMachinesMachine;

public interface BlockSupplier {
	GenericMachineBlock create(MarvelousMachinesMachine machineType, BlockEntitySupplier blockEntity);
}