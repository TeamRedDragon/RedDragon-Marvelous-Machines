package de.jilocasin.dragoncraftmachines.content.fluids;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class VaporizingFluidBlock extends FluidBlock {

	public VaporizingFluidBlock(final FlowableFluid fluid, final Settings properties) {
		super(fluid, properties);
	}

	private class VaporizedResultChance {
		public Block block;
		public float accumulatedWeight;
	}

	private final List<VaporizedResultChance> vaporizedResultChances = new ArrayList<>();
	private float accumulatedWeight;

	public void addVaporizedResultChance(final Block block, final float weight) {
		accumulatedWeight += weight;

		final VaporizedResultChance vaporizedResultChance = new VaporizedResultChance();
		vaporizedResultChance.accumulatedWeight = accumulatedWeight;
		vaporizedResultChance.block = block;

		vaporizedResultChances.add(vaporizedResultChance);
	}

	public Block getVaporizedResult(final World world) {
		final float randomValue = world.random.nextFloat() * accumulatedWeight;

		for (final VaporizedResultChance vaporizedResultChance : vaporizedResultChances) {
			if (vaporizedResultChance.accumulatedWeight >= randomValue) {
				return vaporizedResultChance.block;
			}
		}

		return Blocks.WATER; // should only happen when there are no entries
	}

	@Override
	public void randomTick(final BlockState state, final ServerWorld world, final BlockPos pos, final Random random) {
		if (canVaporize(state, world, pos)) {
			vaporize(state, world, pos);
		}
	}

	private boolean canVaporize(final BlockState state, final ServerWorld world, final BlockPos pos) {
		if (!isSourceBlock(state)) {
			return false;
		}

		if (world.getLightLevel(pos.up(), 0) < world.getMaxLightLevel()) {
			return false;
		}

		if (isWaterNearby(world, pos, 1, 7)) {
			return false;
		}

		if (world.hasRain(pos.up())) {
			return false;
		}

		return true;
	}

	private void vaporize(final BlockState state, final World world, final BlockPos pos) {
		world.setBlockState(pos, getVaporizedResult(world).getDefaultState(), 3);
		world.syncWorldEvent(1501, pos, 0); // ExtingushEvent
	}

	private boolean isSourceBlock(final BlockState state) {
		final FluidState fluidState = getFluidState(state);
		return fluidState.isStill();
	}

	private boolean isWaterNearby(final WorldView world, final BlockPos pos, final int radius, final int sources) {
		int count = 0;
		final Iterator<BlockPos> var2 = BlockPos.iterate(pos.add(-radius, -1, -radius), pos.add(radius, 0, radius))
				.iterator();

		BlockPos blockPos;
		do {
			if (!var2.hasNext()) {
				return false;
			}
			blockPos = var2.next();
			final FluidState fluidState = world.getFluidState(blockPos);
			if (fluidState.isIn(FluidTags.WATER) && fluidState.isStill()) {
				count++;
			}
		} while (count <= sources);

		return true;
	}

	@Override
	public boolean hasRandomTicks(final BlockState state) {
		return true;
	}

}
