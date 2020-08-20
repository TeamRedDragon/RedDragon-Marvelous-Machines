package reddragon.marvelousmachines.content.machines.blockbreaker;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import reborncore.client.screen.builder.BuiltScreenHandler;
import reborncore.client.screen.builder.ScreenHandlerBuilder;
import reborncore.common.util.ItemUtils;
import reborncore.common.util.RebornInventory;
import reddragon.marvelousmachines.content.MarvelousMachinesMachine;
import reddragon.marvelousmachines.content.machines.TickingOperationMachineBlockEntity;
import team.reborn.energy.EnergyTier;

public abstract class AbstractBlockBreakerBlockEntity extends TickingOperationMachineBlockEntity {

	public static final int OUTPUT_SLOT = 0;
	public static final int ENERGY_SLOT = 1;

	private final RebornInventory<AbstractBlockBreakerBlockEntity> inventory;

	/**
	 * The randomly selected item stack the block in front would drop in the current
	 * tick.
	 */
	private ItemStack currentTickDrop;

	public AbstractBlockBreakerBlockEntity(final MarvelousMachinesMachine machineType) {
		super(machineType);
		inventory = new RebornInventory<>(2, getClass().getSimpleName(), 64, this);
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

	protected abstract boolean isBlockAllowed(final Block block);

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
				.outputSlot(OUTPUT_SLOT, 115, 40)
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
