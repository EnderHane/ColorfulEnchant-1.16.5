package com.github.enderhane.colorfulenchant.client.renderer;

import com.github.enderhane.colorfulenchant.ColorfulEnchant;
import com.github.enderhane.colorfulenchant.client.renderer.vertex.MatrixColorVertexBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.entity.model.ShieldModel;
import net.minecraft.client.renderer.entity.model.TridentModel;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.BannerTileEntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * @see net.minecraft.client.renderer.ItemRenderer
 */
@OnlyIn(Dist.CLIENT)
public class CEItemRenderer extends ItemRenderer {

    public static final ResourceLocation CUSTOM_GLINT_LOCATION = new ResourceLocation(ColorfulEnchant.MOD_ID, "textures/misc/custom_glint.png");
    public float blitOffset;
    private final ItemModelMesher itemModelShaper;
    private final TextureManager textureManager;
    private final ItemColors itemColors;
    /**
     * 备用 ItemRenderer
     */
    private final ItemRenderer originalItemRenderer;

    private final ShieldModel shieldModel = new ShieldModel();
    private final TridentModel tridentModel = new TridentModel();

    public CEItemRenderer(ItemRenderer originalItemRenderer, TextureManager textureManager, ModelManager modelManager, ItemColors itemColors) {
        super(textureManager, modelManager, itemColors);
        this.itemModelShaper = originalItemRenderer.getItemModelShaper();
        this.textureManager = textureManager;
        this.itemColors = itemColors;
        this.originalItemRenderer = originalItemRenderer;
    }

