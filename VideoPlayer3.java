package net.wzz.bluearchivescraft.menu;

import net.wzz.bluearchivescraft.util.JavaUtil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.io.File;

import javax.sound.sampled.*;
import java.nio.Buffer;
import java.nio.ShortBuffer;

public class VideoPlayer3 {
    private FFmpegFrameGrabber grabber;
    private FFmpegFrameGrabber grabberAudio;
    private AudioPlayer audioPlayer;
    public boolean hasVideo;
    private long previousVideoPts = -1;
    private long previousAudioPts = -1;

    public void loadVideo(String filePath) throws Exception {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                JavaUtil.sendErrorMessage("发生错误：找不到menuVideo.mp4文件夹，请确保文件存在\n如果没有文件，请将blue_start_video.txt中的true改为false\n点击确定退出游戏");
                System.exit(0);
            }
            grabber = new FFmpegFrameGrabber(filePath);
            grabberAudio = new FFmpegFrameGrabber(filePath);
            grabber.setVideoOption("hwaccel", "cuda");
            grabber.start();
            grabberAudio.start();
            audioPlayer = new AudioPlayer(grabberAudio.getSampleRate(), grabberAudio.getAudioChannels());
            hasVideo = true;
        } catch (Exception e) {
            System.err.println("加载视频失败: " + e.getMessage());
            throw e;
        }
    }

//    private void delayPlayback(long frameTime) {
//        try {
//            Thread.sleep(frameTime);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    private void delayPlayback(long frameTime) {
        long startTime = System.nanoTime();
        long delayTime = frameTime * 1_000_000;
        while (System.nanoTime() - startTime < delayTime) {}
    }

    public Frame getNextFrame() throws Exception {
        Frame frame;
        while ((frame = grabber.grab()) != null) {
            if (frame.image != null) {
                long currentPts = frame.timestamp;
                if (previousVideoPts != -1) {
                    long frameTime = currentPts - previousVideoPts;
	    //感觉视频卡可以增加这个值
                    delayPlayback(frameTime / 4000);
                }
                previousVideoPts = currentPts;
                return frame;
            }
        }
        return null;
    }

    public void playAudio() throws Exception {
        Frame audioFrame;
        while ((audioFrame = grabberAudio.grabSamples()) != null) {
            if (audioFrame.samples != null) {
                long currentPts = audioFrame.timestamp;
                if (previousAudioPts != -1) {
                    long frameTime = currentPts - previousAudioPts;
                    delayPlayback(frameTime / 1200);
                }
                previousAudioPts = currentPts;
                audioPlayer.writeSamples(audioFrame.samples[0]);
            }
        }
    }

    public void close() throws Exception {
        if (grabber != null) {
            grabber.stop();
            grabber.release();
            grabber.close();
        }
        if (grabberAudio != null) {
            grabberAudio.stop();
            grabberAudio.release();
            grabberAudio.close();
        }
        if (audioPlayer != null) {
            audioPlayer.drain();
            audioPlayer.close();
        }
        hasVideo = false;
    }

    private static class AudioPlayer {
        private SourceDataLine line;

        public AudioPlayer(int sampleRate, int channels) throws LineUnavailableException {
            AudioFormat format = new AudioFormat(sampleRate, 16, channels, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
        }

        public void writeSamples(Buffer buffer) {
            if (buffer instanceof ShortBuffer) {
                ShortBuffer shortBuffer = (ShortBuffer) buffer;
                short[] samples = new short[shortBuffer.remaining()];
                shortBuffer.get(samples);

                byte[] byteSamples = new byte[samples.length * 2];
                for (int i = 0; i < samples.length; i++) {
                    byteSamples[i * 2] = (byte) (samples[i] & 0xFF);
                    byteSamples[i * 2 + 1] = (byte) ((samples[i] >> 8) & 0xFF);
                }
                line.write(byteSamples, 0, byteSamples.length);
            } else {
                throw new IllegalArgumentException("Unsupported buffer type: " + buffer.getClass());
            }
        }

        public void drain() {
            line.drain();
        }

        public void close() {
            line.close();
        }
    }
}