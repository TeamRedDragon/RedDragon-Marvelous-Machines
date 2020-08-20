package reddragon.marvelousmachines.content.machines.blockbreaker;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import reborncore.client.gui.builder.GuiBase;
import reborncore.client.screen.builder.BuiltScreenHandler;
import reborncore.common.util.StringUtils;
import reddragon.marvelousmachines.content.machines.AbstractMachineBlockEntity;

public abstract class BlockBreakerGui extends GuiBase<BuiltScreenHandler> {

	public static final Identifier guiTextureSheet = new Identifier("marvelousmachines", "textures/gui/guielements.png");

	protected final AbstractBlockBreakerBlockEntity blockEntity;

	public BlockBreakerGui(final int syncID, final PlayerEntity player, final AbstractMachineBlockEntity blockEntity) {
		super(player, blockEntity, blockEntity.createScreenHandler(syncID, player));
		this.blockEntity = (AbstractBlockBreakerBlockEntity) blockEntity;
	}

	@Override
	protected void drawBackground(final MatrixStack matrixStack, final float partialTicks, final int mouseX,
			final int mouseY) {
		super.drawBackground(matrixStack, partialTicks, mouseX, mouseY);

		// Battery slot
		drawSlot(matrixStack, 8, 72, GuiBase.Layer.BACKGROUND);

		// Output slot
		drawOutputSlot(matrixStack, 115, 40, GuiBase.Layer.BACKGROUND);
	}

	@Override
	protected void drawForeground(final MatrixStack matrixStack, final int mouseX, final int mouseY) {
		super.drawForeground(matrixStack, mouseX, mouseY);

		builder.drawMultiEnergyBar(matrixStack, this, 9, 19, (int) blockEntity.getEnergy(),
				(int) blockEntity.getMaxPower(), mouseX, mouseY, 0, GuiBase.Layer.FOREGROUND);

	}

	protected void drawDrillHead(final MatrixStack matrixStack, final int x, final int y, final int mouseX, final int mouseY, final GuiBase.Layer layer) {
		drawProgressTexture(matrixStack, x, y, 0, 0, 16, 16, mouseX, mouseY, layer, 20, 8);
	}

	protected void drawLogSawWheel(final MatrixStack matrixStack, final int x, final int y, final int mouseX, final int mouseY, final GuiBase.Layer layer) {
		drawProgressTexture(matrixStack, x, y, 0, 16, 16, 16, mouseX, mouseY, layer, 20, 3);
	}

	@SuppressWarnings("resource")
	private void drawProgressTexture(final MatrixStack matrixStack,
			int x, int y,
			final int textureX, final int textureY,
			final int textureW, final int textureH,
			int mouseX, int mouseY,
			final GuiBase.Layer layer, final int animationFps, final int animationFrames) {
		if (hideGuiElements()) {
			return;
		}

		if (layer == GuiBase.Layer.BACKGROUND) {
			x += getGuiLeft();
			y += getGuiTop();
		}

		int animationIndex = (int) ((getMinecraft().world.getTime() / (20.0 / animationFps)) % animationFrames);

		if (!blockEntity.isActive()) {
			animationIndex = 0;
		}

		getMinecraft().getTextureManager().bindTexture(guiTextureSheet);
		drawTexture(matrixStack, x, y, textureX + textureW * animationIndex, textureY, textureW, textureH);

		if (isPointInRect(x, y, textureW, textureH, mouseX, mouseY)) {
			final int percentage = percentage(100, blockEntity.getProgressScaled(100));
			final List<Text> list = new ArrayList<>();
			list.add(
					new LiteralText(String.valueOf(percentage))
							.formatted(StringUtils.getPercentageColour(percentage))
							.append("%"));
			if (layer == GuiBase.Layer.FOREGROUND) {
				mouseX -= getGuiLeft();
				mouseY -= getGuiTop();
			}
			renderTooltip(matrixStack, list, mouseX, mouseY);
			RenderSystem.disableLighting();
			RenderSystem.color4f(1, 1, 1, 1);
		}
	}

	protected int percentage(final int MaxValue, final int CurrentValue) {
		if (CurrentValue == 0) {
			return 0;

		}
		return (int) ((CurrentValue * 100.0f) / MaxValue);
	}
}
