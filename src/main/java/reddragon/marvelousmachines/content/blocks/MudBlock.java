package reddragon.marvelousmachines.content.blocks;

import java.util.Iterator;
import java.util.Random;

import org.apache.commons.lang3.RandomUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.IntProperty;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class MudBlock extends Block {

	public static final IntProperty MOISTURE = IntProperty.of("moisture", 0, 7);

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(MOISTURE);
	}

	public MudBlock(Settings settings) {
		super(settings);
		setDefaultState(stateManager.getDefaultState().with(MOISTURE, 7));
	}

	@Override
	public boolean hasRandomTicks(BlockState state) {
		return true;
	}

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		int moisture = state.get(MOISTURE);
		if (isAdjacentLit(world, pos)) {
			// its only drying at sunlight / heat

			if (isWaterNearby(world, pos, 1) || world.hasRain(pos.up())) {
				// if its raining or water is nearby, it stays moist
				world.setBlockState(pos, state.with(MOISTURE, 7), 2);
			} else {
				if (moisture > 0) {
					world.setBlockState(pos, state.with(MOISTURE, moisture - 1), 2);
				} else {
					setToDryBlock(state, world, pos);
				}
			}
		} else {
			// without sunlight/heat its not drying out!
			if (isWaterNearby(world, pos, 3) || world.hasRain(pos.up())) {
				// if its raining or water is nearby, it stays moist
				world.setBlockState(pos, state.with(MOISTURE, 7), 2);
			}
		}
	}

	public static void setToDryBlock(BlockState state, World world, BlockPos pos) {
		float randomRoll = RandomUtils.nextFloat(0, 9);
		// In a 3x3 Field about 2 become CLAY and about 1 become MYCELIUM while the
		// rest becomes DIRT
		BlockState dryBlock;
		if (randomRoll <= 2f) {
			dryBlock = Blocks.CLAY.getDefaultState();
		} else if (randomRoll <= 8f) {
			dryBlock = Blocks.DIRT.getDefaultState();
		} else {
			dryBlock = Blocks.MYCELIUM.getDefaultState();
		}
		world.setBlockState(pos, dryBlock);
		world.syncWorldEvent(1501, pos, 0); // ExtingushEvent
	}

	private static boolean isWaterNearby(WorldView world, BlockPos pos, int radius) {
		Iterator<BlockPos> var2 = BlockPos.iterate(pos.add(-radius, -1, -radius), pos.add(radius, 1, radius))
				.iterator();

		BlockPos blockPos;
		do {
			if (!var2.hasNext()) {
				return false;
			}
			blockPos = var2.next();
		} while (!world.getFluidState(blockPos).isIn(FluidTags.WATER));
		return true;
	}

	private static boolean isAdjacentLit(WorldView world, BlockPos pos) {
		return (world.getLightLevel(pos.up(), 0) >= 15)
				|| (world.getLightLevel(pos.north(), 0) >= 15)
				|| (world.getLightLevel(pos.east(), 0) >= 15)
				|| (world.getLightLevel(pos.down(), 0) >= 15)
				|| (world.getLightLevel(pos.south(), 0) >= 15)
				|| (world.getLightLevel(pos.west(), 0) >= 15);
	}

}
