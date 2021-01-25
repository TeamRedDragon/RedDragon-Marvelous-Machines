package reddragon.marvelousmachines.content.machines;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import reborncore.api.blockentity.IMachineGuiHandler;
import reborncore.common.blocks.BlockMachineBase;
import reddragon.marvelousmachines.content.MarvelousMachinesMachine;

/**
 * Single and common machine block that does not provide any custom properties.
 * <p>
 * The orientable block is initialized as {@link Material.METAL} with a strength
 * of 2.
 */
public class GenericMachineBlock extends BlockMachineBase {

	private final MarvelousMachinesMachine machineType;

	private final BlockEntitySupplier blockEntitySupplier;

	public GenericMachineBlock(final MarvelousMachinesMachine machineType, final BlockEntitySupplier blockEntitySupplier) {
		super(FabricBlockSettings.of(Material.METAL).strength(2F, 2F));

		setDefaultState(stateManager.getDefaultState().with(FACING, Direction.NORTH).with(ACTIVE, false));

		this.machineType = machineType;
		this.blockEntitySupplier = blockEntitySupplier;
	}

	@Override
	public BlockState getPlacementState(final ItemPlacementContext ctx) {
		return getDefaultState().with(FACING, ctx.getPlayerFacing());
	}

	@Override
	public IMachineGuiHandler getGui() {
		return getMachineType().getGuiType();
	}

	protected MarvelousMachinesMachine getMachineType() {
		return machineType;
	}

	@Override
	public BlockEntity createBlockEntity(final BlockView blockView) {
		return blockEntitySupplier.create(machineType);
	}
}