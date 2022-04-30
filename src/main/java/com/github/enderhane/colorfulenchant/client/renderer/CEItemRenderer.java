package com.github.enderhane.colorfulenchant.client.renderer;

import com.github.enderhane.colorfulenchant.ColorfulEnchant;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.MatrixApplyingVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * 继承 {@link net.minecraft.client.renderer.ItemRenderer}
 */
@OnlyIn(Dist.CLIENT)
public class CEItemRenderer extends ItemRenderer {

    private final BufferBuilder selfBuilder = new BufferBuilder(256);

    public static final ResourceLocation CUSTOM_GLINT_LOCATION = new ResourceLocation(ColorfulEnchant.MOD_ID, "textures/misc/custom_glint.png");
    public float blitOffset;
    private final ItemModelMesher itemModelShaper;
    private final TextureManager textureManager;
    private final ItemColors itemColors;
    /**
     * 备用 ItemRenderer
     */
    private final ItemRenderer originalItemRenderer;

    /**
     * @param originalItemRenderer 应当传入原有的 ItemRenderer。 将作为一个备用渲染器
     * @param textureManager 应当传入 Minecraft 中的单例 TextureManager
     * @param modelManager 应当传入 Minecraft 中的单例 ModelManager
     * @param itemColors 应当传入 Minecraft 中的单例 ItemColors
     */
    public CEItemRenderer(ItemRenderer originalItemRenderer, TextureManager textureManager, ModelManager modelManager, ItemColors itemColors) {
        super(textureManager, modelManager, itemColors);
        this.itemModelShaper = originalItemRenderer.getItemModelShaper();
        this.textureManager = textureManager;
        this.itemColors = itemColors;
        this.originalItemRenderer = originalItemRenderer;
    }

    /**
     * 覆写的核心渲染方法
     */
    @Override
    public void render(
        ItemStack itemStack,
        ItemCameraTransforms.TransformType transformType,
        boolean leftHandHackery,
        MatrixStack matrixStack,
        IRenderTypeBuffer renderTypeBuffer,
        int lightmapCoord,
        int overlayCoord,
        IBakedModel bakedModel) {

        CERenderTypeBuffer appointedBuffer = CERenderUtil.ITEM_RENDER_BUFFER.itemBufferSource();
        if (!itemStack.isEmpty()) {
            matrixStack.pushPose();
            boolean flag = transformType == ItemCameraTransforms.TransformType.GUI ||
                transformType == ItemCameraTransforms.TransformType.GROUND ||
                transformType == ItemCameraTransforms.TransformType.FIXED;
            if (itemStack.getItem() == Items.TRIDENT && flag) {
                bakedModel = itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
            }
            bakedModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(matrixStack, bakedModel, transformType, leftHandHackery);
            matrixStack.translate(-0.5D, -0.5D, -0.5D);
            if (!bakedModel.isCustomRenderer() && (itemStack.getItem() != Items.TRIDENT || flag)) {
                boolean flag1;
                if (transformType != ItemCameraTransforms.TransformType.GUI && !transformType.firstPerson() && itemStack.getItem() instanceof BlockItem) {
                    Block block = ((BlockItem)itemStack.getItem()).getBlock();
                    flag1 = !(block instanceof BreakableBlock) && !(block instanceof StainedGlassPaneBlock);
                } else {
                    flag1 = true;
                }
                if (bakedModel.isLayered()) {
                    net.minecraftforge.client.ForgeHooksClient.drawItemLayered(this, bakedModel, itemStack, matrixStack, renderTypeBuffer, lightmapCoord, overlayCoord, flag1);
                }
                else {
                    RenderType renderType = RenderTypeLookup.getRenderType(itemStack, flag1);
                    IVertexBuilder itemBuilder = renderTypeBuffer.getBuffer(renderType);
                            //getColoredFoilBufferDirect(renderTypeBuffer, renderType, true, itemStack.hasFoil());
                    renderModelLists(bakedModel, itemStack, lightmapCoord, overlayCoord, matrixStack, itemBuilder);
                    if (itemStack.hasFoil()){
                        renderFoil(bakedModel, itemStack, matrixStack, transformType, renderTypeBuffer, lightmapCoord, overlayCoord, renderType);
                    }
                }
            } else {
                itemStack.getItem().getItemStackTileEntityRenderer().renderByItem(itemStack, transformType, matrixStack, renderTypeBuffer, lightmapCoord, overlayCoord);
            }
            matrixStack.popPose();
        }
        ((IRenderTypeBuffer.Impl) renderTypeBuffer).endBatch();
    }

