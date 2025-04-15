package live.hisui.backpacks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import live.hisui.backpacks.item.BackpackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BackpackRenderLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
    public BackpackRenderLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource bufferSource, int packedLight, T livingEntity,
                       float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);

        if(stack.getItem() instanceof BackpackItem){
            matrixStack.pushPose();

                getParentModel().body.translateAndRotate(matrixStack);
                matrixStack.translate(0.0f, 0.2f, 0.275f);
                matrixStack.mulPose(Axis.XP.rotationDegrees(180.0f));
                matrixStack.scale(0.8f,0.8f,0.8f);

                Minecraft.getInstance().getItemRenderer().renderStatic(
                        stack,
                        ItemDisplayContext.FIXED,
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        matrixStack,
                        bufferSource,
                        livingEntity.level(),
                        livingEntity.getId()
                );
            matrixStack.popPose();
        }
    }
}
