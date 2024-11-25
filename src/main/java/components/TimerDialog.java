
package components;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import actions.StartStopTrackingAction;
import actions.PauseResumeTrackingAction;


public class TimerDialog extends DialogWrapper {

    private static TimerDialog instance = null;
    private final StartStopTrackingAction startStopTrackingAction;
    private Timer timer;
    private long startTime;
    private long pauseTime;
    private boolean isPaused = false;
    private Action pauseAction;

    public TimerDialog(StartStopTrackingAction startStopTrackingAction) {
        super(true); // use current window as parent
        this.startStopTrackingAction = startStopTrackingAction;
        setTitle("Timer");
        setModal(false);
        init();
        instance = this;
    }

    //  Used to retrieve the instance of the class in other functions for pausing and resuming.
    public static TimerDialog getInstance() {
        return instance;
    }

    // Creates the Timer Dialog box which contains buttons to pause and stop the timer.
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        // Create the panel itself along with the button to stop the timer and call the startstoptrackingaction.
        JPanel dialogPanel = new JPanel(new BorderLayout());

        // Create a display which is formatted to hold the timer.
        JLabel timerDisplay = new JLabel("Time: 0:00", SwingConstants.CENTER);
        timerDisplay.setFont(new Font("Arial", Font.BOLD, 24));

        // Get the current time in milliseconds.
        startTime = System.currentTimeMillis();
        // Create a new timer which updates every 1000 milliseconds and calculates the minutes and seconds from the start.
        timer = new Timer(1000, e -> {
            long timeFromStart;
            if (isPaused)
                timeFromStart = pauseTime;
            else
                timeFromStart = System.currentTimeMillis() - startTime;
            int seconds = (int) (timeFromStart / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            timerDisplay.setText(String.format("Time: %d:%02d", minutes, seconds));
        });

        // Start the timer and add the components + adjust the panel location.
        timer.start();
        dialogPanel.add(timerDisplay, BorderLayout.CENTER);
        placePanelTopRight();
        return dialogPanel;
    }

    // Function which pauses the timer.
    public void pauseTimer(boolean buttonPressed)
    {
        if (timer.isRunning())
        {
            // Calculates the pause time and updates the button text to say "resume".
            timer.stop();
            pauseTime = System.currentTimeMillis() - startTime;
            isPaused = true;
            pauseAction.putValue(pauseAction.NAME, "Resume");
            // If the pause button on the timer was pressed, update the dropdown text as well.
            if (buttonPressed)
                PauseResumeTrackingAction.getInstance().update(createAnActionEvent());
        }
    }

    // Function which resumes the timer.
    public void resumeTimer(boolean buttonPressed)
    {
        if (!timer.isRunning())
        {
            // Calculates the new start time and updates the button text to say "pause".
            startTime = System.currentTimeMillis() - pauseTime;
            timer.start();
            isPaused = false;
            pauseAction.putValue(pauseAction.NAME, "Pause");
            // If the pause button on the timer was pressed, update the dropdown text as well.
            if (buttonPressed)
                PauseResumeTrackingAction.getInstance().update(createAnActionEvent());
        }
    }

    // Function designed to set the location of the panel to the top right of the screen.
    private void placePanelTopRight()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int minDistance = 20;   // Minimum distance between the panel and the edges of the screen.
        setLocation(screenSize.width - getContentPanel().getWidth()
                - minDistance, minDistance);
    }


    // Function to override the two default buttons with buttons for pausing/resuming and stopping.
    @Override
    protected Action @NotNull [] createActions() {
        // Create a custom "Stop Tracking" button which calls the stop tracking action and closes the timer.
        Action stopTrackingAction = new DialogWrapperAction("Stop") {
            @Override
            protected void doAction(ActionEvent e) {
                startStopTrackingAction.actionPerformed(createAnActionEvent()); // Trigger the action
                close(OK_EXIT_CODE); // Close the dialog
            }
        };

        // Creates a custom "Pause Tracking" button to pause and unpause the timer.
        // PauseAction is global to allow for pause/resume tracking to update the text.
        pauseAction = new DialogWrapperAction("Pause") {
            @Override
            protected void doAction(ActionEvent e) {
                if (isPaused)
                    resumeTimer(true);
                else
                    pauseTimer(true);
                PauseResumeTrackingAction.getInstance().actionPerformed(createAnActionEvent()); // Trigger the action
            }
        };

        return new Action[]{
                pauseAction,
                stopTrackingAction
        };
    }

    // Placeholder function to enable the usage of functions requiring AnAction
    // within pauseResumeTracking and startStopTracking.
    private AnActionEvent createAnActionEvent() {
        DataContext dataContext = new DataContext() {
            @Override
            public @Nullable Object getData(@NotNull String s) {
                return null;
            }
        };

        return new AnActionEvent(null, dataContext, "TimerDialog", new Presentation(), ActionManager.getInstance(), 0
        );
    }

}