    private void renderFoil(
        IBakedModel bakedModel,
        ItemStack itemStack,
        MatrixStack matrixStack,
        ItemCameraTransforms.TransformType transformType,
        IRenderTypeBuffer renderTypeBuffer,
        int lightmapCoord,
        int overlayCoord,
        RenderType renderType){

        IVertexBuilder foilBuilder;
        if (itemStack.getItem()==Items.COMPASS) {
            matrixStack.pushPose();
            MatrixStack.Entry matrixStack$entry = matrixStack.last();
            if (transformType == ItemCameraTransforms.TransformType.GUI) {
                matrixStack$entry.pose().multiply(0.5F);
            } else if (transformType.firstPerson()) {
                matrixStack$entry.pose().multiply(0.75F);
            }
            matrixStack.popPose();
            foilBuilder = new MatrixApplyingVertexBuilder(renderTypeBuffer.getBuffer(CERenderType.coloredGlint()), matrixStack$entry.pose(), matrixStack$entry.normal());
        } else if(Minecraft.useShaderTransparency() && renderType == Atlases.translucentItemSheet()){
            foilBuilder = renderTypeBuffer.getBuffer(CERenderType.coloredGlintTranslucent());
        } else {
            foilBuilder = renderTypeBuffer.getBuffer(CERenderType.coloredGlint());
        }
        renderModelWithColor(matrixStack, bakedModel, foilBuilder, 0xffff9999, lightmapCoord, overlayCoord);
    }

    private void renderModelWithColor(MatrixStack matrixStack, IBakedModel model, IVertexBuilder vertexBuilder, int color, int lightmapCoord, int overlayCoord){
        Random random = new Random();
        for (Direction direction: Direction.values()){
            random.setSeed(42L);
            List<BakedQuad> quads = model.getQuads(null, direction, random, null);
            renderQuadWithColor(matrixStack, vertexBuilder, quads, color, lightmapCoord, overlayCoord);
        }
        random.setSeed(42L);
        List<BakedQuad> quads = model.getQuads(null, null, random, null);
        renderQuadWithColor(matrixStack, vertexBuilder, quads, color, lightmapCoord, overlayCoord);
    }

    private void renderQuadWithColor(MatrixStack matrixStack, IVertexBuilder vertexBuilder, List<BakedQuad> quads, int color, int lightmapCoord, int overlayCoord){
        MatrixStack.Entry entry = matrixStack.last();
        float alpha = (float) ((color >> 24) & 0xff) / 255.f;
        float red = (float) ((color >> 16) & 0xff) / 255.f;
        float green = (float) ((color >> 8) & 0xff) / 255.f;
        float blue = (float) (color & 0xff) / 255.f;
        for (BakedQuad quad: quads){
            vertexBuilder.addVertexData(entry, quad, red, green, blue ,1.f, lightmapCoord, overlayCoord, true);
        }
    }

    @Override
    public void renderModelLists(IBakedModel bakedModel, ItemStack itemStack, int lightmapCoord, int overlayCoord, MatrixStack matrixStack, IVertexBuilder vertexBuilder){
        Random random = new Random();
        for(Direction direction : Direction.values()) {
            random.setSeed(42L);
            renderQuadList(matrixStack, vertexBuilder, bakedModel.getQuads(null, direction, random), itemStack, lightmapCoord, overlayCoord);
        }
        random.setSeed(42L);
        renderQuadList(matrixStack, vertexBuilder, bakedModel.getQuads(null, null, random), itemStack, lightmapCoord, overlayCoord);
    }

