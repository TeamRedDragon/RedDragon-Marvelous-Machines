package reddragon.marvelousmachines.content.blocks;

import static reddragon.api.content.fluids.DryingFluidBlock.MIN_LIGHT_LEVEL_FOR_DRYING;

import java.util.Iterator;
import java.util.Random;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.IntProperty;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import reddragon.api.random.RandomPicker;
import reddragon.marvelousmachines.content.MarvelousMachinesBlock;

public class MudBlock extends Block {

	public static final IntProperty MOISTURE = IntProperty.of("moisture", 0, 7);

	/**
	 * In a 3x3 grid statistically 2 blocks become CLAY, 1 becomes MYCELIUM and 6
	 * become DIRT
	 */
	private static final RandomPicker<Block> DRIED_RESULT_PICKER = new RandomPicker<Block>()
			.withChance(4, () -> Blocks.CLAY)
			.withChance(16, () -> Blocks.DIRT)
			.withChance(1, () -> Blocks.MYCELIUM);

	@Override
	protected void appendProperties(final Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(MOISTURE);
	}

	public MudBlock(final Settings settings) {
		super(settings);
		setDefaultState(stateManager.getDefaultState().with(MOISTURE, 7));
	}

	@Override
	public boolean hasRandomTicks(final BlockState state) {
		return true;
	}

	@Override
	public void randomTick(final BlockState state, final ServerWorld world, final BlockPos pos, final Random random) {
		final int moisture = state.get(MOISTURE);
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

	public static void setToDryBlock(final BlockState state, final World world, final BlockPos pos) {
		final Block dryBlock = DRIED_RESULT_PICKER.pick();

		world.setBlockState(pos, dryBlock.getDefaultState());
		world.syncWorldEvent(1501, pos, 0); // ExtingushEvent
	}

	private static boolean isWaterNearby(final WorldView world, final BlockPos pos, final int radius) {
		final Iterator<BlockPos> var2 = BlockPos.iterate(pos.add(-radius, -1, -radius), pos.add(radius, 1, radius))
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

	private static boolean isAdjacentLit(final WorldView world, final BlockPos pos) {
		return (world.getLightLevel(pos.up(), 0) >= MIN_LIGHT_LEVEL_FOR_DRYING)
				|| (world.getLightLevel(pos.north(), 0) >= MIN_LIGHT_LEVEL_FOR_DRYING)
				|| (world.getLightLevel(pos.east(), 0) >= MIN_LIGHT_LEVEL_FOR_DRYING)
				|| (world.getLightLevel(pos.down(), 0) >= MIN_LIGHT_LEVEL_FOR_DRYING)
				|| (world.getLightLevel(pos.south(), 0) >= MIN_LIGHT_LEVEL_FOR_DRYING)
				|| (world.getLightLevel(pos.west(), 0) >= MIN_LIGHT_LEVEL_FOR_DRYING);
	}

	@Override
	public void onLandedUpon(final World world, final BlockPos pos, final Entity entity, final float distance) {
		super.onLandedUpon(world, pos, entity, distance);

		entity.playSound(SoundEvents.BLOCK_HONEY_BLOCK_SLIDE, 1.0F, 1.0F);

		addParticles(world, entity.getPos(), 10);
	}

	@Override
	public void onPlaced(final World world, final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);

		final Vec3d particlePos = Vec3d.ofCenter(new Vec3i(pos.getX(), pos.getY(), pos.getZ()));
		addParticles(world, particlePos, 10);
	}

	@Environment(EnvType.CLIENT)
	private static void addParticles(final World world, final Vec3d vec3d, final int count) {
		if (world.isClient) {
			final BlockState blockState = MarvelousMachinesBlock.MUD_BLOCK.getBlock().getDefaultState();

			for (int i = 0; i < count; ++i) {
				world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState), vec3d.getX(), vec3d.getY(), vec3d.getZ(), 0.0D, 0.0D,
						0.0D);
			}

		}
	}
}
