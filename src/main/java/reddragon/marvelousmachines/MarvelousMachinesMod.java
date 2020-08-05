package reddragon.marvelousmachines;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.ItemGroup;
import reddragon.api.utils.ItemGroupUtils;
import reddragon.marvelousmachines.content.MarvelousMachinesBlock;
import reddragon.marvelousmachines.content.MarvelousMachinesFluid;
import reddragon.marvelousmachines.content.ModMachine;

public class MarvelousMachinesMod implements ModInitializer {

	public static final String NAMESPACE = "marvelousmachines";

	public static final ItemGroup ITEMGROUP = ItemGroupUtils.createItemGroup(NAMESPACE,
			ModMachine.STONE_BREAKER.getBlock());

	public static final Logger LOG = LogManager.getLogger(NAMESPACE);

	@Override
	public void onInitialize() {
		for (final ModMachine machine : ModMachine.values()) {
			machine.register();
		}

		for (final MarvelousMachinesBlock block : MarvelousMachinesBlock.values()) {
			block.getConfig().register(NAMESPACE, block.name());
		}

		for (final MarvelousMachinesFluid fluid : MarvelousMachinesFluid.values()) {
			fluid.getConfig().register(NAMESPACE, fluid.name());
		}
	}

}
