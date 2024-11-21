
package components;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

import actions.StartStopTrackingAction;

public class TimerDialog extends DialogWrapper {

    private final StartStopTrackingAction startStopTrackingAction = new StartStopTrackingAction();

    public TimerDialog() {
        super(true); // use current window as parent
        setTitle("Timer");
        setModal(false);
        init();
    }

    // Function designed to set the location of the panel to the top right of the screen.
    private void placePanelTopRight()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int minDistance = 20;   // Minimum distance between the panel and the edges of the screen.
        setLocation(screenSize.width - getContentPanel().getWidth()
                - minDistance, minDistance);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        // Create the panel itself along with the button to stop the timer and call the startstoptrackingaction.
        JPanel dialogPanel = new JPanel(new BorderLayout());
        JButton stopButton = getStopButton();

        // Create a display which is formatted to hold the timer.
        JLabel timerDisplay = new JLabel("Time: 0:00", SwingConstants.CENTER);
        timerDisplay.setFont(new Font("Arial", Font.BOLD, 24));

        // Get the current time in milliseconds.
        long start = System.currentTimeMillis();
        // Create a new timer which updates every 1000 milliseconds and calculates the minutes and seconds from the start.
        Timer timer = new Timer(1000, e -> {
            long timeFromStart = System.currentTimeMillis() - start;
            int seconds = (int) (timeFromStart / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            timerDisplay.setText(String.format("Time: %d:%02d", minutes, seconds));
        });

        // Start the timer and add the components + adjust the panel location.
        timer.start();
        dialogPanel.add(timerDisplay, BorderLayout.CENTER);
        dialogPanel.add(stopButton, BorderLayout.SOUTH);
        placePanelTopRight();
        return dialogPanel;
    }

    // Function which creates a button to stop the tracking.
    @NotNull
    private JButton getStopButton() {
        JButton stopButton = new JButton("Stop Tracking");
        stopButton.addActionListener(actionEvent -> {
            ActionManager actionManager = ActionManager.getInstance();
            DataContext dataContext = SimpleDataContext.EMPTY_CONTEXT;
            AnActionEvent trigger = new AnActionEvent(null, dataContext, "place", new Presentation(),
                    actionManager, 0);
            startStopTrackingAction.actionPerformed(trigger);
            this.dispose();
        });
        return stopButton;
    }
}