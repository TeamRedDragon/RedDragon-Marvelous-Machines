package reddragon.marvelousmachines.content.machines.blockbreaker;

import java.util.Arrays;
import java.util.List;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import reddragon.marvelousmachines.content.MarvelousMachinesMachine;

public class LogBreakerBlockEntity extends BlockBreakerBlockEntity {
	private static final double ENERGY_PER_OPERATION = 50;
	private static final int TICKS_PER_OPERATION = 20;

	private static final List<Identifier> ALLOWED_TAGS = Arrays.asList(
			new Identifier("minecraft", "logs"));

	public LogBreakerBlockEntity(final MarvelousMachinesMachine machineType) {
		super(machineType);
	}

	@Override
	protected boolean isBlockAllowed(final Block block) {
		for (final Identifier identifier : ALLOWED_TAGS) {
			if (block.isIn(TagRegistry.block(identifier))) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected int getTicksPerOperation() {
		return TICKS_PER_OPERATION;
	}

	@Override
	protected double getEnergyPerOperation() {
		return ENERGY_PER_OPERATION;
	}
}
