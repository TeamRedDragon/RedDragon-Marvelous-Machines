package de.jilocasin.dragoncraftmachines.content.machines;

import de.jilocasin.dragoncraftmachines.content.DragoncraftMachine;

public interface BlockEntitySupplier {
	AbstractMachineBlockEntity create(DragoncraftMachine machineType);
}