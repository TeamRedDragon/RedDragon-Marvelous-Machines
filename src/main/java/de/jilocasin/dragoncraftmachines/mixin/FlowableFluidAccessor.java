package de.jilocasin.dragoncraftmachines.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.world.WorldView;

@Mixin(FlowableFluid.class)
public interface FlowableFluidAccessor {
	@Invoker()
	int callGetLevelDecreasePerBlock(WorldView world);
}
