package reddragon.marvelousmachines.content.machines.planter;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import reborncore.client.gui.builder.GuiBase;
import reddragon.marvelousmachines.content.machines.AbstractMachineBlockEntity;
import reddragon.marvelousmachines.content.machines.AbstractMachineGui;

public class PlanterGui extends AbstractMachineGui<PlanterBlockEntity> {

	public PlanterGui(final int syncID, final PlayerEntity player, final AbstractMachineBlockEntity blockEntity) {
		super(syncID, player, blockEntity);
	}

	@Override
	protected void drawBackground(final MatrixStack matrixStack, final float partialTicks, final int mouseX,
			final int mouseY) {
		super.drawBackground(matrixStack, partialTicks, mouseX, mouseY);

		final GuiBase.Layer layer = GuiBase.Layer.BACKGROUND;

		// Battery slot
		drawSlot(matrixStack, 8, 72, layer);

		// Input slot
		drawSlot(matrixStack, 80, 45, layer);
	}

	@Override
	protected void drawForeground(final MatrixStack matrixStack, final int mouseX, final int mouseY) {
		super.drawForeground(matrixStack, mouseX, mouseY);

		final GuiBase.Layer layer = GuiBase.Layer.FOREGROUND;

		builder.drawMultiEnergyBar(matrixStack, this, 9, 19, (int) blockEntity.getEnergy(),
				(int) blockEntity.getMaxPower(), mouseX, mouseY, 0, layer);

	}
}
