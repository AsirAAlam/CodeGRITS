package actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import components.ConfigDialog;
import components.TimerDialog;
import entity.Config;
import org.jetbrains.annotations.NotNull;
import trackers.EyeTracker;
import trackers.IDETracker;
import trackers.ScreenRecorder;
import utils.AvailabilityChecker;
import utils.Heatmap;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Objects;

/**
 * This class is the action for starting/stopping tracking.
 */
public class StartStopTrackingAction extends AnAction {

    /**
     * This variable indicates whether the tracking is started.
     */
    private static boolean isTracking = false;
    /**
     * This variable is the IDE tracker.
     */
    private static IDETracker iDETracker;
    /**
     * This variable is the eye tracker.
     */
    private static EyeTracker eyeTracker;
    /**
     * This variable is the screen recorder.
     */
    private final ScreenRecorder screenRecorder = ScreenRecorder.getInstance();
    /**
     * This variable is the configuration.
     */
    Config config = new Config();

    /**
     * This variable is the configuration.
     */
    TimerDialog timerDialog;

    /**
     * Update the text of the action button.
     *
     * @param e The action event.
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(isTracking ? "Stop Tracking" : "Start Tracking");
    }

    /**
     * This method is called when the action is performed. It will start/stop tracking.
     *
     * @param e The action event.
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (config.configExists()) {
            config.loadFromJson();
        } else {
            Notification notification = new Notification("CodeGRITS Notification Group", "Configuration",
                    "Please configure the plugin first.", NotificationType.WARNING);
            notification.notify(e.getProject());
            return;
        }
        try {
            if (!isTracking) {
                if (config.getCheckBoxes().get(1)) {
                    String pythonEnvironmentStatus = AvailabilityChecker.checkPythonEnvironment(config.getPythonInterpreter());

                    if (!pythonEnvironmentStatus.equals("OK")) {
                        JOptionPane.showMessageDialog(null, "Error verifying python environment:\n\n" + pythonEnvironmentStatus);
                        return;
                    }
                    if (config.getEyeTrackerDevice() != 0 && !AvailabilityChecker.checkEyeTracker(config.getPythonInterpreter())) {
                        JOptionPane.showMessageDialog(null, "Eye tracker not found. Please configure the mouse simulation first.");
                        return;
                    }
                }

                isTracking = true;
                ConfigAction.setIsEnabled(false);
                AddLabelActionGroup.setIsEnabled(true);
                String projectPath = e.getProject() != null ? e.getProject().getBasePath() : "";
                String realDataOutputPath = Objects.equals(config.getDataOutputPath(), ConfigDialog.selectDataOutputPlaceHolder)
                        ? projectPath : config.getDataOutputPath();
                realDataOutputPath += "/" + System.currentTimeMillis() + "/";

                if (config.getCheckBoxes().get(2)) {
                    screenRecorder.setDataOutputPath(realDataOutputPath);
                    screenRecorder.startRecording();
                }

                iDETracker = IDETracker.getInstance();
                iDETracker.setProjectPath(projectPath);
                iDETracker.setDataOutputPath(realDataOutputPath);
                iDETracker.startTracking(e.getProject());

                if (config.getCheckBoxes().get(1)) {
                    eyeTracker = new EyeTracker();
                    eyeTracker.setProjectPath(projectPath);
                    eyeTracker.setDataOutputPath(realDataOutputPath);
                    eyeTracker.setPythonInterpreter(config.getPythonInterpreter());
                    eyeTracker.setSampleFrequency(config.getSampleFreq());
                    eyeTracker.setDeviceIndex(config.getEyeTrackerDevice());
                    eyeTracker.setPythonScriptTobii();
                    eyeTracker.setPythonScriptMouse();
                    eyeTracker.startTracking(e.getProject());
                }
                AddLabelAction.setIsEnabled(true);
                timerDialog = new TimerDialog(this);
                timerDialog.show();

            } else {
                isTracking = false;
                iDETracker.stopTracking();
                AddLabelAction.setIsEnabled(false);
                ConfigAction.setIsEnabled(true);
                if (config.getCheckBoxes().get(1) && eyeTracker != null) {
                    eyeTracker.stopTracking();

                    if (config.getCheckBoxes().get(3)) {
                        Heatmap.genHeatmap(config.getPythonInterpreter(), eyeTracker.getDataOutputPath());
                    }
                }
                if (config.getCheckBoxes().get(2)) {
                    screenRecorder.stopRecording();
                }
                eyeTracker = null;
                // If the timer dialog was not closed manually, close the timer dialog.
                if (timerDialog != null)
                    timerDialog.close(0);
            }
        } catch (ParserConfigurationException | TransformerException | IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean isTracking() {
        return isTracking;
    }

    public static boolean isPaused() {
        return !iDETracker.isTracking();
    }

    public static void pauseTracking() {
        iDETracker.pauseTracking();
        if (eyeTracker != null) {
            eyeTracker.pauseTracking();
        }
    }

    public static void resumeTracking() {
        iDETracker.resumeTracking();
        if (eyeTracker != null) {
            eyeTracker.resumeTracking();
        }
    }

}