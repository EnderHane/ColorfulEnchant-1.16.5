package com.github.enderhane.colorfulenchant.client.renderer.vertex;

import com.mojang.blaze3d.vertex.DefaultColorVertexBuilder;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

public class MatrixColorVertexBuilder extends DefaultColorVertexBuilder {

    private final IVertexBuilder delegate;
    private final Matrix4f cameraInversePose;
    private final Matrix3f normalInversePose;
    private float x;
    private float y;
    private float z;
    private int r;
    private int g;
    private int b;
    private int a;
    private int overlayU;
    private int overlayV;
    private int lightmap;
    private float nx;
    private float ny;
    private float nz;

    public MatrixColorVertexBuilder(IVertexBuilder vertexBuilder,Matrix4f pose, Matrix3f normal) {
        this.delegate = vertexBuilder;
        this.cameraInversePose = pose.copy();
        this.cameraInversePose.invert();
        this.normalInversePose = normal.copy();
        this.normalInversePose.invert();
        this.resetState();
    }

    private void resetState(){
        x = 0.0F;
        y = 0.0F;
        z = 0.0F;
        overlayU = 0;
        overlayV = 10;
        lightmap = 15728880;
        nx = 0.0F;
        ny = 1.0F;
        nz = 0.0F;
    }

    @Override
    public IVertexBuilder vertex(double xIn, double yIn, double zIn) {
        x = (float) xIn;
        y = (float) yIn;
        z = (float) zIn;
        return this;
    }

    @Override
    public IVertexBuilder color(int rIn, int gIn, int bIn, int aIn) {
        r = rIn;
        g = gIn;
        b = bIn;
        a = aIn;
        return this;
    }

    @Override
    public IVertexBuilder uv(float texU, float texV) {
        return this;
    }

    @Override
    public IVertexBuilder overlayCoords(int u1, int v1) {
        overlayU = u1;
        overlayV = v1;
        return this;
    }

    @Override
    public IVertexBuilder uv2(int u2, int v2) {
        this.lightmap = u2 | v2 << 16;
        return this;
    }

    @Override
    public IVertexBuilder normal(float xIn, float yIn, float zIn) {
        nx = xIn;
        ny = yIn;
        nz = zIn;
        return this;
    }

    @Override
    public void endVertex() {
        Vector3f vector3f = new Vector3f(nx, ny, nz);
        vector3f.transform(normalInversePose);
        Direction direction = Direction.getNearest(vector3f.x(), vector3f.y(), vector3f.z());
        Vector4f vector4f = new Vector4f(x, y, z, 1.0F);
        vector4f.transform(cameraInversePose);
        vector4f.transform(Vector3f.YP.rotationDegrees(180.0F));
        vector4f.transform(Vector3f.XP.rotationDegrees(-90.0F));
        vector4f.transform(direction.getRotation());
        float f = -vector4f.x();
        float f1 = -vector4f.y();
        delegate
            .vertex(x, y, z)
            .color(r, g, b, a)
            .uv(f, f1)
            .overlayCoords(overlayU, overlayV)
            .uv2(lightmap)
            .normal(nx, ny, nz)
            .endVertex();
        resetState();
    }
}
