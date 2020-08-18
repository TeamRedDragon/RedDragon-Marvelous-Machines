package reddragon.marvelousmachines.content.machines.treecutter;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import reborncore.client.gui.builder.GuiBase;
import reborncore.client.screen.builder.BuiltScreenHandler;
import reddragon.marvelousmachines.content.machines.AbstractMachineBlockEntity;

public class TreeCutterGui extends GuiBase<BuiltScreenHandler> {

	private final TreeCutterBlockEntity blockEntity;

	public TreeCutterGui(final int syncID, final PlayerEntity player, final AbstractMachineBlockEntity blockEntity) {
		super(player, blockEntity, blockEntity.createScreenHandler(syncID, player));
		this.blockEntity = (TreeCutterBlockEntity) blockEntity;
	}

	@Override
	protected void drawBackground(final MatrixStack matrixStack, final float partialTicks, final int mouseX,
			final int mouseY) {
		super.drawBackground(matrixStack, partialTicks, mouseX, mouseY);

		final GuiBase.Layer layer = GuiBase.Layer.BACKGROUND;

		// Battery slot
		drawSlot(matrixStack, 8, 72, layer);

		// Output slots
		drawSlot(matrixStack, 38, 45, layer);
		drawSlot(matrixStack, 58, 45, layer);
		drawSlot(matrixStack, 78, 45, layer);
		drawSlot(matrixStack, 98, 45, layer);

		drawSlot(matrixStack, 152, 35, layer);
		drawSlot(matrixStack, 152, 55, layer);

		drawOutputSlotBar(matrixStack, 39, 44, 4, layer);
	}

	@Override
	protected void drawForeground(final MatrixStack matrixStack, final int mouseX, final int mouseY) {
		super.drawForeground(matrixStack, mouseX, mouseY);

		final GuiBase.Layer layer = GuiBase.Layer.FOREGROUND;

		builder.drawTank(matrixStack, this, 127, 25, mouseX, mouseY, blockEntity.getTank().getFluidInstance(), blockEntity.getTank().getCapacity(),
				blockEntity.getTank().isEmpty(), layer);

		builder.drawMultiEnergyBar(matrixStack, this, 9, 19, (int) blockEntity.getEnergy(),
				(int) blockEntity.getMaxPower(), mouseX, mouseY, 0, layer);

	}
}
