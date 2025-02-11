package ca.fxco.moreculling.mixin.blockstates;

import ca.fxco.moreculling.MoreCulling;
import ca.fxco.moreculling.api.block.MoreBlockCulling;
import ca.fxco.moreculling.api.blockstate.MoreStateCulling;
import ca.fxco.moreculling.utils.CullingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Block.class, priority = 2500)
public class Block_fabricDrawSideMixin implements MoreBlockCulling {
    /**
     * Many mods use Block.shouldDrawSide() directly so its basically required that we override it (using an inject)
     * If your mixin breaks due to this, please use the API if MoreCulling is present
     */
    @Inject(
            method = "shouldRenderFace",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void moreculling$customShouldDrawSide(BlockState thisState, BlockState sideState,
                                                         Direction side, CallbackInfoReturnable<Boolean> cir) {
        if (MoreCulling.CONFIG.useBlockStateCulling && ((MoreStateCulling) thisState).moreculling$canCull()) {
            cir.setReturnValue(CullingUtils.shouldDrawSideCulling(thisState, sideState,
                    EmptyBlockGetter.INSTANCE, BlockPos.ZERO, side, BlockPos.ZERO));
        }
    }
}
