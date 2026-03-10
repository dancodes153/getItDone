// GetItDone.java
// A task manager app to help you stay productive and hit your study goals!

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

public class GetItDone extends JFrame {

    // --- data ---

    ArrayList<Task> myTasks = new ArrayList<Task>();

    int studyGoal      = 5;
    int tasksFinished  = 0;
    int goalsCompleted = 0;

    // tracks which task we're up to for rotating the completion messages
    int completionMsgIndex = 0;

    // --- GUI components that need updating ---

    DefaultListModel<String> taskListModel      = new DefaultListModel<String>();
    DefaultListModel<String> completedListModel = new DefaultListModel<String>();

    JLabel goalProgressLabel = new JLabel("  Goal: 0 / 5 tasks");

    JProgressBar goalProgressBar = new JProgressBar(0, 5);

    // the achievements area on the Goals tab
    JTextArea achievementsArea;

    // reminder countdowns (minutes)
    int breakMinutesLeft = 25;
    int waterMinutesLeft = 60;
    int breakMsgIndex    = 0;
    int waterMsgIndex    = 0;

    // --- message banks ---

    // shown one by one each time a task is completed
    String[] completionMessages = {
        "Good job studying! Keep it up!",
        "Nice work! You're making real progress.",
        "Well done! Every task counts.",
        "That's the way to do it! Keep going.",
        "Great job! You should be proud of yourself.",
        "Awesome! One step closer to your goal.",
        "You're doing amazing - don't stop now!",
        "Look at you go! Another one checked off.",
        "Solid work! You're building great habits.",
        "Keep crushing it! You've got this."
    };

    // shown silently as toast notifications for breaks
    String[] breakMessages = {
        "Time for a quick stretch!",
        "Give your eyes a rest for a moment.",
        "Stand up and move around a little!",
        "Take a 5-minute breather - you earned it!",
        "Step away from the screen for a bit.",
        "Your brain needs a short break too!"
    };

    // shown silently as toast notifications for water
    String[] waterMessages = {
        "Go for a water break!",
        "Stay hydrated - drink some water!",
        "Time to refill your water bottle.",
        "Hydration check! Have you had water lately?",
        "Drink water, stay focused!",
        "A glass of water will do you good right now."
    };


    // -------------------------------------------------------
    // CONSTRUCTOR
    // -------------------------------------------------------
    public GetItDone() {
        setTitle("Get It Done - Stay Productive!");
        setSize(780, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(248, 250, 255));

        mainPanel.add(buildTopBar(), BorderLayout.NORTH);
        mainPanel.add(buildTabs(),   BorderLayout.CENTER);

        add(mainPanel);

        loadCompletedTasksFromFile();
        startReminderTimer();
    }


    // -------------------------------------------------------
    // TOP BAR
    // -------------------------------------------------------
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        topBar.setBackground(new Color(45, 95, 155));
        topBar.setBorder(new EmptyBorder(4, 10, 4, 10));

        JLabel appTitle = new JLabel("  Get It Done");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appTitle.setForeground(Color.WHITE);

        goalProgressLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        goalProgressLabel.setForeground(new Color(180, 230, 255));

        topBar.add(appTitle);
        topBar.add(makeSpacer(30));
        topBar.add(goalProgressLabel);

