package ca.fxco.moreculling.mixin.models.cullshape;

import ca.fxco.moreculling.api.model.BakedOpacity;
import ca.fxco.moreculling.api.model.CullShapeElement;
import ca.fxco.moreculling.api.model.ExtendedUnbakedModel;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BlockModel.class)
public abstract class BlockModel_fabricCullShapeMixin implements ExtendedUnbakedModel {

    @Shadow
    @Nullable
    private UnbakedModel parent;

    @Shadow @Nullable abstract List<BlockElement> getElements();

    @Inject(
            method = "bake",
            at = @At(
                    value = "RETURN",
                    shift = At.Shift.BEFORE
            )
    )
    private void moreculling$onBake(TextureSlots p_387258_, ModelBaker p_388168_, ModelState settings,
                                    boolean p_111455_, boolean p_387632_, ItemTransforms p_386577_,
                                    CallbackInfoReturnable<BakedModel> cir) {
        BakedModel bakedModel = cir.getReturnValue();
        if (bakedModel == null || !(parent instanceof BlockModel blockModel)) {
            return;
        }
        BakedOpacity bakedOpacity = (BakedOpacity) bakedModel;
        if (!bakedOpacity.moreculling$canSetCullingShape()) {
            return;
        }
        ResourceLocation id = blockModel.parentLocation;
        if (moreculling$getUseModelShape(id) && settings.getRotation() == Transformation.identity()) {
            List<BlockElement> modelElementList = this.getElements();
            if (modelElementList != null && !modelElementList.isEmpty()) {
                VoxelShape voxelShape = Shapes.empty();
                for (BlockElement e : modelElementList) {
                    if (e.rotation == null || e.rotation.angle() == 0) {
                        VoxelShape shape = Block.box(
                                e.from.x, e.from.y, e.from.z, e.to.x, e.to.y, e.to.z
                        );
                        voxelShape = Shapes.or(voxelShape, shape);
                    }
                }
                bakedOpacity.moreculling$setCullingShape(voxelShape);
                return;
            }
        } else {
            List<CullShapeElement> cullShapeElementList = moreculling$getCullShapeElements(id);
            if (cullShapeElementList != null && !cullShapeElementList.isEmpty()) {
                VoxelShape voxelShape = Shapes.empty();
                for (CullShapeElement e : cullShapeElementList) {
                    VoxelShape shape = Block.box(e.from.x, e.from.y, e.from.z, e.to.x, e.to.y, e.to.z);
                    voxelShape = Shapes.or(voxelShape, shape);
                }
                bakedOpacity.moreculling$setCullingShape(voxelShape);
                return;
            }
        }
        bakedOpacity.moreculling$setCullingShape(null);
    }
}
