package reddragon.marvelousmachines;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.ItemGroup;
import reddragon.marvelousmachines.content.ModBlock;
import reddragon.marvelousmachines.content.ModFluid;
import reddragon.marvelousmachines.content.ModMachine;
import reddragon.marvelousmachines.utils.ItemGroupUtils;

public class MarvelousMachinesMod implements ModInitializer {

	public static final String NAMESPACE = "marvelousmachines";

	public static final ItemGroup ITEMGROUP = ItemGroupUtils.createItemGroup();

	public static final Logger LOG = LogManager.getLogger(NAMESPACE);

	@Override
	public void onInitialize() {
		for (final ModMachine machine : ModMachine.values()) {
			machine.register();
		}

		for (final ModBlock block : ModBlock.values()) {
			block.register();
		}

		for (final ModFluid modFluid : ModFluid.values()) {
			modFluid.register();
		}
	}

}
