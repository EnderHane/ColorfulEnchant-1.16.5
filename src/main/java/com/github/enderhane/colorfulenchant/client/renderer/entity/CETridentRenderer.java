package com.github.enderhane.colorfulenchant.client.renderer.entity;

import com.github.enderhane.colorfulenchant.client.renderer.CEBufferSourceHelper;
import com.github.enderhane.colorfulenchant.client.renderer.CERenderType;
import com.github.enderhane.colorfulenchant.client.renderer.CERenderTypeBuffer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.TridentRenderer;
import net.minecraft.client.renderer.entity.model.TridentModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class CETridentRenderer extends EntityRenderer<TridentEntity> {

    private final TridentModel model = new TridentModel();

    public CETridentRenderer(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    public void render(TridentEntity trident, float roll, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer bufferSourceIn, int lightmap) {
        CERenderTypeBuffer appointedBufferSource = CEBufferSourceHelper.bufferSource();
        matrixStack.pushPose();
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTick, trident.yRotO, trident.yRot) - 90.0F));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTick, trident.xRotO, trident.xRot) + 90.0F));
        IVertexBuilder tridentBuilder = appointedBufferSource.getBuffer(model.renderType(getTextureLocation(trident)));
        model.renderToBuffer(matrixStack, tridentBuilder, lightmap, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        appointedBufferSource.endBatch(model.renderType(getTextureLocation(trident)));
        if (trident.isFoil()) {
            IVertexBuilder foilBuilder = appointedBufferSource.getBuffer(CERenderType.coloredEntityGlintDirect());
            model.renderToBuffer(matrixStack, foilBuilder, lightmap, OverlayTexture.NO_OVERLAY, 1.0F, 0.5F, 0.5F, 1.0F);
            appointedBufferSource.endBatch(CERenderType.coloredEntityGlintDirect());
        }
        matrixStack.popPose();
        super.render(trident, roll, partialTick, matrixStack, bufferSourceIn, lightmap);
    }

    @Override
    public ResourceLocation getTextureLocation(TridentEntity trident) {
        return TridentRenderer.TRIDENT_LOCATION;
    }
}
