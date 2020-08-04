package reddragon.marvelousmachines.utils;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import reddragon.marvelousmachines.MarvelousMachinesMod;
import reddragon.marvelousmachines.content.ModMachine;

public class ItemGroupUtils {
	private static final String ITEM_GROUP_IDENTIFIER_PATH = "item_group";

	public static ItemGroup createItemGroup() {
		return FabricItemGroupBuilder.create(
				new Identifier(MarvelousMachinesMod.NAMESPACE, ITEM_GROUP_IDENTIFIER_PATH))
				.icon(() -> new ItemStack(ModMachine.STONE_BREAKER.getBlock()))
				.build();
	}
}
