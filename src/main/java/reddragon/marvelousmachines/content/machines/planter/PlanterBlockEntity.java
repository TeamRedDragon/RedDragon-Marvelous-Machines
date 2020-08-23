package reddragon.marvelousmachines.content.machines.planter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import reborncore.client.screen.builder.BuiltScreenHandler;
import reborncore.client.screen.builder.ScreenHandlerBuilder;
import reborncore.common.util.RebornInventory;
import reddragon.marvelousmachines.content.MarvelousMachinesMachine;
import reddragon.marvelousmachines.content.machines.TickingOperationMachineBlockEntity;
import team.reborn.energy.EnergyTier;

public class PlanterBlockEntity extends TickingOperationMachineBlockEntity {

	/**
	 * The side length of the square in front of the planter that it will scan for
	 * possible spaces.
	 * <p>
	 * This value must be an odd number because there must be a single center block
	 * position.
	 */
	private static final int PLANTER_SCAN_SIDE_LENGTH = 5;

	/**
	 * The planter scan radius, excluding the working area center itself.
	 */
	private static final int PLANTER_SCAN_RADIUS = (int) Math.floor(PLANTER_SCAN_SIDE_LENGTH / 2.0);

	private static final double ENERGY_PER_OPERATION = 10;

	private static final int TICKS_PER_OPERATION = 38;

	public static final int INPUT_SLOT = 1;

	public static final int ENERGY_SLOT = 0;

	private final RebornInventory<PlanterBlockEntity> inventory = new RebornInventory<>(2, getClass().getSimpleName(), 64, this);

	public PlanterBlockEntity(final MarvelousMachinesMachine machineType) {
		super(machineType);
	}

	@Override
	public CompoundTag toTag(final CompoundTag tag) {
		super.toTag(tag);

		inventory.write(tag);

		return tag;
	}

