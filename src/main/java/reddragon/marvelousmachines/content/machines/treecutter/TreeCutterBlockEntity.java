package reddragon.marvelousmachines.content.machines.treecutter;

import static reddragon.marvelousmachines.utils.BlockPosUtils.allFromTag;
import static reddragon.marvelousmachines.utils.BlockPosUtils.allToTag;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import reborncore.client.screen.builder.BlockEntityScreenHandlerBuilder;
import reborncore.client.screen.builder.BuiltScreenHandler;
import reborncore.client.screen.builder.ScreenHandlerBuilder;
import reborncore.common.fluid.FluidValue;
import reborncore.common.util.ItemUtils;
import reborncore.common.util.RebornInventory;
import reborncore.common.util.Tank;
import reddragon.marvelousmachines.content.MarvelousMachinesFluid;
import reddragon.marvelousmachines.content.MarvelousMachinesMachine;
import reddragon.marvelousmachines.content.machines.TickingOperationMachineBlockEntity;
import team.reborn.energy.EnergyTier;
import techreborn.utils.FluidUtils;

public class TreeCutterBlockEntity extends TickingOperationMachineBlockEntity {

	/**
	 * The side length of the square in front of the tree cutter that it will scan
	 * for trees (log blocks).
	 * <p>
	 * This value must be an odd number because there must be a single center block
	 * position.
	 */
	private static final int TREE_SCAN_SIDE_LENGTH = 5;

	/**
	 * The side length of the square around each harvested tree that the tree cutter
	 * will try to catch all of the tree, including leaves.
	 * <p>
	 * This value must be an odd number because there must be a single center block
	 * position (the log block).
	 */
	private static final int TREE_CUTTING_SIDE_LENGTH = 9;

	/**
	 * The tree scan radius, excluding the working area center itself.
	 */
	private static final int TREE_SCAN_RADIUS = (int) Math.floor(TREE_SCAN_SIDE_LENGTH / 2.0);

	/**
	 * The tree cutting radius, excluding the tree center itself.
	 */
	private static final int TREE_CUTTING_RADIUS = (int) Math.floor(TREE_CUTTING_SIDE_LENGTH / 2.0);

	private static final double ENERGY_PER_OPERATION = 20;

	private static final int TICKS_PER_OPERATION = 8;

	/**
	 * The maximum number of blocks the tree cutter may check per tick. This limit
	 * simply exists for performance reasons to avoid checking too many blocks (if
	 * all are non-harvestable) before finding a harvestable block.
	 */
	private static final int MAX_BLOCK_CHECKS_PER_TICK = 256;

	public static final FluidValue SEWAGE_TANK_CAPACITY = FluidValue.BUCKET.multiply(16);

	/**
	 * The millibuckets of sewage per harvested block.
	 */
	public static final int SEWAGE_MB_PER_BLOCK = 10;

	public static final List<Integer> OUTPUT_SLOTS = Arrays.asList(3, 4, 5, 6);

	public static final int ENERGY_SLOT = 0;

	public static final int BUCKET_INPUT_SLOT = 1;

	public static final int BUCKET_OUTPUT_SLOT = 2;

	/**
	 * Because of how {@link FluidValue} is implemented by Tech Reborn, we can only
	 * output fluid amounts of at least 250.
	 * <p>
	 * To implement smaller steps, we create this internal counter that holds
	 * sub-250 values and will produce a 250 fluid value when being >= 250.
	 */
	private int sewageMbCounter = 0;

	private final Tank sewageTank = new Tank("sewageTank", SEWAGE_TANK_CAPACITY, this);

	private final RebornInventory<TreeCutterBlockEntity> inventory = new RebornInventory<>(OUTPUT_SLOTS.size() + 3, getClass().getSimpleName(), 64, this);

	/**
	 * Remaining positions of blocks to be harvested.
	 */
	private final Deque<BlockPos> pendingBlockPositions = new LinkedList<>();

	/**
	 * Indicates that this tree cutter has currently stopped working because its
	 * output inventory is full. This flag is reset if the inventory has an empty
	 * output slot again.
	 */
	private boolean isBlockedByOutputInventory = false;

	public TreeCutterBlockEntity(final MarvelousMachinesMachine machineType) {
		super(machineType);
	}

