package com.github.enderhane.colorfulenchant.client.renderer.entity.layers;

import com.github.enderhane.colorfulenchant.client.renderer.CEBufferSourceHelper;
import com.github.enderhane.colorfulenchant.client.renderer.CERenderType;
import com.github.enderhane.colorfulenchant.client.renderer.CERenderTypeBuffer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.model.ElytraModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class CEElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer<T, M> {

    private final ElytraModel<T> elytraModel = new ElytraModel<>();

    public CEElytraLayer(IEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer bufferSourceIn, int lightmap, T entity, float dum1, float dum2, float dum3, float dum4, float dum5, float dum6) {
        ItemStack itemStack = entity.getItemBySlot(EquipmentSlotType.CHEST);
        if (shouldRender(itemStack, entity)) {
            CERenderTypeBuffer appointedBufferSource = CEBufferSourceHelper.bufferSource();
            ResourceLocation resourceLocation;
            if (entity instanceof AbstractClientPlayerEntity) {
                AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)entity;
                if (player.isElytraLoaded() && player.getElytraTextureLocation() != null) {
                    resourceLocation = player.getElytraTextureLocation();
                } else if (player.isCapeLoaded() && player.getCloakTextureLocation() != null && player.isModelPartShown(PlayerModelPart.CAPE)) {
                    resourceLocation = player.getCloakTextureLocation();
                } else {
                    resourceLocation = getElytraTexture(itemStack, entity);
                }
            } else {
                resourceLocation = getElytraTexture(itemStack, entity);
            }

            matrixStack.pushPose();
            matrixStack.translate(0.0D, 0.0D, 0.125D);
            getParentModel().copyPropertiesTo(elytraModel);
            elytraModel.setupAnim(entity, dum1, dum2, dum4, dum5, dum6);
            IVertexBuilder elytraBuilder = appointedBufferSource.getBuffer(RenderType.armorCutoutNoCull(resourceLocation));
            elytraModel.renderToBuffer(matrixStack, elytraBuilder, lightmap, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            appointedBufferSource.endBatch(RenderType.armorCutoutNoCull(resourceLocation));
            if(itemStack.hasFoil()){
                IVertexBuilder foilBuilder = appointedBufferSource.getBuffer(CERenderType.coloredArmorEntityGlint());
                elytraModel.renderToBuffer(matrixStack, foilBuilder, lightmap, OverlayTexture.NO_OVERLAY, 1.0F, 0.5F, 0.5F, 1.0F);
                appointedBufferSource.endBatch(CERenderType.coloredArmorEntityGlint());
            }
            matrixStack.popPose();
        }
    }

}