	@Override
	public void fromTag(final BlockState state, final CompoundTag tag) {
		super.fromTag(state, tag);

		inventory.read(tag);
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	@Override
	protected int getEnergySlot() {
		return ENERGY_SLOT;
	}

	@Override
	public double getBaseMaxInput() {
		return EnergyTier.MEDIUM.getMaxInput();
	}

	@Override
	protected double getEnergyPerOperation() {
		return ENERGY_PER_OPERATION;
	}

	@Override
	protected int getTicksPerOperation() {
		return TICKS_PER_OPERATION;
	}

	/**
	 * The planter can perform an operation if there is a tillable block or if a
	 * sapling from its input slot can be placed anywhere.
	 */
	@Override
	protected boolean couldPerformOperation() {
		return findTillableBlockPosition().isPresent() || (!findEmptyFarmlandBlockPositions().isEmpty() && !inventory.getStack(INPUT_SLOT).isEmpty());
	}

	/**
	 * Each operation will try to first till a soil block and then start placing
	 * saplings from the input slot.
	 */
	@Override
	protected void performOperation() {

		// Try to till a soil block first.

		final Optional<BlockPos> newFarmlandPosition = findTillableBlockPosition();

		if (newFarmlandPosition.isPresent()) {
			if (world.isClient) {
				// Clients will play the sound only.

				final Vec3d farmlandSoundPosition = Vec3d
						.ofCenter(new Vec3i(newFarmlandPosition.get().getX(), newFarmlandPosition.get().getY(), newFarmlandPosition.get().getZ()));

				world.playSound(
						farmlandSoundPosition.getX(),
						farmlandSoundPosition.getY(),
						farmlandSoundPosition.getZ(),
						SoundEvents.ITEM_HOE_TILL,
						SoundCategory.BLOCKS,
						1.0f,
						1.0f,
						false);
			} else {
				world.setBlockState(newFarmlandPosition.get(), Blocks.FARMLAND.getDefaultState(), 3);
			}

			return;
		}

		// Secondary, try to use the item in the input slot.

		if (!world.isClient) {
			final ItemStack inputSlotItem = inventory.getStack(INPUT_SLOT);

			if (!inputSlotItem.isEmpty() && inputSlotItem.getItem() instanceof BlockItem) {
				final List<BlockPos> emptyPositions = findEmptyFarmlandBlockPositions();

				for (final BlockPos emptyPosition : emptyPositions) {
					if (tryPlace(inputSlotItem, emptyPosition)) {
						return;
					}
				}
			}
		}
	}

	/**
	 * Tries to find a block position with a dirt or grass block beneath the working
	 * area.
	 */
	private Optional<BlockPos> findTillableBlockPosition() {
		final BlockPos centerPosition = getWorkingAreaCenterPos();

		for (int offsetX = -PLANTER_SCAN_RADIUS; offsetX <= PLANTER_SCAN_RADIUS; offsetX++) {
			for (int offsetZ = -PLANTER_SCAN_RADIUS; offsetZ <= PLANTER_SCAN_RADIUS; offsetZ++) {
				final BlockPos checkedPos = centerPosition.add(offsetX, -1, offsetZ);
				final BlockState blockState = world.getBlockState(checkedPos);

				if (blockState.getBlock().is(Blocks.DIRT) || blockState.getBlock().is(Blocks.GRASS_BLOCK)) {
					final BlockPos checkedPosAbove = checkedPos.add(0, 1, 0);
					final BlockState blockStateAbove = world.getBlockState(checkedPosAbove);

					if (blockStateAbove.getBlock().is(Blocks.AIR)) {
						return Optional.of(checkedPos);
					}
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Tries to find a block position with a farmland or sand block and an empty
	 * space above.
	 */
	private List<BlockPos> findEmptyFarmlandBlockPositions() {
		final List<BlockPos> result = new ArrayList<>();

		final BlockPos centerPosition = getWorkingAreaCenterPos();

		for (int offsetX = -PLANTER_SCAN_RADIUS; offsetX <= PLANTER_SCAN_RADIUS; offsetX++) {
			for (int offsetZ = -PLANTER_SCAN_RADIUS; offsetZ <= PLANTER_SCAN_RADIUS; offsetZ++) {
				final BlockPos checkedPos = centerPosition.add(offsetX, -1, offsetZ);
				final BlockState blockState = world.getBlockState(checkedPos);

				if (blockState.getBlock().is(Blocks.FARMLAND) || blockState.getBlock().isIn(BlockTags.SAND)) {
					final BlockPos checkedPosAbove = checkedPos.add(0, 1, 0);
					final BlockState blockStateAbove = world.getBlockState(checkedPosAbove);

					if (blockStateAbove.getBlock().is(Blocks.AIR)) {
						result.add(checkedPosAbove);
					}
				}
			}
		}

		return result;
	}

	private boolean tryPlace(final ItemStack inputSlotItem, final BlockPos position) {
		final AutomaticItemPlacementContext context = new AutomaticItemPlacementContext(
				world,
				position,
				getFacing(),
				inputSlotItem,
				getFacing().getOpposite());

		return ((BlockItem) inputSlotItem.getItem()).place(context).isAccepted();
	}

	@Override
	public BuiltScreenHandler createScreenHandler(final int syncID, final PlayerEntity playerEntity) {
		return new ScreenHandlerBuilder(getClass().getSimpleName())
				.player(playerEntity.inventory)
				.inventory()
				.hotbar()
				.addInventory()
				.blockEntity(this)
				.filterSlot(INPUT_SLOT, 80, 45, this::isValidInputItem)
				.energySlot(ENERGY_SLOT, 8, 72)
				.syncEnergyValue()
				.addInventory()
				.create(this, syncID);
	}

	private boolean isValidInputItem(final ItemStack itemStack) {
		final Block placedBlock = Block.getBlockFromItem(itemStack.getItem());

		return itemStack.getItem().isIn(ItemTags.SAPLINGS) || placedBlock.isIn(BlockTags.CROPS) || placedBlock.isIn(BlockTags.SAPLINGS);
	}

	/**
	 * Returns the center of the working area of this planter. The center is
	 * <code>WORKING_RADIUS + 1</code> blocks away from the entity.
	 */
	private BlockPos getWorkingAreaCenterPos() {
		return pos.offset(getFacing(), PLANTER_SCAN_RADIUS + 1);
	}
}
