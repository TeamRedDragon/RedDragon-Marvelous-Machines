package de.jilocasin.dragoncraftmachines.utils;

import de.jilocasin.dragoncraftmachines.DragoncraftMachinesMod;
import de.jilocasin.dragoncraftmachines.content.DragoncraftMachine;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ItemGroupUtils {
	private static final String ITEM_GROUP_IDENTIFIER_PATH = "item_group";

	public static ItemGroup createItemGroup() {
		return FabricItemGroupBuilder.create(
				new Identifier(DragoncraftMachinesMod.NAMESPACE, ITEM_GROUP_IDENTIFIER_PATH))
				.icon(() -> new ItemStack(DragoncraftMachine.STONE_BREAKER.getBlock()))
				.build();
	}
}
