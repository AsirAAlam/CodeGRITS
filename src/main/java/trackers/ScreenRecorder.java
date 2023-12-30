package trackers;

import com.opencsv.CSVWriter;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenRecorder {

    /*
     * 0: initial state; only startAction enabled
     * 1: started, not paused; stopAction and pauseAction enabled
     * 2: started, paused; only resumeAction enabled
     */
    int state = 0;
    int frameRate = 4; // higher frame rate (e.g., 12) will result in larger file size and blurry video
    private FrameRecorder recorder;
    private FrameGrabber grabber;
    private final ArrayList<String[]> timeList = new ArrayList<>();
    private CSVWriter csvWriter;
    boolean isRecording = false;
    private int clipNumber = 1;
    private int frameNumber = 0;
    private String dataOutputPath = "";
    private static ScreenRecorder instance = null;

    public static ScreenRecorder getInstance() {
        if (instance == null) {
            instance = new ScreenRecorder();
        }
        return instance;
    }


    private void createEncoder() throws IOException {
        // avfoundation for macOS, gdigrab for Windows, xcbgrab for Linux
        if (utils.OSDetector.isMac()) {
            grabber = new FFmpegFrameGrabber("1");
            grabber.setFormat("avfoundation");
        } else if (utils.OSDetector.isWindows()) {
            grabber = new FFmpegFrameGrabber("desktop");
            grabber.setFormat("gdigrab");
        } else if (utils.OSDetector.isUnix()) {
            grabber = new FFmpegFrameGrabber(":0.0");
            grabber.setFormat("x11grab");
        } else {
            throw new IOException("Unsupported OS");
        }
        grabber.setFrameRate(frameRate);
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration();
        // set image width and height to be the same as the resolution of the *first* screen (in case of multiple screens)
        grabber.setImageWidth((int) (Toolkit.getDefaultToolkit().getScreenSize().width * config.getDefaultTransform().getScaleX()));
        grabber.setImageHeight((int) (Toolkit.getDefaultToolkit().getScreenSize().height * config.getDefaultTransform().getScaleY()));
        grabber.setOption("offset_x", "0");
        grabber.setOption("offset_y", "0");
        grabber.start();

        recorder = FrameRecorder.createDefault(dataOutputPath + "/screen_recording/clip_" + clipNumber + ".mp4", grabber.getImageWidth(), grabber.getImageHeight());
        recorder.setFrameRate(frameRate);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.start();
    }


    public void startRecording() throws IOException {
        state = 1;
        clipNumber = 1;
        timeList.clear();
        isRecording = true;
        File file = new File(dataOutputPath + "/screen_recording/frames.csv");
        file.getParentFile().mkdirs();
        csvWriter = new CSVWriter(new FileWriter(file));
        csvWriter.writeNext(new String[]{"timestamp", "frame_number", "clip_number"});
        timeList.add(new String[]{String.valueOf(System.currentTimeMillis()), "Start", String.valueOf(clipNumber)});
        try {
            recordScreen();
        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() throws IOException {
        state = 0;
        isRecording = false;
        timeList.add(new String[]{String.valueOf(System.currentTimeMillis()), "Stop", String.valueOf(clipNumber)});
        csvWriter.writeAll(timeList);
        csvWriter.close();
    }

    public void pauseRecording() throws IOException {
        state = 2;
        isRecording = false;
        timeList.add(new String[]{String.valueOf(System.currentTimeMillis()), "Pause", String.valueOf(clipNumber)});
        clipNumber++;
    }

    public void resumeRecording() {
        state = 1;
        isRecording = true;
        timeList.add(new String[]{String.valueOf(System.currentTimeMillis()), "Resume", String.valueOf(clipNumber)});
        try {
            recordScreen();
        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }

    private void recordScreen() throws AWTException, IOException {
        createEncoder();
        frameNumber = 0;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isRecording) {
                    try {
                        grabber.stop();
                        recorder.stop();
                        grabber.release();
                        recorder.release();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    timer.cancel();
                } else {
                    frameNumber++;
                    timeList.add(new String[]{String.valueOf(System.currentTimeMillis()), String.valueOf(frameNumber), String.valueOf(clipNumber)});
                    try {
                        Frame frame = grabber.grabFrame();
                        recorder.record(frame);
                    } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }, 0, 1000 / frameRate);
    }

    public void setDataOutputPath(String dataOutputPath) {
        this.dataOutputPath = dataOutputPath;
    }
}