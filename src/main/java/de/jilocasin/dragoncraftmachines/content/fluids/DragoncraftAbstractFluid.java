package de.jilocasin.dragoncraftmachines.content.fluids;

import java.util.function.Supplier;

import de.jilocasin.dragoncraftmachines.DragoncraftMachinesMod;
import de.jilocasin.dragoncraftmachines.mixin.FlowableFluidAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class DragoncraftAbstractFluid extends FlowableFluid {

	private final Supplier<Fluid> flowingFluidSuppler;
	private final Supplier<Fluid> stillFluidSuppler;
	private final Supplier<FluidBlock> fluidBlockSupplier;
	private final Supplier<BucketItem> bucketItemSuppler;

	private final int levelDecreasePerBlock;
	private final int tickRate;

	public DragoncraftAbstractFluid(
			final Supplier<Fluid> stillFluidSuppler,
			final Supplier<Fluid> flowingFluidSuppler,
			final Supplier<FluidBlock> fluidBlockSupplier,
			final Supplier<BucketItem> bucketItemSuppler,
			final int levelDecreasePerBlock,
			final int tickRate) {
		this.stillFluidSuppler = stillFluidSuppler;
		this.flowingFluidSuppler = flowingFluidSuppler;
		this.fluidBlockSupplier = fluidBlockSupplier;
		this.bucketItemSuppler = bucketItemSuppler;
		this.levelDecreasePerBlock = levelDecreasePerBlock;
		this.tickRate = tickRate;
	}

	@Override
	public Fluid getStill() {
		return stillFluidSuppler.get();
	}

	@Override
	public Fluid getFlowing() {
		return flowingFluidSuppler.get();
	}

	@Override
	protected boolean isInfinite() {
		return false;
	}

	/**
	 * Perform actions when fluid flows into a replaceable block. Water drops the
	 * block's loot table. Lava plays the "block.lava.extinguish" sound.
	 */
	@Override
	protected void beforeBreakingBlock(final WorldAccess world, final BlockPos pos, final BlockState state) {
		final BlockEntity blockEntity = state.getBlock().hasBlockEntity() ? world.getBlockEntity(pos) : null;
		Block.dropStacks(state, world.getWorld(), pos, blockEntity);
	}

	@Override
	protected int getFlowSpeed(final WorldView world) {
		return 4;
	}

	@Override
	public int getLevelDecreasePerBlock(final WorldView world) {
		return levelDecreasePerBlock;
	}

	@Override
	public Item getBucketItem() {
		return bucketItemSuppler.get();
	}

	@Override
	protected boolean canBeReplacedWith(final FluidState state, final BlockView world, final BlockPos pos, final Fluid fluid, final Direction direction) {
		if (isStill(null)) {
			return false;
		}

		final BlockPos sourcePosition = pos.offset(direction.getOpposite());
		final BlockPos targetPosition = pos;

		final FluidState sourceState = world.getFluidState(sourcePosition);
		final FluidState targetState = world.getFluidState(targetPosition);

		final int assumedNewBlockLevel;

		DragoncraftMachinesMod.LOG.info("Flow from " + sourceState.getFluid().toString() + " to " + targetState.getFluid().toString());

		if (sourceState.getFluid() instanceof FlowableFluidAccessor && world instanceof WorldView) {
			final FlowableFluidAccessor sourceFluid = (FlowableFluidAccessor) sourceState.getFluid();

			if (direction.getAxis().isVertical()) {
				assumedNewBlockLevel = 8;
				DragoncraftMachinesMod.LOG.info("Assuming vertical flow");
			} else {
				assumedNewBlockLevel = sourceState.getLevel() - sourceFluid.callGetLevelDecreasePerBlock((WorldView) world);
				DragoncraftMachinesMod.LOG.info("Assuming horizontal flow");
			}
		} else {
			DragoncraftMachinesMod.LOG.error("Assuming decrease of 1");

			DragoncraftMachinesMod.LOG.error(sourceState.getFluid() instanceof FlowableFluidAccessor);
			DragoncraftMachinesMod.LOG.error(world instanceof WorldView);

			// Cannot access getter. Assume a decrease of 1.

			assumedNewBlockLevel = sourceState.getLevel() - 1;
		}

		DragoncraftMachinesMod.LOG.info("Assumed source level " + assumedNewBlockLevel + " @" + sourcePosition);
		DragoncraftMachinesMod.LOG.info("Target level " + targetState.getLevel() + " @" + targetPosition);

		return assumedNewBlockLevel > targetState.getLevel();
	}

	@Override
	public int getTickRate(final WorldView world) {
		return tickRate;
	}

	@Override
	protected float getBlastResistance() {
		return 100;
	}

	@Override
	protected BlockState toBlockState(final FluidState fluidState) {
		return fluidBlockSupplier.get().getDefaultState().with(FluidBlock.LEVEL, method_15741(fluidState));
	}

	@Override
	public int getLevel(final FluidState fluidState) {
		return isStill(fluidState) ? 8 : fluidState.get(LEVEL);
	}

	@Override
	public boolean matchesType(final Fluid fluid) {
		return getFlowing() == fluid || getStill() == fluid;
	}

	@Override
	public String toString() {
		return fluidBlockSupplier.get().getTranslationKey();
	}

	public static class Still extends DragoncraftAbstractFluid {
		public Still(final Supplier<Fluid> stillFluidSuppler, final Supplier<Fluid> flowingFluidSuppler, final Supplier<FluidBlock> fluidBlockSupplier,
				final Supplier<BucketItem> bucketItemSuppler, final int levelDecreasePerBlock, final int flowSpeed) {
			super(stillFluidSuppler, flowingFluidSuppler, fluidBlockSupplier, bucketItemSuppler, levelDecreasePerBlock, flowSpeed);
		}

		@Override
		public boolean isStill(final FluidState fluidState) {
			return true;
		}
	}

	public static class Flowing extends DragoncraftAbstractFluid {
		public Flowing(final Supplier<Fluid> stillFluidSuppler, final Supplier<Fluid> flowingFluidSuppler, final Supplier<FluidBlock> fluidBlockSupplier,
				final Supplier<BucketItem> bucketItemSuppler, final int levelDecreasePerBlock, final int flowSpeed) {
			super(stillFluidSuppler, flowingFluidSuppler, fluidBlockSupplier, bucketItemSuppler, levelDecreasePerBlock, flowSpeed);
		}

		@Override
		protected void appendProperties(final StateManager.Builder<Fluid, FluidState> builder) {
			super.appendProperties(builder);
			builder.add(LEVEL);
		}

		@Override
		public boolean isStill(final FluidState fluidState) {
			return false;
		}
	}
}
