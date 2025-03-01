package net.wzz.bluearchivescraft.menu;

import org.bytedeco.javacv.Frame;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;

public class Main {
    public static VideoPlayer3 player = new VideoPlayer3();
    public static void render() throws Exception {
        //这里改到mc屏幕渲染的render里面
        if (player != null) {
            if (!player.hasVideo) {
                player.loadVideo("menuVideo.mp4");
                Thread audioThread = new Thread(() -> {
                    try {
                        player.playAudio();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                audioThread.start();
            }
            VideoRenderer videoRenderer = new VideoRenderer();
            Frame frame = player.getNextFrame();
            int textureID = TextureLoader2.loadTexture(frame);
            if (textureID == -1) {
                player.close();
	return;
            }
            videoRenderer.render(textureID);
        }
    }
}
