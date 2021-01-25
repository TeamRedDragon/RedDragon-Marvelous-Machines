package reddragon.marvelousmachines.gui;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import reborncore.api.IListInfoProvider;
import reborncore.common.BaseBlockEntityProvider;
import reborncore.common.powerSystem.PowerSystem;
import reborncore.common.util.StringUtils;
import reddragon.marvelousmachines.MarvelousMachinesMod;
import team.reborn.energy.Energy;
import team.reborn.energy.EnergyHolder;
import team.reborn.energy.EnergySide;
import techreborn.events.StackToolTipHandler;
import techreborn.init.TRContent;
import techreborn.items.UpgradeItem;
import techreborn.utils.ToolTipAssistUtils;

/**
 * Additional tooltip handler for TechReborn-like items.
 * <p>
 * This class is mostly copy pasted from {@link StackToolTipHandler} because the
 * original handler performs hardcoded checks for the "techreborn" identifier
 * and thus can not be used for our mod items.
 */
public class ToolTipHandler implements ItemTooltipCallback {

	private static final Formatting INSTRUCT_COLOR = Formatting.BLUE;

	private static final Formatting INFO_COLOR = Formatting.GOLD;

	public static final Map<Item, Boolean> IS_MOD_RELATED_ITEM = Maps.newHashMap();

	public static void setup() {
		ItemTooltipCallback.EVENT.register(new ToolTipHandler());
	}

	@Override
	public void getTooltip(final ItemStack stack, final TooltipContext tooltipContext, final List<Text> components) {
		final Item item = stack.getItem();

		if (!IS_MOD_RELATED_ITEM.computeIfAbsent(item, ToolTipHandler::isModRelatedItem)) {
			return;
		}

		final Block block = Block.getBlockFromItem(item);

		if (block instanceof BaseBlockEntityProvider) {
			addInfo(item.getTranslationKey(), components);
		}

		if (item instanceof UpgradeItem) {
			final UpgradeItem upgrade = (UpgradeItem) item;

			addInfo(item.getTranslationKey(), components, false);
			components.addAll(
					ToolTipAssistUtils.getUpgradeStats(TRContent.Upgrades.valueOf(upgrade.name.toUpperCase()), stack.getCount(), Screen.hasShiftDown()));
		}

		// Other section
		if (item instanceof IListInfoProvider) {
			((IListInfoProvider) item).addInfo(components, false, false);
		} else if (stack.getItem() instanceof EnergyHolder) {
			final LiteralText line1 = new LiteralText(PowerSystem.getLocalizedPowerNoSuffix(Energy.of(stack).getEnergy()));
			line1.append("/");
			line1.append(PowerSystem.getLocalizedPowerNoSuffix(Energy.of(stack).getMaxStored()));
			line1.append(" ");
			line1.append(PowerSystem.getDisplayPower().abbreviation);
			line1.formatted(Formatting.GOLD);

			components.add(1, line1);

			if (Screen.hasShiftDown()) {
				final int percentage = percentage(Energy.of(stack).getMaxStored(), Energy.of(stack).getEnergy());
				final Formatting color = StringUtils.getPercentageColour(percentage);
				components.add(2, new LiteralText(color + "" + percentage + "%" + Formatting.GRAY + " Charged"));
				// TODO: show both input and output rates
				components.add(3, new LiteralText(Formatting.GRAY + "I/O Rate: " + Formatting.GOLD
						+ PowerSystem.getLocalizedPowerNoSuffix(((EnergyHolder) item).getMaxInput(EnergySide.UNKNOWN))));
			}
		} else {
			try {
				if ((block instanceof BlockEntityProvider) && isModRelatedBlock(block)) {
					@SuppressWarnings("resource")
					final BlockEntity blockEntity = ((BlockEntityProvider) block).createBlockEntity(MinecraftClient.getInstance().world);
					boolean hasData = false;
					if (stack.hasTag() && stack.getTag().contains("blockEntity_data")) {
						final CompoundTag blockEntityData = stack.getTag().getCompound("blockEntity_data");
						blockEntity.fromTag(blockEntity.getCachedState(), blockEntityData);
						hasData = true;
						components.add(new LiteralText("Block data contained").formatted(Formatting.DARK_GREEN));
					}
					if (blockEntity instanceof IListInfoProvider) {
						((IListInfoProvider) blockEntity).addInfo(components, false, hasData);
					}
				}
			} catch (final NullPointerException e) {
				MarvelousMachinesMod.LOG.error("Failed to load info for " + stack.getName());
			}
		}
	}

	private static boolean isModRelatedItem(final Item item) {
		return Registry.ITEM.getId(item).getNamespace().equals(MarvelousMachinesMod.NAMESPACE);
	}

	private static boolean isModRelatedBlock(final Block block) {
		return Registry.BLOCK.getId(block).getNamespace().equals(MarvelousMachinesMod.NAMESPACE);
	}

	private static int percentage(final double MaxValue, final double CurrentValue) {
		if (CurrentValue == 0) {
			return 0;
		}

		return (int) ((CurrentValue * 100.0f) / MaxValue);
	}

	public static void addInfo(final String inKey, final List<Text> list) {
		addInfo(inKey, list, true);
	}

	public static void addInfo(final String inKey, final List<Text> list, final boolean hidden) {
		final String key = ("marvelousmachines.tooltip." + inKey);

		if (I18n.hasTranslation(key)) {
			if (!hidden || Screen.hasShiftDown()) {
				final String info = I18n.translate(key);
				final String[] infoLines = info.split("\\r?\\n");

				for (final String infoLine : infoLines) {
					list.add(new LiteralText(INFO_COLOR + infoLine));
				}
			} else {
				list.add(new LiteralText(INSTRUCT_COLOR + "Hold shift for info"));
			}
		}
	}
}
