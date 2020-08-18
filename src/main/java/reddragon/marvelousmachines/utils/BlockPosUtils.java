package reddragon.marvelousmachines.utils;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

public class BlockPosUtils {

	public static CompoundTag toTag(final BlockPos position) {
		final CompoundTag tag = new CompoundTag();

		tag.putInt("x", position.getX());
		tag.putInt("y", position.getY());
		tag.putInt("z", position.getZ());

		return tag;
	}

	public static BlockPos fromTag(final CompoundTag tag) {
		return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
	}

	public static CompoundTag allToTag(final Collection<BlockPos> positions) {
		final CompoundTag tag = new CompoundTag();

		int i = 0;
		for (final BlockPos position : positions) {
			tag.put(String.valueOf(i), toTag(position));
			i++;
		}

		return tag;
	}

	public static Collection<BlockPos> allFromTag(final CompoundTag tag) {
		final ArrayList<BlockPos> result = new ArrayList<>();

		for (final String key : tag.getKeys()) {
			result.add(fromTag(tag.getCompound(key)));
		}

		return result;
	}
}
