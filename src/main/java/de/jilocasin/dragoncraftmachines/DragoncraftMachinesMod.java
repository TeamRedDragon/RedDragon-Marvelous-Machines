package de.jilocasin.dragoncraftmachines;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.jilocasin.dragoncraftmachines.content.DragoncraftBlock;
import de.jilocasin.dragoncraftmachines.content.DragoncraftFluid;
import de.jilocasin.dragoncraftmachines.content.DragoncraftMachine;
import de.jilocasin.dragoncraftmachines.utils.ItemGroupUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.ItemGroup;

public class DragoncraftMachinesMod implements ModInitializer {

	public static final String NAMESPACE = "dragoncraftmachines";

	public static final ItemGroup ITEMGROUP = ItemGroupUtils.createItemGroup();

	public static final Logger LOG = LogManager.getLogger(NAMESPACE);

	@Override
	public void onInitialize() {
		for (final DragoncraftMachine machine : DragoncraftMachine.values()) {
			machine.register();
		}

		for (final DragoncraftBlock block : DragoncraftBlock.values()) {
			block.register();
		}

		for (final DragoncraftFluid modFluid : DragoncraftFluid.values()) {
			modFluid.register();
		}
	}

}
