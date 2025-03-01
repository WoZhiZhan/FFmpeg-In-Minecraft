package net.wzz.bluearchivescraft.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

public class VideoRenderer {

    public void render(int textureID) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, textureID);
        Minecraft minecraft = Minecraft.getInstance();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(0, screenHeight, 0).uv(0, 0).endVertex();
        bufferBuilder.vertex(screenWidth, screenHeight, 0).uv(1, 0).endVertex();
        bufferBuilder.vertex(screenWidth, 0, 0).uv(1, 1).endVertex();
        bufferBuilder.vertex(0, 0, 0).uv(0, 1).endVertex();
        Tesselator.getInstance().end();
    }

}