package de.jilocasin.dragoncraftmachines.content.machines.stonebreaker;

import java.util.Arrays;
import java.util.List;

import de.jilocasin.dragoncraftmachines.content.DragoncraftMachine;
import de.jilocasin.dragoncraftmachines.content.machines.TickingOperationMachineBlockEntity;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import reborncore.client.screen.builder.BuiltScreenHandler;
import reborncore.client.screen.builder.ScreenHandlerBuilder;
import reborncore.common.util.ItemUtils;
import reborncore.common.util.RebornInventory;
import team.reborn.energy.EnergyTier;

public class StoneBreakerBlockEntity extends TickingOperationMachineBlockEntity {

	public static final int OUTPUT_SLOT = 0;
	public static final int ENERGY_SLOT = 1;

	private static final double ENERGY_PER_OPERATION = 200;
	private static final int TICKS_PER_OPERATION = 100;

	private static final List<Block> ALLOWED_BLOCKS = Arrays.asList(
			Blocks.COBBLESTONE,
			Blocks.STONE);

	private static final List<Identifier> ALLOWED_TAGS = Arrays.asList(
			new Identifier("minecraft", "logs"));

	private final RebornInventory<StoneBreakerBlockEntity> inventory;

	/**
	 * The randomly selected item stack the block in front would drop in the current
	 * tick.
	 */
	private ItemStack currentTickDrop;

	public StoneBreakerBlockEntity(final DragoncraftMachine machineType) {
		super(machineType);
		inventory = new RebornInventory<>(4, getClass().getSimpleName(), 64, this);
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
	protected int getTicksPerOperation() {
		return TICKS_PER_OPERATION;
	}

	@Override
	protected double getEnergyPerOperation() {
		return ENERGY_PER_OPERATION;
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
	public void tick() {
		if (!world.isClient) {
			final BlockPos targetBlockPos = getBlockPosInFront();
			final BlockState targetBlockState = world.getBlockState(getBlockPosInFront());
			final BlockEntity targetBlockEntity = world.getBlockEntity(targetBlockPos);

			final List<ItemStack> dropList = Block.getDroppedStacks(
					targetBlockState,
					(ServerWorld) world,
					targetBlockPos,
					targetBlockEntity);

			if (!dropList.isEmpty()) {
				currentTickDrop = dropList.get(0);
			} else {
				currentTickDrop = ItemStack.EMPTY;
			}
		}

		super.tick();
	}

	private boolean isBlockAllowed(final Block block) {
		if (ALLOWED_BLOCKS.contains(block)) {
			return true;
		}

		for (final Identifier identifier : ALLOWED_TAGS) {
			if (block.isIn(TagRegistry.block(identifier))) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected boolean couldPerformOperation() {
		final BlockState targetBlockState = world.getBlockState(getBlockPosInFront());

		if (!isBlockAllowed(targetBlockState.getBlock())) {
			return false;
		}

		return true;
	}

	@Override
	protected void performOperation() {
		final BlockPos targetBlockPos = getBlockPosInFront();

		if (!currentTickDrop.isEmpty() && couldInsertToOutputInventory(currentTickDrop)) {
			insertToOutputInventory(currentTickDrop);
			world.breakBlock(targetBlockPos, false);
		} else {
			world.breakBlock(targetBlockPos, true);
		}
	}

	@Override
	public BuiltScreenHandler createScreenHandler(final int syncID, final PlayerEntity playerEntity) {
		return new ScreenHandlerBuilder(getClass().getSimpleName()).player(playerEntity.inventory)
				.inventory()
				.hotbar()
				.addInventory()
				.blockEntity(this)
				.outputSlot(OUTPUT_SLOT, 105, 40)
				.energySlot(ENERGY_SLOT, 8, 72)
				.syncEnergyValue()
				.addInventory()
				.create(this, syncID);
	}

	private BlockPos getBlockPosInFront() {
		return pos.offset(getFacing());
	}

	private boolean couldInsertToOutputInventory(final ItemStack stackToInsert) {
		final ItemStack inventoryStack = inventory.getStack(OUTPUT_SLOT);

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

		return false;
	}

	private void insertToOutputInventory(final ItemStack stackToInsert) {
		final ItemStack inventoryStack = inventory.getStack(OUTPUT_SLOT);

		if (inventoryStack.isEmpty()) {
			inventory.setStack(OUTPUT_SLOT, stackToInsert.copy());
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
