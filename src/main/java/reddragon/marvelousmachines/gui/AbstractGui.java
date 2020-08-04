package reddragon.marvelousmachines.gui;

import java.util.Locale;
import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import reborncore.RebornCore;
import reborncore.api.blockentity.IMachineGuiHandler;
import reborncore.client.screen.BuiltScreenHandlerProvider;
import reborncore.client.screen.builder.BuiltScreenHandler;
import reddragon.marvelousmachines.MarvelousMachinesMod;
import reddragon.marvelousmachines.content.ModMachine;

public final class AbstractGui<T extends BlockEntity> implements IMachineGuiHandler {
	public static <T extends BlockEntity> AbstractGui<T> register(
			final ModMachine machineType,
			final Supplier<Supplier<GuiFactory<T>>> factorySupplierMeme) {
		final Identifier identifier = new Identifier(MarvelousMachinesMod.NAMESPACE,
				machineType.name().toLowerCase(Locale.ROOT));

		final AbstractGui<T> type = new AbstractGui<>(identifier, factorySupplierMeme);

		return type;
	}

	private final Supplier<Supplier<GuiFactory<T>>> guiFactory;
	private final ScreenHandlerType<BuiltScreenHandler> screenHandlerType;

	private AbstractGui(final Identifier identifier, final Supplier<Supplier<GuiFactory<T>>> factorySupplierMeme) {
		this.guiFactory = factorySupplierMeme;
		this.screenHandlerType = ScreenHandlerRegistry.registerExtended(identifier, getScreenHandlerFactory());

		RebornCore.clientOnly(() -> () -> ScreenRegistry.register(screenHandlerType, getGuiFactory()));
	}

	private ScreenHandlerRegistry.ExtendedClientHandlerFactory<BuiltScreenHandler> getScreenHandlerFactory() {
		return (syncId, playerInventory, packetByteBuf) -> {
			final BlockEntity blockEntity = playerInventory.player.world.getBlockEntity(packetByteBuf.readBlockPos());
			final BuiltScreenHandler screenHandler = ((BuiltScreenHandlerProvider) blockEntity)
					.createScreenHandler(syncId, playerInventory.player);

			// Set the screen handler type, not ideal but works lol
			screenHandler.setType(screenHandlerType);

			return screenHandler;
		};
	}

	@Environment(EnvType.CLIENT)
	private GuiFactory<T> getGuiFactory() {
		return guiFactory.get().get();
	}

	@Override
	public void open(final PlayerEntity player, final BlockPos pos, final World world) {
		if (!world.isClient) {
			// This is awful
			player.openHandledScreen(new ExtendedScreenHandlerFactory() {
				@Override
				public void writeScreenOpeningData(final ServerPlayerEntity serverPlayerEntity,
						final PacketByteBuf packetByteBuf) {
					packetByteBuf.writeBlockPos(pos);
				}

				@Override
				public Text getDisplayName() {
					return new LiteralText("What is this for?");
				}

				@Override
				public ScreenHandler createMenu(final int syncId, final PlayerInventory inv,
						final PlayerEntity player) {
					final BlockEntity blockEntity = player.world.getBlockEntity(pos);
					final BuiltScreenHandler screenHandler = ((BuiltScreenHandlerProvider) blockEntity)
							.createScreenHandler(syncId, player);
					screenHandler.setType(screenHandlerType);
					return screenHandler;
				}
			});
		}
	}

	@Environment(EnvType.CLIENT)
	public interface GuiFactory<T extends BlockEntity>
			extends ScreenRegistry.Factory<BuiltScreenHandler, HandledScreen<BuiltScreenHandler>> {
		HandledScreen<?> create(int syncId, PlayerEntity playerEntity, T blockEntity);

		@SuppressWarnings("rawtypes")
		@Override
		default HandledScreen create(final BuiltScreenHandler builtScreenHandler, final PlayerInventory playerInventory,
				final Text text) {
			final PlayerEntity playerEntity = playerInventory.player;
			@SuppressWarnings("unchecked")
			final T blockEntity = (T) builtScreenHandler.getBlockEntity();
			return create(builtScreenHandler.syncId, playerEntity, blockEntity);
		}
	}

}