package reddragon.marvelousmachines.content;

import net.minecraft.block.Block;

/**
 * Indicates that each class instance has a {@link Block} instance that can be
 * access via {@link #getBlock()}.
 */
public interface BlockHolder {
	Block getBlock();
}
