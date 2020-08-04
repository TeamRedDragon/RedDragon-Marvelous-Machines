package de.jilocasin.dragoncraftmachines.content;

import java.util.Locale;

import de.jilocasin.dragoncraftmachines.DragoncraftMachinesMod;
import de.jilocasin.dragoncraftmachines.content.blocks.MudBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import reborncore.RebornRegistry;

public enum DragoncraftBlock implements BlockHolder {
	MUD_BLOCK(FabricBlockSettings.of(Material.SOLID_ORGANIC)
			.strength(1.2f, 5.0f)
			.slipperiness(0.985f)
			.sounds(BlockSoundGroup.SLIME)
			.breakByTool(FabricToolTags.SHOVELS)
			.breakByHand(true)
			.ticksRandomly());

	private Block block;

	private DragoncraftBlock(final Settings settings) {
		block = new MudBlock(settings);
	}

	@Override
	public Block getBlock() {
		return block;
	}

	public void register() {
		RebornRegistry.registerBlock(
				block,
				new BlockItem(getBlock(), new Item.Settings().group(DragoncraftMachinesMod.ITEMGROUP)),
				buildIdentifier());
	}

	/**
	 * Returns an {@link Identifier} for this block.
	 */
	public Identifier buildIdentifier() {
		return new Identifier(DragoncraftMachinesMod.NAMESPACE, name().toLowerCase(Locale.ROOT));
	}
}
