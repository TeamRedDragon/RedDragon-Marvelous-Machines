package reddragon.marvelousmachines.content;

import static reddragon.marvelousmachines.MarvelousMachinesMod.ITEMGROUP;
import static reddragon.marvelousmachines.MarvelousMachinesMod.NAMESPACE;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import reddragon.api.configs.ModBlockConfig;
import reddragon.api.configs.RegisterableBlock;
import reddragon.api.content.BlockHolder;
import reddragon.marvelousmachines.content.blocks.MudBlock;

public enum MarvelousMachinesBlock implements BlockHolder, RegisterableBlock {
	MUD_BLOCK(new MudBlock(FabricBlockSettings.of(Material.SOLID_ORGANIC)
			.strength(1.2f, 5.0f)
			.slipperiness(0.985f)
			.breakByTool(FabricToolTags.SHOVELS)
			.breakByHand(true)
			.jumpVelocityMultiplier(0.5f)
			.sounds(BlockSoundGroup.HONEY)
			.ticksRandomly()));

	private final ModBlockConfig config;

	private MarvelousMachinesBlock(final Block block) {
		config = new ModBlockConfig(block);
	}

	@Override
	public Block getBlock() {
		return config.getBlock();
	}

	@Override
	public ModBlockConfig getConfig() {
		return config;
	}

	@Override
	public ItemGroup getItemGroup() {
		return ITEMGROUP;
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}
}
