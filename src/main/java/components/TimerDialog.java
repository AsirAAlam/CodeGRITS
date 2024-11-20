
package components;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TimerDialog extends DialogWrapper {

    public TimerDialog() {
        super(true); // use current window as parent
        setTitle("Timer");
        setModal(false);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());

        JLabel timerDisplay = new JLabel("Time: 0:00", SwingConstants.CENTER);
        long start = System.currentTimeMillis();

        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long timeFromStart = System.currentTimeMillis() - start;
                int seconds = (int) (timeFromStart / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                timerDisplay.setText(String.format("Time: %d:%02d", minutes, seconds));
            }
        });
        timer.start();
        dialogPanel.add(timerDisplay, BorderLayout.CENTER);

        return dialogPanel;
    }
}