	@Override
	public CompoundTag toTag(final CompoundTag tag) {
		super.toTag(tag);

		sewageTank.write(tag);
		inventory.write(tag);
		tag.put("pendingBlockPositions", allToTag(pendingBlockPositions));
		tag.putBoolean("isBlockedByOutputInventory", isBlockedByOutputInventory);
		tag.putInt("sewageMbCounter", sewageMbCounter);

		return tag;
	}

	@Override
	public void fromTag(final BlockState state, final CompoundTag tag) {
		super.fromTag(state, tag);

		sewageTank.read(tag);
		inventory.read(tag);
		pendingBlockPositions.clear();

		// Just apply the pending block positions for the server instance. This is
		// because the client never knows about the list and will never poll elements
		// from it.

		pendingBlockPositions.addAll(allFromTag(tag.getCompound("pendingBlockPositions")));
		isBlockedByOutputInventory = tag.getBoolean("isBlockedByOutputInventory");
		sewageMbCounter = tag.getInt("sewageMbCounter");
	}

	@Override
	public Tank getTank() {
		return sewageTank;
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

	@Override
	public void tick() {
		if (hasAnyEmptyOutputSlot()) {
			isBlockedByOutputInventory = false;
		}

		if (!world.isClient && world.getTime() % 20 == 0) {
			if (!inventory.getStack(BUCKET_INPUT_SLOT).isEmpty()) {
				FluidUtils.fillContainers((Tank)sewageTank, (Inventory)inventory, BUCKET_INPUT_SLOT, BUCKET_OUTPUT_SLOT);
			}

			// Required to keep items, fluids and entity properties (like the list of
			// pending positions) in sync with the client.

			syncWithAll();
		}

		super.tick();
	}

	/**
	 * The tree cutter can perform an operation if there are still pending block
	 * positions to be processed or there is any log block in the working area to
	 * start from. Also the sewage output must not be full or contain a different
	 * fluid.
	 */
	@Override
	protected boolean couldPerformOperation() {
		if (isBlockedByOutputInventory) {
			return false;
		}

		// Check the fluid tank capacity and fluid type.

		if (!sewageTank.canFit(MarvelousMachinesFluid.SEWAGE.getFluid(), FluidValue.BUCKET_QUARTER)) {
			return false;
		}

		return !pendingBlockPositions.isEmpty() || nextLogPosInWorkingArea().isPresent();
	}

	/**
	 * Each operation will try to process the list of pending block positions first.
	 * If there are not pending block positions left, the tree cutter will try to
	 * re-fill the list based on the next log block in its working area.
	 */
	@Override
	protected void performOperation() {
		if (world.isClient) {
			return;
		}

		if (isBlockedByOutputInventory) {
			return;
		}

		if (pendingBlockPositions.isEmpty()) {
			findAndQueueNextTree();
		}

		// Perform a block harvest operation on the next position in the list (even if
		// the position was just added).

		tryHarvestNextQueuedPosition(MAX_BLOCK_CHECKS_PER_TICK);
	}

	/**
	 * Tries to find a tree (by checking for a log block at the machine Y level) and
	 * queues all corresponding block positions that are considered part of that
	 * tree.
	 * <p>
	 * Note that due to performance reasons the queuing does not check for all
	 * individual blocks and simply queues <i>all</i> potential positions inside the
	 * tree bounding box. Block checks are then performed per tick when trying to
	 * harvest that block.
	 */
	private void findAndQueueNextTree() {
		final Optional<BlockPos> nextLogBlockPosition = nextLogPosInWorkingArea();

		if (!nextLogBlockPosition.isPresent()) {
			return;
		}

		final BlockPos treeBasePosition = nextLogBlockPosition.get();
		final BlockPos treeTopPosition = findTreeTopPosition(treeBasePosition);

		// Queue the tree top. All further queuing will be performed by the harvested
		// block.

		pendingBlockPositions.add(treeTopPosition);
	}

	/**
	 * Returns the topmost block of a tree at the given position The topmost block
	 * is discovered by checking upwards in Y direction until a non-harvestable
	 * block is found and the block position below is returned.
	 */
	private BlockPos findTreeTopPosition(final BlockPos checkedPosition) {
		final BlockPos upPosition = checkedPosition.up();
		final Block upBlock = world.getBlockState(upPosition).getBlock();

		if (canHarvestBlockType(upBlock)) {
			// Block above can be harvested. Check further.

			return findTreeTopPosition(upPosition);
		} else {
			// Block above can not be harvested. Assume this as top of the tree.

			return checkedPosition;
		}
	}

	private void tryHarvestNextQueuedPosition(final int remainingTickCapacity) {
		if (isBlockedByOutputInventory) {
			return;
		}

		if (pendingBlockPositions.isEmpty()) {
			return;
		}

		final BlockPos nextHarvestBlockPosition = pendingBlockPositions.poll();

		if (tryHarvest(nextHarvestBlockPosition)) {
			// When successfully harvested, queue all neighbors of this block.

			queueNeighbors(nextHarvestBlockPosition);
		} else {
			// When trying to harvest an invalid block, proceed with the next queue element.

			if (remainingTickCapacity > 0) {
				tryHarvestNextQueuedPosition(remainingTickCapacity - 1);
			}
		}
	}

	private void queueNeighbors(final BlockPos blockPosition) {
		// Queue all 8 blocks at the same height.

		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				if (x == 0 && z == 0) {
					continue;
				}

				queuePositionIfValid(blockPosition.add(x, 0, z), pendingBlockPositions::addFirst);
			}
		}

