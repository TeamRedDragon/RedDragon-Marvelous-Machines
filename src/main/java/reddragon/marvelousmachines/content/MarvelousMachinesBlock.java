package reddragon.marvelousmachines.content;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import reddragon.api.configs.ModBlockConfig;
import reddragon.api.content.BlockHolder;

public enum MarvelousMachinesBlock implements BlockHolder {
	MUD_BLOCK(FabricBlockSettings.of(Material.SOLID_ORGANIC)
			.strength(1.2f, 5.0f)
			.slipperiness(0.985f)
			.sounds(BlockSoundGroup.SLIME)
			.breakByTool(FabricToolTags.SHOVELS)
			.breakByHand(true)
			.ticksRandomly());

	private final ModBlockConfig config;

	private MarvelousMachinesBlock(final ModBlockConfig config) {
		this.config = config;
	}

	private MarvelousMachinesBlock(FabricBlockSettings settings) {
		config = new ModBlockConfig(settings);
	}

	@Override
	public Block getBlock() {
		return config.getBlock();
	}

	public ModBlockConfig getConfig() {
		return config;
	}
}
