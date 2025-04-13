package live.hisui.backpacks.compat.curios;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import live.hisui.backpacks.Backpacks;
import live.hisui.backpacks.item.BackpackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class BackpackCurioRenderer implements ICurioRenderer {
    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack,
                                                                          RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer,
                                                                          int light, float limbSwing, float limbSwingAmount, float partialTicks,
                                                                          float ageInTicks, float netHeadYaw, float headPitch) {
        if(stack.getItem() instanceof BackpackItem){
            matrixStack.pushPose();

            if(renderLayerParent.getModel() instanceof HumanoidModel<?> parentModel){
                parentModel.body.translateAndRotate(matrixStack);
                matrixStack.translate(0.0f, 0.2f, 0.275f);
                matrixStack.mulPose(Axis.XP.rotationDegrees(180.0f));
                matrixStack.scale(0.8f,0.8f,0.8f);

                Minecraft.getInstance().getItemRenderer().renderStatic(
                        stack,
                        ItemDisplayContext.FIXED,
                        light,
                        OverlayTexture.NO_OVERLAY,
                        matrixStack,
                        renderTypeBuffer,
                        slotContext.entity().level(),
                        slotContext.entity().getId()
                );
            }

            matrixStack.popPose();
        }
    }
}