		// Queue all 9 blocks above this block at the very front of the deque.

		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				queuePositionIfValid(blockPosition.add(x, 1, z), pendingBlockPositions::addFirst);
			}
		}

		// Queue all 9 blocks beneath this block at the back of the deque (to prioritize
		// the above blocks).

		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				queuePositionIfValid(blockPosition.add(x, -1, z), pendingBlockPositions::addLast);
			}
		}
	}

	/**
	 * Adds the given block position to the deque if the position is inside the
	 * valid working area (including tree scan and cutting size).
	 */
	private void queuePositionIfValid(final BlockPos blockPosition, final Consumer<BlockPos> consumer) {
		final BlockPos centerPosition = getWorkingAreaCenterPos();

		if (Math.abs(blockPosition.getX() - centerPosition.getX()) > TREE_SCAN_RADIUS + TREE_CUTTING_RADIUS) {
			return;
		}

		if (Math.abs(blockPosition.getZ() - centerPosition.getZ()) > TREE_SCAN_RADIUS + TREE_CUTTING_RADIUS) {
			return;
		}

		if (blockPosition.getY() < centerPosition.getY()) {
			return;
		}

		if (!canHarvestBlockType(world.getBlockState(blockPosition).getBlock())) {
			return;
		}

		consumer.accept(blockPosition);
	}

	private boolean canHarvestBlockType(final Block block) {
		return block.isIn(BlockTags.LOGS) || block.isIn(BlockTags.LEAVES);
	}

	/**
	 * Harvests the given block position if possible.
	 *
	 * @return true if the block was successfully harvested and dropped items were
	 *         transferred to the inventory, false otherwise.
	 */
	private boolean tryHarvest(final BlockPos blockPosition) {
		final BlockState blockState = world.getBlockState(blockPosition);
		final BlockEntity blockEntity = world.getBlockEntity(blockPosition);

		if (!canHarvestBlockType(blockState.getBlock())) {
			return false;
		}

		// Decide for dropped items of the target block (may change with each tick due
		// to random loot table).

		final List<ItemStack> dropItemStackList = Block.getDroppedStacks(
				blockState,
				(ServerWorld) world,
				blockPosition,
				blockEntity);

		// Simply break the block if there was no dropped item selected.

		if (dropItemStackList.isEmpty()) {
			world.breakBlock(blockPosition, false);

			createSewageAfterBlockHarvested();

			return true;
		}

		// Otherwise check if the dropped item(s) could be inserted to any output slot
		// and break the block if so.

		boolean couldInsertAllItems = true;

		for (final ItemStack dropItemStack : dropItemStackList) {
			if (!couldInsertToOutputInventory(dropItemStack)) {
				couldInsertAllItems = false;
				break;
			}
		}

		if (couldInsertAllItems) {
			world.breakBlock(blockPosition, false);

			for (final ItemStack dropItemStack : dropItemStackList) {
				insertToOutputInventory(dropItemStack);
			}

			createSewageAfterBlockHarvested();

			return true;
		} else {
			// If we can't insert all items dropped, we enter the blocked state.

			isBlockedByOutputInventory = true;

			// We need to manually sync this block state, because the client never executed
			// this code part that sets isBlockedByOutputInventory.

			syncWithAll();

			return false;
		}
	}

	/**
	 * Creates the defined amount of sewage and adds it to the tank.
	 * <p>
	 * Call this method only when there is enough free space in the sewage tank.
	 */
	private void createSewageAfterBlockHarvested() {
		sewageMbCounter += SEWAGE_MB_PER_BLOCK;

		while (sewageMbCounter >= 250) {

			if (sewageTank.canFit(MarvelousMachinesFluid.SEWAGE.getFluid(), FluidValue.BUCKET_QUARTER)) {
				if (sewageTank.isEmpty()) {
					sewageTank.setFluid(MarvelousMachinesFluid.SEWAGE.getFluid());
				}
				sewageTank.getFluidInstance().addAmount(FluidValue.BUCKET_QUARTER);
			}

			sewageMbCounter -= 250;
		}
	}

	@Override
	public BuiltScreenHandler createScreenHandler(final int syncID, final PlayerEntity playerEntity) {
		BlockEntityScreenHandlerBuilder screenHandlerBuilder = new ScreenHandlerBuilder(getClass().getSimpleName())
				.player(playerEntity.inventory)
				.inventory()
				.hotbar()
				.addInventory()
				.blockEntity(this);

		int outputIndex = 0;
		for (final Integer outputSlot : OUTPUT_SLOTS) {
			screenHandlerBuilder = screenHandlerBuilder.outputSlot(outputSlot, 38 + outputIndex * 20, 45);
			outputIndex++;
		}

		return screenHandlerBuilder
				.outputSlot(BUCKET_OUTPUT_SLOT, 152, 55)
				.fluidSlot(BUCKET_INPUT_SLOT, 152, 35)
				.energySlot(ENERGY_SLOT, 8, 72)
				.sync(sewageTank)
				.syncEnergyValue()
				.addInventory()
				.create(this, syncID);
	}

	/**
	 * Returns the next block position of any log block in the working area or an
	 * empty optional if no log block was found.
	 */
	private Optional<BlockPos> nextLogPosInWorkingArea() {
		final BlockPos centerPosition = getWorkingAreaCenterPos();

		for (int offsetX = -TREE_SCAN_RADIUS; offsetX <= TREE_SCAN_RADIUS; offsetX++) {
			for (int offsetZ = -TREE_SCAN_RADIUS; offsetZ <= TREE_SCAN_RADIUS; offsetZ++) {
				final BlockPos checkedPos = centerPosition.add(offsetX, 0, offsetZ);
				final BlockState blockState = world.getBlockState(checkedPos);

				if (blockState.getBlock().isIn(BlockTags.LOGS)) {
					return Optional.of(checkedPos);
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Returns the center of the working area of this tree cutter. The center is
	 * <code>WORKING_RADIUS + 1</code> blocks away from the entity.
	 */
	private BlockPos getWorkingAreaCenterPos() {
		return pos.offset(getFacing(), TREE_SCAN_RADIUS + 1);
	}

	/**
	 * Returns true if any of the output slots are empty without an item in it.
	 */
	private boolean hasAnyEmptyOutputSlot() {
		for (final Integer outputSlot : OUTPUT_SLOTS) {
			if (inventory.getStack(outputSlot).isEmpty()) {
				return true;
			}
		}

		return false;
	}

	private boolean couldInsertToOutputInventory(final ItemStack stackToInsert) {
		// Note that this way of iterating slots independently does not take into
		// account cases where item stacks to insert could be split to separate "almost
		// full" output slots.

		for (final Integer outputSlot : OUTPUT_SLOTS) {
			final ItemStack inventoryStack = inventory.getStack(outputSlot);

			if (inventoryStack.isEmpty()) {
				return true;
			} else {
				if (ItemUtils.isItemEqual(stackToInsert, inventoryStack, true, false)) {
					final int freeStackSpace = inventoryStack.getMaxCount() - inventoryStack.getCount();

					if (freeStackSpace > 0) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private void insertToOutputInventory(final ItemStack stackToInsert) {
		for (final Integer outputSlot : OUTPUT_SLOTS) {
			final ItemStack inventoryStack = inventory.getStack(outputSlot);

			if (inventoryStack.isEmpty()) {
				inventory.setStack(outputSlot, stackToInsert.copy());
				stackToInsert.setCount(0);
			} else {
				if (ItemUtils.isItemEqual(stackToInsert, inventoryStack, true, false)) {
					final int freeStackSpace = inventoryStack.getMaxCount() - inventoryStack.getCount();

					if (freeStackSpace > 0) {
						final int transferAmount = Math.min(freeStackSpace, stackToInsert.getCount());
						inventoryStack.increment(transferAmount);
						stackToInsert.decrement(transferAmount);
					}
				}
			}
		}
	}
}
