package net.wzz.bluearchivescraft.menu;

import net.wzz.bluearchivescraft.util.BlueArchivesLogger;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class TextureLoader2 {
    private static final Map<Frame, Integer> texturePool = new HashMap<>();
    public static int loadTexture(Frame frame) {
        if (frame == null || frame.image == null) {
            BlueArchivesLogger.getLogger().info("无效帧：无图像数据");
            return -1;
        }
        try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
            BufferedImage image = converter.convert(frame);
            if (image == null) {
                BlueArchivesLogger.getLogger().info("转换失败：不支持的像素格式");
                return -1;
            }
            int textureID = GL33.glGenTextures();
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, textureID);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_REPEAT);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_REPEAT);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
            int width = image.getWidth();
            int height = image.getHeight();
            int[] pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);
            ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
            for (int y = height - 1; y >= 0; y--) { // 翻转 Y 轴
                for (int x = 0; x < width; x++) {
                    int pixel = pixels[y * width + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF));
                    buffer.put((byte) ((pixel >> 8) & 0xFF));
                    buffer.put((byte) (pixel & 0xFF));
                    buffer.put((byte) ((pixel >> 24) & 0xFF));
                }
            }
            buffer.flip();
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
            GL11.glGetError();
            texturePool.put(frame, textureID);
            return textureID;
        }
    }

    public static void deleteAllTextures() {
        for (Map.Entry<Frame, Integer> entry : texturePool.entrySet()) {
            int textureId = entry.getValue();
            GL11.glDeleteTextures(textureId);
        }
        texturePool.clear();
    }
}
