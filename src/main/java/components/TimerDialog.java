
package components;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class TimerDialog extends DialogWrapper {
    private Timer timer;

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
        // timerDisplay.setPreferredSize(new Dimension(100, 100));
        dialogPanel.add(timerDisplay, BorderLayout.CENTER);

        return dialogPanel;
    }
}