    @Override
    public void renderQuadList(MatrixStack matrixStack, IVertexBuilder vertexBuilder, List<BakedQuad> bakedQuads, ItemStack itemStack, int lightmapCoord, int overlayCoord){
        boolean flag = !itemStack.isEmpty();
        MatrixStack.Entry matrixstack$entry = matrixStack.last();
        for(BakedQuad bakedquad : bakedQuads) {
            int i = -1;
            if (flag && bakedquad.isTinted()) {
                i = this.itemColors.getColor(itemStack, bakedquad.getTintIndex());
            }
            float red = (float)(i >> 16 & 255) / 255.0F;
            float green = (float)(i >> 8 & 255) / 255.0F;
            float blue = (float)(i & 255) / 255.0F;
            vertexBuilder.addVertexData(matrixstack$entry, bakedquad, red, green, blue, 1.0f ,lightmapCoord, overlayCoord, true);
        }

    }

    public static IVertexBuilder getColoredFoilBuffer(IRenderTypeBuffer buffer, RenderType renderType, boolean notEntity, boolean hasFoil) {
        if (hasFoil) {
            if (Minecraft.useShaderTransparency() && renderType == Atlases.translucentItemSheet()) {
                return VertexBuilderUtils.create(buffer.getBuffer(CERenderType.coloredGlintTranslucent()), buffer.getBuffer(renderType)) ;
            } else {
                return VertexBuilderUtils.create(buffer.getBuffer(notEntity ? CERenderType.coloredGlint() : CERenderType.coloredEntityGlint()), buffer.getBuffer(renderType));
            }
        } else {
            return buffer.getBuffer(renderType);
        }
    }

    public static IVertexBuilder getColoredFoilBufferDirect(IRenderTypeBuffer buffer, RenderType renderType, boolean notEntity, boolean hasFoil) {
        if (hasFoil) {
            return VertexBuilderUtils.create(buffer.getBuffer(renderType), buffer.getBuffer(notEntity ? CERenderType.coloredGlintDirect() : CERenderType.coloredEntityGlintDirect()));
        } else {
            return buffer.getBuffer(renderType);
        }
    }

    public static IVertexBuilder getColoredArmorFoilBuffer(IRenderTypeBuffer buffer, RenderType renderType, boolean notEntity, boolean hasFoil) {
        if (hasFoil) {
            if(Minecraft.useShaderTransparency() && renderType == Atlases.translucentItemSheet()){
                return VertexBuilderUtils.create(buffer.getBuffer(CERenderType.glintTranslucent()), buffer.getBuffer(renderType));
            } else {
                return VertexBuilderUtils.create(buffer.getBuffer(notEntity ? CERenderType.coloredGlint() : CERenderType.coloredEntityGlint()), buffer.getBuffer(renderType));
            }
        } else {
            return buffer.getBuffer(renderType);
        }
    }

    public static IVertexBuilder getColoredCompassFoilBuffer(IRenderTypeBuffer buffer, RenderType renderType, MatrixStack.Entry entry) {
        return VertexBuilderUtils.create(new MatrixApplyingVertexBuilder(buffer.getBuffer(CERenderType.coloredGlint()), entry.pose(), entry.normal()), buffer.getBuffer(renderType));
    }

    public static IVertexBuilder getColoredCompassFoilBufferDirect(IRenderTypeBuffer buffer, RenderType renderType, MatrixStack.Entry entry) {
        return VertexBuilderUtils.create(new MatrixApplyingVertexBuilder(buffer.getBuffer(CERenderType.coloredGlintDirect()), entry.pose(), entry.normal()), buffer.getBuffer(renderType));
    }

    @Override
    public IBakedModel getModel(ItemStack p_184393_1_, @Nullable World p_184393_2_, @Nullable LivingEntity p_184393_3_) {
        Item item = p_184393_1_.getItem();
        IBakedModel bakedModel;
        if (item == Items.TRIDENT) {
            bakedModel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident_in_hand#inventory"));
        } else {
            bakedModel = this.itemModelShaper.getItemModel(p_184393_1_);
        }

        ClientWorld clientworld = p_184393_2_ instanceof ClientWorld ? (ClientWorld)p_184393_2_ : null;
        IBakedModel bakedModel1 = bakedModel.getOverrides().resolve(bakedModel, p_184393_1_, clientworld, p_184393_3_);
        return bakedModel1 == null ? this.itemModelShaper.getModelManager().getMissingModel() : bakedModel1;
    }

