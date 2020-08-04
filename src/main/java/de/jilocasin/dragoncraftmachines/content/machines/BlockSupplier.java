package de.jilocasin.dragoncraftmachines.content.machines;

import de.jilocasin.dragoncraftmachines.content.DragoncraftMachine;

public interface BlockSupplier {
	AbstractMachineBlock create(DragoncraftMachine machineType, BlockEntitySupplier blockEntity);
}