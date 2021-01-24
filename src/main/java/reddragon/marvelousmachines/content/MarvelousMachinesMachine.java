package reddragon.marvelousmachines.content;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import reborncore.RebornRegistry;
import reddragon.api.content.BlockHolder;
import reddragon.marvelousmachines.MarvelousMachinesMod;
import reddragon.marvelousmachines.content.machines.AbstractMachineBlockEntity;
import reddragon.marvelousmachines.content.machines.BlockEntitySupplier;
import reddragon.marvelousmachines.content.machines.BlockSupplier;
import reddragon.marvelousmachines.content.machines.GenericMachineBlock;
import reddragon.marvelousmachines.content.machines.blockbreaker.all.ArbitraryBlockBreakerBlockEntity;
import reddragon.marvelousmachines.content.machines.blockbreaker.all.ArbitraryBlockBreakerGui;
import reddragon.marvelousmachines.content.machines.blockbreaker.log.LogBreakerBlockEntity;
import reddragon.marvelousmachines.content.machines.blockbreaker.log.LogBreakerGui;
import reddragon.marvelousmachines.content.machines.blockbreaker.stone.StoneBreakerBlockEntity;
import reddragon.marvelousmachines.content.machines.blockbreaker.stone.StoneBreakerGui;
import reddragon.marvelousmachines.content.machines.planter.PlanterBlockEntity;
import reddragon.marvelousmachines.content.machines.planter.PlanterGui;
import reddragon.marvelousmachines.content.machines.treecutter.TreeCutterBlockEntity;
import reddragon.marvelousmachines.content.machines.treecutter.TreeCutterGui;
import reddragon.marvelousmachines.gui.AbstractGui;
import reddragon.marvelousmachines.gui.AbstractGui.GuiFactory;

/**
 * Enumeration of all machine blocks this mod adds to the game.
 * <p>
 * All machines have a corresponding block, block entity and a GUI
 * implementation.
 */
public enum MarvelousMachinesMachine implements BlockHolder {
	STONE_BREAKER(GenericMachineBlock::new, StoneBreakerBlockEntity::new, () -> () -> StoneBreakerGui::new),
	LOG_BREAKER(GenericMachineBlock::new, LogBreakerBlockEntity::new, () -> () -> LogBreakerGui::new),
	ARBITRARY_BLOCK_BREAKER(GenericMachineBlock::new, ArbitraryBlockBreakerBlockEntity::new, () -> () -> ArbitraryBlockBreakerGui::new),
	TREE_CUTTER(GenericMachineBlock::new, TreeCutterBlockEntity::new, () -> () -> TreeCutterGui::new),
	PLANTER(GenericMachineBlock::new, PlanterBlockEntity::new, () -> () -> PlanterGui::new);

	private GenericMachineBlock block;

	private AbstractGui<?> guiType;

	private BlockEntityType<?> blockEntityType;

	private MarvelousMachinesMachine(
			final BlockSupplier blockSupplier,
			final BlockEntitySupplier blockEntitySupplier,
			final Supplier<Supplier<GuiFactory<? extends AbstractMachineBlockEntity>>> guiSupplier) {

		block = blockSupplier.create(this, blockEntitySupplier);

		guiType = AbstractGui.register(this, () -> () -> guiSupplier.get().get());
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
				getBlock(),
				new Item.Settings().group(MarvelousMachinesMod.ITEMGROUP),
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
