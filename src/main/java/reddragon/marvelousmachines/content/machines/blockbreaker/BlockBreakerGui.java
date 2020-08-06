package reddragon.marvelousmachines.content.machines.blockbreaker;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import reborncore.client.gui.builder.GuiBase;
import reborncore.client.gui.guibuilder.GuiBuilder;
import reborncore.client.screen.builder.BuiltScreenHandler;
import reddragon.marvelousmachines.content.machines.AbstractMachineBlockEntity;

public class BlockBreakerGui extends GuiBase<BuiltScreenHandler> {

	private final BlockBreakerBlockEntity blockEntity;

	public BlockBreakerGui(final int syncID, final PlayerEntity player, final AbstractMachineBlockEntity blockEntity) {
		super(player, blockEntity, blockEntity.createScreenHandler(syncID, player));
		this.blockEntity = (BlockBreakerBlockEntity) blockEntity;
	}

	@Override
	protected void drawBackground(final MatrixStack matrixStack, final float partialTicks, final int mouseX,
			final int mouseY) {
		super.drawBackground(matrixStack, partialTicks, mouseX, mouseY);

		final GuiBase.Layer layer = GuiBase.Layer.BACKGROUND;

		// Battery slot
		drawSlot(matrixStack, 8, 72, layer);

		// Output slot
		drawOutputSlot(matrixStack, 105, 40, layer);
	}

	@Override
	protected void drawForeground(final MatrixStack matrixStack, final int mouseX, final int mouseY) {
		super.drawForeground(matrixStack, mouseX, mouseY);

		final GuiBase.Layer layer = GuiBase.Layer.FOREGROUND;

		builder.drawProgressBar(matrixStack, this, blockEntity.getProgressScaled(100), 100, 76, 40, mouseX, mouseY,
				GuiBuilder.ProgressDirection.LEFT, layer);

		builder.drawMultiEnergyBar(matrixStack, this, 9, 19, (int) blockEntity.getEnergy(),
				(int) blockEntity.getMaxPower(), mouseX, mouseY, 0, layer);

	}
}
