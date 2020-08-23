package reddragon.marvelousmachines.content.machines;

import net.minecraft.entity.player.PlayerEntity;
import reborncore.client.gui.builder.GuiBase;
import reborncore.client.screen.builder.BuiltScreenHandler;

public abstract class AbstractMachineGui<T extends AbstractMachineBlockEntity> extends GuiBase<BuiltScreenHandler> {

	protected final T blockEntity;

	@SuppressWarnings("unchecked")
	public AbstractMachineGui(final int syncID, final PlayerEntity player, final AbstractMachineBlockEntity blockEntity) {
		super(player, blockEntity, blockEntity.createScreenHandler(syncID, player));
		this.blockEntity = (T) blockEntity;
	}

}