    /**
     * 覆写的核心渲染方法，分离了物品和光效的渲染，
     * <p>并且接管了三叉戟和盾牌</p>
     */
    @Override
    public void render(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, boolean leftHand,
                       MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int lightmap, int overlay, IBakedModel bakedModel) {
        if (!itemStack.isEmpty()) {
            CERenderTypeBuffer appointedBuffer = CEBufferSourceHelper.bufferSource();
            matrixStack.pushPose();
            boolean flag = transformType == ItemCameraTransforms.TransformType.GUI ||
                transformType == ItemCameraTransforms.TransformType.GROUND ||
                transformType == ItemCameraTransforms.TransformType.FIXED;
            if (itemStack.getItem() == Items.TRIDENT && flag) {
                bakedModel = itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
            }
            bakedModel = ForgeHooksClient.handleCameraTransforms(matrixStack, bakedModel, transformType, leftHand);
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
                    ForgeHooksClient.drawItemLayered(this, bakedModel, itemStack, matrixStack, renderTypeBuffer, lightmap, overlay, flag1);
                }
                else {
                    RenderType itemRenderType = RenderTypeLookup.getRenderType(itemStack, flag1);
                    IVertexBuilder itemBuilder = appointedBuffer.getBuffer(itemRenderType);
                    renderModelLists(bakedModel, itemStack, lightmap, overlay, matrixStack, itemBuilder);
                    appointedBuffer.endBatch(itemRenderType);
                    if (itemStack.hasFoil()){
                        renderFoil(bakedModel, itemStack, matrixStack, transformType, appointedBuffer, lightmap, overlay, itemRenderType);
                    }
                }
            } else if (itemStack.getItem() == Items.SHIELD){
                boolean flag2 = itemStack.getTagElement("BlockEntityTag") != null;
                matrixStack.pushPose();
                matrixStack.scale(1.0F, -1.0F, -1.0F);
                RenderMaterial renderMaterial = flag2 ? ModelBakery.SHIELD_BASE : ModelBakery.NO_PATTERN_SHIELD;
                RenderType shieldRenderType = shieldModel.renderType(renderMaterial.atlasLocation());
                IVertexBuilder vertexBuilder = renderMaterial.sprite().wrap(appointedBuffer.getBuffer(shieldRenderType));
                shieldModel.handle().render(matrixStack, vertexBuilder, lightmap, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
                if (flag2) {
                    List<Pair<BannerPattern, DyeColor>> list = BannerTileEntity.createPatterns(ShieldItem.getColor(itemStack), BannerTileEntity.getItemPatterns(itemStack));
                    BannerTileEntityRenderer.renderPatterns(matrixStack, appointedBuffer, overlay, lightmap, shieldModel.plate(), renderMaterial, false, list, itemStack.hasFoil());
                } else {
                    shieldModel.plate().render(matrixStack, vertexBuilder, lightmap, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
                }
                appointedBuffer.endBatch(shieldRenderType);
                if (itemStack.hasFoil()){
                    RenderType tileEntityFoilRenderType = CERenderType.coloredEntityGlintDirect();
                    renderFoilModel(shieldModel, itemStack, matrixStack, appointedBuffer, lightmap, overlay, tileEntityFoilRenderType);
                }
                matrixStack.popPose();
            } else if (itemStack.getItem() == Items.TRIDENT){
                matrixStack.pushPose();
                matrixStack.scale(1.0F, -1.0F, -1.0F);
                RenderType tridentRenderType = tridentModel.renderType(TridentModel.TEXTURE);
                IVertexBuilder tridentBuilder = appointedBuffer.getBuffer(tridentRenderType);
                tridentModel.renderToBuffer(matrixStack, tridentBuilder, lightmap, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
                appointedBuffer.endBatch(tridentRenderType);
                if (itemStack.hasFoil()){
                    RenderType tileEntityFoilRenderType = CERenderType.coloredEntityGlintDirect();
                    renderFoilModel(tridentModel, itemStack, matrixStack, appointedBuffer, lightmap, overlay, tileEntityFoilRenderType);
                }
                matrixStack.popPose();
            }
            else {
                itemStack.getItem().getItemStackTileEntityRenderer().renderByItem(itemStack, transformType, matrixStack, renderTypeBuffer, lightmap, overlay);
            }
            matrixStack.popPose();
            appointedBuffer.endBatchAfterAll();
        }
    }

    /**
     * 渲染三叉戟和盾牌的光效
     */
    public void renderFoilModel(Model model, ItemStack itemStack, MatrixStack matrixStack, IRenderTypeBuffer bufferSource, int lightmap, int overlay, RenderType renderType){
        IVertexBuilder foilBuilder = bufferSource.getBuffer(renderType);
        model.renderToBuffer(matrixStack, foilBuilder, lightmap, overlay , 1.0f, 0.5f, 0.5f, 1.0f);
    }

    /**
     * 独立光效渲染
     */
    public void renderFoil(IBakedModel bakedModel, ItemStack itemStack, MatrixStack matrixStack, ItemCameraTransforms.TransformType transformType,
                            IRenderTypeBuffer renderTypeBuffer, int lightmap, int overlay, RenderType renderType){
        IVertexBuilder foilBuilder;
        /* TODO: fix compass glint color */
        if (itemStack.getItem()==Items.COMPASS) {
            matrixStack.pushPose();
            MatrixStack.Entry matrixStack$entry = matrixStack.last();
            if (transformType == ItemCameraTransforms.TransformType.GUI) {
                matrixStack$entry.pose().multiply(0.5F);
            } else if (transformType.firstPerson()) {
                matrixStack$entry.pose().multiply(0.75F);
            }
            foilBuilder = new MatrixColorVertexBuilder(renderTypeBuffer.getBuffer(CERenderType.coloredGlint()), matrixStack$entry.pose(), matrixStack$entry.normal());
            //foilBuilder = renderTypeBuffer.getBuffer(CERenderType.coloredGlint());
            //foilBuilder = new MatrixApplyingVertexBuilder(renderTypeBuffer.getBuffer(CERenderType.coloredGlint()), matrixStack$entry.pose(), matrixStack$entry.normal());
            matrixStack.popPose();
        } else if(Minecraft.useShaderTransparency() && renderType == Atlases.translucentItemSheet()){
            foilBuilder = renderTypeBuffer.getBuffer(CERenderType.coloredGlintTranslucent());
        } else {
            foilBuilder = renderTypeBuffer.getBuffer(CERenderType.coloredGlint());
        }
        renderModelWithColor(matrixStack, bakedModel, foilBuilder, 0xffff9999, lightmap, overlay);
    }

    public void renderModelWithColor(MatrixStack matrixStack, IBakedModel model, IVertexBuilder vertexBuilder, int color, int lightmap, int overlay){
        Random random = new Random();
        for (Direction direction: Direction.values()){
            random.setSeed(42L);
            List<BakedQuad> quads = model.getQuads(null, direction, random, null);
            renderQuadWithColor(matrixStack, vertexBuilder, quads, color, lightmap, overlay);
        }
        random.setSeed(42L);
        List<BakedQuad> quads = model.getQuads(null, null, random, null);
        renderQuadWithColor(matrixStack, vertexBuilder, quads, color, lightmap, overlay);
    }

    public void renderQuadWithColor(MatrixStack matrixStack, IVertexBuilder vertexBuilder, List<BakedQuad> quads, int color, int lightmap, int overlay){
        MatrixStack.Entry entry = matrixStack.last();
        float alpha = (float) ((color >> 24) & 0xff) / 255.f;
        float red = (float) ((color >> 16) & 0xff) / 255.f;
        float green = (float) ((color >> 8) & 0xff) / 255.f;
        float blue = (float) (color & 0xff) / 255.f;
        for (BakedQuad quad: quads){
            vertexBuilder.addVertexData(entry, quad, red, green, blue, alpha, lightmap, overlay, true);
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
        MatrixStack.Entry matrixStack$entry = matrixStack.last();
        for(BakedQuad bakedquad : bakedQuads) {
            int i = -1;
            if (flag && bakedquad.isTinted()) {
                i = this.itemColors.getColor(itemStack, bakedquad.getTintIndex());
            }
            float red = (float)(i >> 16 & 255) / 255.0F;
            float green = (float)(i >> 8 & 255) / 255.0F;
            float blue = (float)(i & 255) / 255.0F;
            vertexBuilder.addVertexData(matrixStack$entry, bakedquad, red, green, blue, 1.0f ,lightmapCoord, overlayCoord, true);
        }
    }

    @Override
    public IBakedModel getModel(ItemStack itemStack, @Nullable World world, @Nullable LivingEntity entity) {
        Item item = itemStack.getItem();
        IBakedModel bakedModel;
        if (item == Items.TRIDENT) {
            bakedModel = itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident_in_hand#inventory"));
        } else {
            bakedModel = itemModelShaper.getItemModel(itemStack);
        }
        ClientWorld clientworld = world instanceof ClientWorld ? (ClientWorld)world : null;
        IBakedModel overriddenModel = bakedModel.getOverrides().resolve(bakedModel, itemStack, clientworld, entity);
        return overriddenModel == null ? itemModelShaper.getModelManager().getMissingModel() : overriddenModel;
    }

    @Override
    public void renderStatic(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, int lightmap, int overlay,
                             MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer) {
        renderStatic(null, itemStack, transformType, false, matrixStack, renderTypeBuffer, null, lightmap, overlay);
    }

    @Override
    public void renderStatic(@Nullable LivingEntity entity, ItemStack itemStack, ItemCameraTransforms.TransformType transformType, boolean leftHand,
                             MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, @Nullable World world, int lightmapCoord, int overlayCoord) {
        if (!itemStack.isEmpty()) {
            IBakedModel bakedModel = getModel(itemStack, world, entity);
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

    private void tryRenderGuiItem(@Nullable LivingEntity entity, ItemStack itemStack, int x, int y) {
        if (!itemStack.isEmpty()) {
            blitOffset += 50.0F;
            try {
                renderGuiItem(itemStack, x, y, getModel(itemStack, null, entity));
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> String.valueOf(itemStack.getItem()));
                crashreportcategory.setDetail("Registry Name", () -> String.valueOf(itemStack.getItem().getRegistryName()));
                crashreportcategory.setDetail("Item Damage", () -> String.valueOf(itemStack.getDamageValue()));
                crashreportcategory.setDetail("Item NBT", () -> String.valueOf(itemStack.getTag()));
                crashreportcategory.setDetail("Item Foil", () -> String.valueOf(itemStack.hasFoil()));
                throw new ReportedException(crashreport);
            }
            blitOffset -= 50.0F;
        }
    }

    @Override
    public void renderAndDecorateItem(ItemStack itemStack, int x, int y) {
        tryRenderGuiItem(Minecraft.getInstance().player, itemStack, x, y);
    }

    @Override
    public void renderAndDecorateFakeItem(ItemStack itemStack, int x, int y) {
        tryRenderGuiItem(null, itemStack, x, y);
    }

    @Override
    public void renderAndDecorateItem(LivingEntity entity, ItemStack itemStack, int x, int y) {
        tryRenderGuiItem(entity, itemStack, x, y);
    }

    @Override
    public void onResourceManagerReload(IResourceManager p_195410_1_) {
        itemModelShaper.rebuildCache();
    }

    @Override
    public ItemModelMesher getItemModelShaper() {
        return itemModelShaper;
    }

    public ItemRenderer getOriginalItemRenderer(){
        return originalItemRenderer;
    }

}
