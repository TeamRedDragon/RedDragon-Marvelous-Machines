package reddragon.marvelousmachines.content;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import reborncore.RebornRegistry;
import reddragon.api.content.BlockHolder;
import reddragon.marvelousmachines.MarvelousMachinesMod;
import reddragon.marvelousmachines.content.machines.AbstractMachineBlock;
import reddragon.marvelousmachines.content.machines.AbstractMachineBlockEntity;
import reddragon.marvelousmachines.content.machines.BlockEntitySupplier;
import reddragon.marvelousmachines.content.machines.BlockSupplier;
import reddragon.marvelousmachines.content.machines.stonebreaker.StoneBreakerBlock;
import reddragon.marvelousmachines.content.machines.stonebreaker.StoneBreakerBlockEntity;
import reddragon.marvelousmachines.content.machines.stonebreaker.StoneBreakerGui;
import reddragon.marvelousmachines.gui.AbstractGui;
import reddragon.marvelousmachines.gui.AbstractGui.GuiFactory;

/**
 * Enumeration of all machine blocks this mod adds to the game.
 * <p>
 * All machines have a corresponding block, block entity and a GUI
 * implementation.
 */
public enum ModMachine implements BlockHolder {
	STONE_BREAKER(StoneBreakerBlock::new, StoneBreakerBlockEntity::new, StoneBreakerGui::new);

	private AbstractMachineBlock block;

	private AbstractGui<?> guiType;

	private BlockEntityType<?> blockEntityType;

	private ModMachine(
			final BlockSupplier blockSupplier,
			final BlockEntitySupplier blockEntitySupplier,
			final GuiFactory<? extends AbstractMachineBlockEntity> guiSupplier) {

		block = blockSupplier.create(this, blockEntitySupplier);

		guiType = AbstractGui.register(this, () -> () -> guiSupplier);
	}

	public void setEntityType(final BlockEntityType<?> newEntityType) {
		if (blockEntityType != null) {
			throw new IllegalStateException("Entity type already set for machine type " + toString());
		}

		blockEntityType = newEntityType;
	}

	public BlockEntityType<?> getEntityType() {
		return blockEntityType;
	}

	@Override
	public Block getBlock() {
		return block;
	}

	public Supplier<? extends BlockEntity> getEntitySupplier() {
		return () -> block.createBlockEntity(null);
	}

	public AbstractGui<?> getGuiType() {
		return guiType;
	}

	public void register() {
		RebornRegistry.registerBlock(
				block,
				new BlockItem(getBlock(), new Item.Settings().group(MarvelousMachinesMod.ITEMGROUP)),
				buildIdentifier());

		final BlockEntityType<?> entityType = BlockEntityType.Builder
				.create(getEntitySupplier(), block)
				.build(null);

		Registry.register(
				Registry.BLOCK_ENTITY_TYPE,
				buildIdentifier(),
				entityType);

		setEntityType(entityType);
	}

	/**
	 * Returns an {@link Identifier} for this machine.
	 */
	public Identifier buildIdentifier() {
		return new Identifier(MarvelousMachinesMod.NAMESPACE, name().toLowerCase(Locale.ROOT));
	}
}