        return topBar;
    }


    // -------------------------------------------------------
    // TABS
    // -------------------------------------------------------
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(new Color(248, 250, 255));

        tabs.addTab("  My Tasks  ",        buildTasksTab());
        tabs.addTab("  Goals  ",           buildGoalsTab());
        tabs.addTab("  Completed Tasks  ", buildCompletedTab());

        return tabs;
    }


    // -------------------------------------------------------
    // TASKS TAB
    // -------------------------------------------------------
    private JPanel buildTasksTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(new Color(248, 250, 255));

        JList<String> taskListDisplay = new JList<String>(taskListModel);
        taskListDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taskListDisplay.setSelectionBackground(new Color(80, 130, 210));
        taskListDisplay.setSelectionForeground(Color.WHITE);
        taskListDisplay.setBorder(new EmptyBorder(6, 10, 6, 10));

        JScrollPane taskScrollPane = new JScrollPane(taskListDisplay);
        taskScrollPane.setBorder(makeTitledBorder("  Your Tasks  ", new Color(45, 95, 155)));

        // ---- add task form ----
        JPanel addTaskForm = new JPanel(new GridBagLayout());
        addTaskForm.setBackground(new Color(235, 243, 255));
        addTaskForm.setBorder(makeTitledBorder("  Add a New Task  ", new Color(45, 95, 155)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill   = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JTextField taskNameInput = new JTextField(24);
        taskNameInput.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // date + time spinner for the deadline
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner deadlineSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(deadlineSpinner, "MMM dd, yyyy  hh:mm a");
        deadlineSpinner.setEditor(dateEditor);
        deadlineSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel hintLbl = new JLabel("Click on the date or time, then use the arrows to change it");
        hintLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLbl.setForeground(Color.GRAY);

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        addTaskForm.add(makeLabel("Task name:"), c);
        c.gridx = 1; c.weightx = 1.0;
        addTaskForm.add(taskNameInput, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        addTaskForm.add(makeLabel("Deadline:"), c);
        c.gridx = 1; c.weightx = 1.0;
        addTaskForm.add(deadlineSpinner, c);

        c.gridx = 1; c.gridy = 2;
        addTaskForm.add(hintLbl, c);

        // ---- buttons ----
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttonsPanel.setBackground(new Color(248, 250, 255));

        JButton addBtn      = makeButton("  Add Task  ",      new Color(45, 95, 155), Color.WHITE);
        JButton deleteBtn   = makeButton("  Delete  ",        new Color(190, 65, 65),  Color.WHITE);
        JButton completeBtn = makeButton("  Mark Complete  ", new Color(45, 150, 70),  Color.WHITE);

        buttonsPanel.add(addBtn);
        buttonsPanel.add(deleteBtn);
        buttonsPanel.add(completeBtn);

        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = taskNameInput.getText().trim();

                if (name.equals("")) {
                    JOptionPane.showMessageDialog(null,
                        "Please enter a task name!", "Oops!", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Date selectedDate = (Date) deadlineSpinner.getValue();
                SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy  hh:mm a");
                String deadline = fmt.format(selectedDate);

                Task newTask = new Task(name, deadline);
                myTasks.add(newTask);
                taskListModel.addElement(newTask.toString());

                taskNameInput.setText("");
                taskNameInput.requestFocus();
            }
        });

        deleteBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = taskListDisplay.getSelectedIndex();

                if (index == -1) {
                    JOptionPane.showMessageDialog(null,
                        "Please click on a task first, then press Delete.",
                        "Nothing selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int answer = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to delete this task?",
                    "Delete task?", JOptionPane.YES_NO_OPTION);

                if (answer == JOptionPane.YES_OPTION) {
                    myTasks.remove(index);
                    taskListModel.remove(index);
                }
            }
        });

        completeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = taskListDisplay.getSelectedIndex();

                if (index == -1) {
                    JOptionPane.showMessageDialog(null,
                        "Please click on a task first, then press Mark Complete.",
                        "Nothing selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Task t = myTasks.get(index);

                saveTaskToFile(t);
                completedListModel.addElement("[DONE]  " + t.name + "  |  Was due: " + t.deadline);

                myTasks.remove(index);
                taskListModel.remove(index);

                tasksFinished++;
                updateStatsDisplay();

                // pick the next message in rotation and show it
                String msg = completionMessages[completionMsgIndex % completionMessages.length];
                completionMsgIndex++;

                // check for milestone messages - these override the regular rotation
                String milestoneMsg = getMilestoneMessage();
                if (!milestoneMsg.equals("")) {
                    msg = milestoneMsg;
                }

                JOptionPane.showMessageDialog(null,
                    msg, "Task Complete!", JOptionPane.INFORMATION_MESSAGE);

                // also check if the daily goal was just hit
                checkGoal();
            }
        });

        JPanel bottomHalf = new JPanel(new BorderLayout());
        bottomHalf.setBackground(new Color(248, 250, 255));
        bottomHalf.add(addTaskForm,  BorderLayout.CENTER);
        bottomHalf.add(buttonsPanel, BorderLayout.SOUTH);

        panel.add(taskScrollPane, BorderLayout.CENTER);
        panel.add(bottomHalf,     BorderLayout.SOUTH);

        return panel;
    }


    // -------------------------------------------------------
    // GOALS TAB
    // -------------------------------------------------------
    private JPanel buildGoalsTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(18, 22, 18, 22));
        panel.setBackground(new Color(248, 250, 255));

        // --- set goal ---
        JLabel setGoalTitle = makeSectionTitle("Set Your Daily Study Goal");

        JLabel setGoalDesc = makeLabel("How many tasks do you want to complete today?");
        setGoalDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel setGoalRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        setGoalRow.setBackground(new Color(248, 250, 255));
        setGoalRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField goalInput = new JTextField("5", 5);
        goalInput.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JButton setGoalBtn = makeButton("  Set Goal  ", new Color(45, 95, 155), Color.WHITE);

        setGoalRow.add(makeLabel("Number of tasks: "));
        setGoalRow.add(goalInput);
        setGoalRow.add(setGoalBtn);

        // --- progress ---
        JLabel progressTitle = makeSectionTitle("Your Progress Today");

        goalProgressBar.setValue(tasksFinished);
        goalProgressBar.setStringPainted(true);
        goalProgressBar.setString(tasksFinished + " / " + studyGoal + " tasks completed");
        goalProgressBar.setForeground(new Color(45, 150, 70));
        goalProgressBar.setBackground(new Color(220, 235, 220));
        goalProgressBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        goalProgressBar.setMaximumSize(new Dimension(520, 28));
        goalProgressBar.setPreferredSize(new Dimension(400, 28));
        goalProgressBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- achievements ---
        JLabel achievementsTitle = makeSectionTitle("Your Achievements");

        achievementsArea = new JTextArea(10, 40);
        achievementsArea.setEditable(false);
        achievementsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        achievementsArea.setBackground(new Color(245, 255, 245));
        achievementsArea.setLineWrap(true);
        achievementsArea.setWrapStyleWord(true);
        achievementsArea.setBorder(new EmptyBorder(12, 14, 12, 14));
        updateAchievementsDisplay();

        JScrollPane achievementsScroll = new JScrollPane(achievementsArea);
        achievementsScroll.setBorder(BorderFactory.createLineBorder(new Color(100, 180, 120), 1));
        achievementsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        achievementsScroll.setMaximumSize(new Dimension(560, 240));

        setGoalBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int newGoal = Integer.parseInt(goalInput.getText().trim());
                    if (newGoal < 1) {
                        JOptionPane.showMessageDialog(null,
                            "Your goal must be at least 1 task!", "Too small", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    studyGoal = newGoal;
                    updateStatsDisplay();
                    JOptionPane.showMessageDialog(null,
                        "Goal set to " + studyGoal + " tasks!  You can do it!",
                        "Goal Updated!", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null,
                        "Please enter a whole number (like 3 or 10).",
                        "Invalid input", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(setGoalTitle);
        panel.add(Box.createVerticalStrut(6));
        panel.add(setGoalDesc);
        panel.add(Box.createVerticalStrut(8));
        panel.add(setGoalRow);
        panel.add(Box.createVerticalStrut(22));
        panel.add(progressTitle);
        panel.add(Box.createVerticalStrut(8));
        panel.add(goalProgressBar);
        panel.add(Box.createVerticalStrut(22));
        panel.add(achievementsTitle);
        panel.add(Box.createVerticalStrut(8));
        panel.add(achievementsScroll);

        return panel;
    }


    // -------------------------------------------------------
    // COMPLETED TASKS TAB
    // -------------------------------------------------------
    private JPanel buildCompletedTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(new Color(248, 250, 255));

        JList<String> completedDisplay = new JList<String>(completedListModel);
        completedDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        completedDisplay.setBackground(new Color(242, 255, 242));
        completedDisplay.setForeground(new Color(30, 100, 30));
        completedDisplay.setBorder(new EmptyBorder(6, 10, 6, 10));

        JScrollPane scrollPane = new JScrollPane(completedDisplay);
        scrollPane.setBorder(makeTitledBorder("  All Completed Tasks  ", new Color(45, 150, 70)));

        JLabel noteLabel = new JLabel(
            "  These are saved in 'completed_tasks.txt' so you can always look back at what you accomplished!");
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        noteLabel.setForeground(new Color(60, 120, 60));
        noteLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(noteLabel,  BorderLayout.SOUTH);

        return panel;
    }


    // -------------------------------------------------------
    // REMINDER TIMER - runs silently in the background
    // -------------------------------------------------------
    private void startReminderTimer() {
        Timer minuteTimer = new Timer(60000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                breakMinutesLeft--;
                waterMinutesLeft--;

                if (breakMinutesLeft <= 0) {
                    breakMinutesLeft = 25;
                    String msg = breakMessages[breakMsgIndex % breakMessages.length];
                    breakMsgIndex++;
                    showToastNotification(msg, new Color(70, 120, 200));
                }

                if (waterMinutesLeft <= 0) {
                    waterMinutesLeft = 60;
                    String msg = waterMessages[waterMsgIndex % waterMessages.length];
                    waterMsgIndex++;
                    showToastNotification(msg, new Color(30, 150, 180));
                }
            }
        });

        minuteTimer.start();
    }

    // small banner that appears at the top of the window and disappears after 5 seconds
    private void showToastNotification(String message, Color bgColor) {
        JWindow toast = new JWindow(this);

        JPanel toastPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10));
        toastPanel.setBackground(bgColor);
        toastPanel.setBorder(BorderFactory.createLineBorder(bgColor.darker(), 1));

        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        msgLabel.setForeground(Color.WHITE);

        toastPanel.add(msgLabel);
        toast.add(toastPanel);
        toast.pack();

        int x = getX() + (getWidth()  - toast.getWidth())  / 2;
        int y = getY() + 58;
        toast.setLocation(x, y);
        toast.setVisible(true);

        Timer closeTimer = new Timer(5000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toast.dispose();
            }
        });
        closeTimer.setRepeats(false);
        closeTimer.start();
    }


    // -------------------------------------------------------
    // STATS & ACHIEVEMENTS
    // -------------------------------------------------------
    private void updateStatsDisplay() {
        goalProgressLabel.setText("  Goal: " + tasksFinished + " / " + studyGoal + " tasks");

        goalProgressBar.setMaximum(studyGoal);
        goalProgressBar.setValue(tasksFinished);
        goalProgressBar.setString(tasksFinished + " / " + studyGoal + " tasks completed");

        updateAchievementsDisplay();
    }

    // returns a special message at task milestones, or empty string if no milestone
    private String getMilestoneMessage() {
        if (tasksFinished == 1)  return "Great start! The first one is always the hardest!";
        if (tasksFinished == 3)  return "3 tasks done! You're on a roll!";
        if (tasksFinished == 5)  return "5 tasks finished! You're having a really productive day!";
        if (tasksFinished == 10) return "10 tasks! Wow, you are seriously crushing it today!";
        if (tasksFinished == 15) return "15 tasks! That's an incredible amount of work. Be proud!";
        if (tasksFinished == 20) return "20 tasks! You are absolutely unstoppable!";
        return "";  // no milestone, use the regular rotation
    }

    private void checkGoal() {
        if (tasksFinished == studyGoal) {
            goalsCompleted++;
            updateAchievementsDisplay();

            String message = "YOU HIT YOUR GOAL!\n\n" +
                "You completed all " + studyGoal + " tasks for today.\n" +
                "That took real effort - you should be proud!\n\n" +
                "Total tasks finished today: " + tasksFinished;

            if (goalsCompleted >= 3) {
                message += "\n\nYou've hit your daily goal " + goalsCompleted
                    + " times now!\nYou are building an amazing study habit!";
            }

            JOptionPane.showMessageDialog(null, message,
                "Goal Reached!", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateAchievementsDisplay() {
        if (achievementsArea == null) {
            return;
        }

        // build up a summary of what the user has accomplished
        StringBuilder sb = new StringBuilder();

        sb.append("TASKS COMPLETED:  ").append(tasksFinished).append("\n");
        sb.append("DAILY GOALS HIT:  ").append(goalsCompleted).append("\n\n");

        // show a message that reflects their current progress
        if (tasksFinished == 0) {
            sb.append("Add your first task and get started!\n");
            sb.append("You've got this.\n");
        } else if (tasksFinished < 3) {
            sb.append("Good job studying! You're off to a solid start.\n");
            sb.append("Keep going - momentum builds quickly!\n");
        } else if (tasksFinished < 5) {
            sb.append("You're doing great! Every task you finish\n");
            sb.append("is proof that you're putting in the work.\n");
        } else if (tasksFinished < 10) {
            sb.append("Impressive work! You're clearly focused today.\n");
            sb.append("This kind of effort really pays off over time.\n");
        } else if (tasksFinished < 15) {
            sb.append("10+ tasks? That's seriously productive.\n");
            sb.append("You should feel really good about today's work!\n");
        } else {
            sb.append("You are absolutely crushing it.\n");
            sb.append("The amount of work you're putting in is incredible.\n");
            sb.append("Keep this energy going!\n");
        }

        sb.append("\n--- MILESTONES REACHED ---\n");
        if (tasksFinished >= 1)  sb.append("[x]  First task completed\n");
        if (tasksFinished >= 3)  sb.append("[x]  3 tasks in a session\n");
        if (tasksFinished >= 5)  sb.append("[x]  5 tasks in a session\n");
        if (tasksFinished >= 10) sb.append("[x]  10 tasks in a session\n");
        if (tasksFinished >= 15) sb.append("[x]  15 tasks in a session\n");
        if (tasksFinished >= 20) sb.append("[x]  20 tasks in a session\n");
        if (goalsCompleted >= 1) sb.append("[x]  Hit your daily goal\n");
        if (goalsCompleted >= 3) sb.append("[x]  Hit your daily goal 3 times\n");
        if (goalsCompleted >= 5) sb.append("[x]  Hit your daily goal 5 times\n");

        achievementsArea.setText(sb.toString());
    }


    // -------------------------------------------------------
    // FILE OPERATIONS
    // -------------------------------------------------------
    private void saveTaskToFile(Task t) {
        try {
            FileWriter fw     = new FileWriter("completed_tasks.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("Task: " + t.name + "  |  Deadline was: " + t.deadline);
            bw.newLine();
            bw.close();
            fw.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "Couldn't save to file: " + e.getMessage(),
                "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCompletedTasksFromFile() {
        File file = new File("completed_tasks.txt");
        if (!file.exists()) {
            return;
        }

        try {
            FileReader fr     = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().equals("")) {
                    completedListModel.addElement("[DONE]  " + line);
                }
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "Couldn't load completed tasks: " + e.getMessage(),
                "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // -------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------
    private JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return label;
    }

    private JLabel makeSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(new Color(45, 95, 155));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private TitledBorder makeTitledBorder(String title, Color color) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(color, 1),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            color
        );
    }

    private Component makeSpacer(int width) {
        return Box.createHorizontalStrut(width);
    }


    // -------------------------------------------------------
    // MAIN METHOD
    // -------------------------------------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GetItDone app = new GetItDone();
                app.setVisible(true);
            }
        });
    }
}