    @Override
    public void renderStatic(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, int lightmapCoord, int overlayCoord, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer) {
        renderStatic(null, itemStack, transformType, false, matrixStack, renderTypeBuffer, null, lightmapCoord, overlayCoord);
    }

    @Override
    public void renderStatic(@Nullable LivingEntity entity, ItemStack itemStack, ItemCameraTransforms.TransformType transformType, boolean leftHand, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, @Nullable World world, int lightmapCoord, int overlayCoord) {
        if (!itemStack.isEmpty()) {
            IBakedModel bakedModel = this.getModel(itemStack, world, entity);
            render(itemStack, transformType, leftHand, matrixStack, renderTypeBuffer, lightmapCoord, overlayCoord, bakedModel);
        }
    }

    @Override
    protected void renderGuiItem(ItemStack itemStack, int x, int y, IBakedModel bakedModel) {
        RenderSystem.pushMatrix();
        textureManager.bind(AtlasTexture.LOCATION_BLOCKS);
        textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.translatef((float)x, (float)y, 100.0F + this.blitOffset);
        RenderSystem.translatef(8.0F, 8.0F, 0.0F);
        RenderSystem.scalef(1.0F, -1.0F, 1.0F);
        RenderSystem.scalef(16.0F, 16.0F, 16.0F);
        MatrixStack matrixstack = new MatrixStack();
        //IRenderTypeBuffer.Impl buffer = ModRenderTypeBuffers.getInstance().bufferSource();
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !bakedModel.usesBlockLight();
        if (flag) {
            RenderHelper.setupForFlatItems();
        }
        render(itemStack, ItemCameraTransforms.TransformType.GUI, false, matrixstack, buffer, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
        buffer.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            RenderHelper.setupFor3DItems();
        }
        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
    }

    private void tryRenderGuiItem(@Nullable LivingEntity p_239387_1_, ItemStack p_239387_2_, int p_239387_3_, int p_239387_4_) {
        if (!p_239387_2_.isEmpty()) {
            this.blitOffset += 50.0F;

            try {
                this.renderGuiItem(p_239387_2_, p_239387_3_, p_239387_4_, this.getModel(p_239387_2_, null, p_239387_1_));
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> String.valueOf(p_239387_2_.getItem()));
                crashreportcategory.setDetail("Registry Name", () -> String.valueOf(p_239387_2_.getItem().getRegistryName()));
                crashreportcategory.setDetail("Item Damage", () -> String.valueOf(p_239387_2_.getDamageValue()));
                crashreportcategory.setDetail("Item NBT", () -> String.valueOf(p_239387_2_.getTag()));
                crashreportcategory.setDetail("Item Foil", () -> String.valueOf(p_239387_2_.hasFoil()));
                throw new ReportedException(crashreport);
            }

            this.blitOffset -= 50.0F;
        }
    }

    public void renderAndDecorateItem(ItemStack p_180450_1_, int p_180450_2_, int p_180450_3_) {
        this.tryRenderGuiItem(Minecraft.getInstance().player, p_180450_1_, p_180450_2_, p_180450_3_);
    }

    public void renderAndDecorateFakeItem(ItemStack p_239390_1_, int p_239390_2_, int p_239390_3_) {
        this.tryRenderGuiItem(null, p_239390_1_, p_239390_2_, p_239390_3_);
    }

    public void renderAndDecorateItem(LivingEntity p_184391_1_, ItemStack p_184391_2_, int p_184391_3_, int p_184391_4_) {
        this.tryRenderGuiItem(p_184391_1_, p_184391_2_, p_184391_3_, p_184391_4_);
    }

    public void onResourceManagerReload(IResourceManager p_195410_1_) {
        this.itemModelShaper.rebuildCache();
    }

    public ItemModelMesher getItemModelShaper() {
        return this.itemModelShaper;
    }

    public ItemRenderer getOriginalItemRenderer(){
        return originalItemRenderer;
    }

    public void dummy(){

    }

}
