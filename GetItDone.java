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

    // keeping track of points and goals
    int totalPoints = 0;
    int studyGoal = 5;         // default goal: complete 5 tasks
    int tasksFinished = 0;
    int goalsCompleted = 0;    // for the "on fire" badge

    // --- GUI components that need to be updated later ---

    // list models let me add/remove items from a JList
    DefaultListModel<String> taskListModel = new DefaultListModel<String>();
    DefaultListModel<String> completedListModel = new DefaultListModel<String>();

    // labels in the top bar
    JLabel pointsLabel       = new JLabel("  Points: 0");
    JLabel goalProgressLabel = new JLabel("  Goal: 0 / 5 tasks");

    // bottom reminder label
    JLabel reminderLabel = new JLabel("  Next break in: 25 min   |   Next water reminder in: 60 min");

    // progress bar on the goals tab
    JProgressBar goalProgressBar = new JProgressBar(0, 5);

    // text area on goals tab that shows badge status
    JTextArea badgeStatusArea;

    // countdown in minutes for reminders
    int breakMinutesLeft = 25;
    int waterMinutesLeft = 60;

    // --- reward / gift card system ---

    // tracks how many rewards the user has earned but not claimed yet
    int unclaimedRewards = 0;

    // tracks which point tiers have already given out a reward (so they don't get it twice)
    boolean reward50given  = false;
    boolean reward100given = false;
    boolean reward200given = false;
    boolean reward300given = false;

    // label that shows the claim button and notice
    JLabel rewardNoticeLabel = new JLabel("  No rewards to claim yet. Keep completing tasks!");
    JButton claimRewardBtn   = makeButton("  Claim Reward  ", new Color(180, 110, 0), Color.WHITE);


    // -------------------------------------------------------
    // CONSTRUCTOR - this builds the whole window
    // -------------------------------------------------------
    public GetItDone() {
        setTitle("Get It Done - Stay Productive!");
        setSize(780, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // center the window on screen

        // main container for everything
        JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(248, 250, 255));

        // build the different sections
        JPanel topBar    = buildTopBar();
        JTabbedPane tabs = buildTabs();
        JPanel bottomBar = buildBottomBar();

        mainPanel.add(topBar,    BorderLayout.NORTH);
        mainPanel.add(tabs,      BorderLayout.CENTER);
        mainPanel.add(bottomBar, BorderLayout.SOUTH);

        add(mainPanel);

        // load any tasks we saved last time the app was open
        loadCompletedTasksFromFile();

        // start the timer that reminds us to take breaks and drink water
        startReminderTimer();
    }


    // -------------------------------------------------------
    // TOP BAR - shows the app name, points, and goal progress
    // -------------------------------------------------------
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        topBar.setBackground(new Color(45, 95, 155));
        topBar.setBorder(new EmptyBorder(4, 10, 4, 10));

        JLabel appTitle = new JLabel("  Get It Done");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appTitle.setForeground(Color.WHITE);

        pointsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pointsLabel.setForeground(new Color(255, 230, 80));

        goalProgressLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        goalProgressLabel.setForeground(new Color(180, 230, 255));

        topBar.add(appTitle);
        topBar.add(makeSpacer(30));
        topBar.add(pointsLabel);
        topBar.add(makeSpacer(20));
        topBar.add(goalProgressLabel);

        return topBar;
    }


    // -------------------------------------------------------
    // TABS - the three main sections of the app
    // -------------------------------------------------------
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(new Color(248, 250, 255));

        tabs.addTab("  My Tasks  ",        buildTasksTab());
        tabs.addTab("  Goals & Rewards  ", buildGoalsTab());
        tabs.addTab("  Completed Tasks  ", buildCompletedTab());

        return tabs;
    }


    // -------------------------------------------------------
    // TASKS TAB - add, view, delete, and complete tasks
    // -------------------------------------------------------
    private JPanel buildTasksTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(new Color(248, 250, 255));

        // the list that shows all current tasks
        JList<String> taskListDisplay = new JList<String>(taskListModel);
        taskListDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taskListDisplay.setSelectionBackground(new Color(80, 130, 210));
        taskListDisplay.setSelectionForeground(Color.WHITE);
        taskListDisplay.setBorder(new EmptyBorder(6, 10, 6, 10));

        JScrollPane taskScrollPane = new JScrollPane(taskListDisplay);
        taskScrollPane.setBorder(makeTitledBorder("  Your Tasks  ", new Color(45, 95, 155)));

        // ---- form to add a new task ----
        JPanel addTaskForm = new JPanel(new GridBagLayout());
        addTaskForm.setBackground(new Color(235, 243, 255));
        addTaskForm.setBorder(makeTitledBorder("  Add a New Task  ", new Color(45, 95, 155)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets  = new Insets(6, 8, 6, 8);
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.anchor  = GridBagConstraints.WEST;

        JTextField taskNameInput = new JTextField(24);
        taskNameInput.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // date+time spinner - lets the user pick a deadline from a calendar-style picker
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner deadlineSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(deadlineSpinner, "MMM dd, yyyy  hh:mm a");
        deadlineSpinner.setEditor(dateEditor);
        deadlineSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel nameLbl     = makeLabel("Task name:");
        JLabel deadlineLbl = makeLabel("Deadline:");
        JLabel hintLbl     = new JLabel("Use the arrows to change the date and time");
        hintLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLbl.setForeground(Color.GRAY);

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        addTaskForm.add(nameLbl, c);
        c.gridx = 1; c.weightx = 1.0;
        addTaskForm.add(taskNameInput, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        addTaskForm.add(deadlineLbl, c);
        c.gridx = 1; c.weightx = 1.0;
        addTaskForm.add(deadlineSpinner, c);

        c.gridx = 1; c.gridy = 2;
        addTaskForm.add(hintLbl, c);

        // ---- buttons ----
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttonsPanel.setBackground(new Color(248, 250, 255));

        JButton addBtn      = makeButton("  Add Task  ",      new Color(45, 95, 155),  Color.WHITE);
        JButton deleteBtn   = makeButton("  Delete  ",        new Color(190, 65, 65),   Color.WHITE);
        JButton completeBtn = makeButton("  Mark Complete  ", new Color(45, 150, 70),   Color.WHITE);

        buttonsPanel.add(addBtn);
        buttonsPanel.add(deleteBtn);
        buttonsPanel.add(completeBtn);

        // ---- button actions ----

        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = taskNameInput.getText().trim();

                if (name.equals("")) {
                    JOptionPane.showMessageDialog(null,
                        "Please enter a task name!",
                        "Oops!", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // format the date from the spinner into a readable string
                Date selectedDate = (Date) deadlineSpinner.getValue();
                SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy  hh:mm a");
                String deadline = formatter.format(selectedDate);

                Task newTask = new Task(name, deadline);
                myTasks.add(newTask);
                taskListModel.addElement(newTask.toString());

                // clear the name field and put the cursor back in it
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

                // save the task to the file so it's not forgotten
                saveTaskToFile(t);

                // add it to the completed list display
                completedListModel.addElement("[DONE]  " + t.name + "  |  Was due: " + t.deadline);

                // remove from the active task list
                myTasks.remove(index);
                taskListModel.remove(index);

                // reward the user with points
                totalPoints += 10;
                tasksFinished++;
                updateStatsDisplay();

                // check if they just hit their goal or earned a badge
                checkForGoalAndBadges();

                JOptionPane.showMessageDialog(null,
                    "Awesome! You completed:\n\"" + t.name + "\"\n\n+10 points!  Keep it up!",
                    "Task Complete!", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // put the bottom half of the tab together
        JPanel bottomHalf = new JPanel(new BorderLayout());
        bottomHalf.setBackground(new Color(248, 250, 255));
        bottomHalf.add(addTaskForm,  BorderLayout.CENTER);
        bottomHalf.add(buttonsPanel, BorderLayout.SOUTH);

        panel.add(taskScrollPane, BorderLayout.CENTER);
        panel.add(bottomHalf,     BorderLayout.SOUTH);

        return panel;
    }


    // -------------------------------------------------------
    // GOALS TAB - set a study goal, track progress, see rewards
    // -------------------------------------------------------
    private JPanel buildGoalsTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(18, 22, 18, 22));
        panel.setBackground(new Color(248, 250, 255));

        // --- set goal section ---
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

        // --- progress section ---
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

        // --- rewards section ---
        JLabel rewardsTitle = makeSectionTitle("Points & Badges");

        // this text area shows what badges they can earn (and which they've got)
        badgeStatusArea = new JTextArea(8, 40);
        badgeStatusArea.setEditable(false);
        badgeStatusArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        badgeStatusArea.setBackground(new Color(255, 253, 220));
        badgeStatusArea.setLineWrap(true);
        badgeStatusArea.setWrapStyleWord(true);
        badgeStatusArea.setBorder(new EmptyBorder(12, 14, 12, 14));
        updateBadgeDisplay();   // fill in the badge status right away

        JScrollPane badgeScroll = new JScrollPane(badgeStatusArea);
        badgeScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 200, 100), 1));
        badgeScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        badgeScroll.setMaximumSize(new Dimension(560, 200));

        // set goal button action
        setGoalBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String input = goalInput.getText().trim();
                try {
                    int newGoal = Integer.parseInt(input);
                    if (newGoal < 1) {
                        JOptionPane.showMessageDialog(null,
                            "Your goal must be at least 1 task!",
                            "Too small", JOptionPane.WARNING_MESSAGE);
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

        // --- reward claim section ---
        JLabel claimTitle = makeSectionTitle("Gift Card Rewards");

        JLabel claimDesc = makeLabel("Earn points to unlock gift card rewards. Show proof to claim them!");
        claimDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        // reward tiers info
        JPanel tiersPanel = new JPanel();
        tiersPanel.setLayout(new BoxLayout(tiersPanel, BoxLayout.Y_AXIS));
        tiersPanel.setBackground(new Color(240, 248, 255));
        tiersPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(45, 95, 155), 1),
            new EmptyBorder(10, 14, 10, 14)
        ));
        tiersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tiersPanel.setMaximumSize(new Dimension(560, 110));

        String[] tiers = {
            "50 pts   ->   Level 1 Reward  (show to parent/teacher to claim)",
            "100 pts  ->   Level 2 Reward  (show to parent/teacher to claim)",
            "200 pts  ->   Level 3 Reward  (show to parent/teacher to claim)",
            "300 pts  ->   Grand Reward    (show to parent/teacher to claim)"
        };
        for (String tier : tiers) {
            JLabel tierLabel = makeLabel(tier);
            tierLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            tiersPanel.add(tierLabel);
        }

        // notice + claim button row
        JPanel claimRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        claimRow.setBackground(new Color(248, 250, 255));
        claimRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        rewardNoticeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        rewardNoticeLabel.setForeground(new Color(150, 90, 0));
        claimRewardBtn.setEnabled(false);  // disabled until a reward is available

        claimRow.add(claimRewardBtn);
        claimRow.add(rewardNoticeLabel);

        claimRewardBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showRewardClaimDialog();
            }
        });

        // stack everything vertically
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
        panel.add(rewardsTitle);
        panel.add(Box.createVerticalStrut(8));
        panel.add(badgeScroll);
        panel.add(Box.createVerticalStrut(22));
        panel.add(claimTitle);
        panel.add(Box.createVerticalStrut(6));
        panel.add(claimDesc);
        panel.add(Box.createVerticalStrut(8));
        panel.add(tiersPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(claimRow);

        return panel;
    }


    // -------------------------------------------------------
    // COMPLETED TASKS TAB - shows everything you've done
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
    // BOTTOM BAR - shows reminder countdown timers
    // -------------------------------------------------------
    private JPanel buildBottomBar() {
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(new Color(220, 245, 215));
        bottomBar.setBorder(new EmptyBorder(6, 10, 6, 10));

        reminderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reminderLabel.setForeground(new Color(25, 95, 25));

        bottomBar.add(reminderLabel, BorderLayout.WEST);

        return bottomBar;
    }


    // -------------------------------------------------------
    // REMINDER TIMER - fires every minute
    // -------------------------------------------------------
    private void startReminderTimer() {
        // this timer ticks once every 60 seconds (1 minute)
        Timer minuteTimer = new Timer(60000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                breakMinutesLeft--;
                waterMinutesLeft--;

                // time for a break?
                if (breakMinutesLeft <= 0) {
                    breakMinutesLeft = 25;  // reset the 25-minute countdown
                    JOptionPane.showMessageDialog(null,
                        "TIME FOR A BREAK!\n\n" +
                        "You've been working for 25 minutes.\n" +
                        "Stand up, stretch, and rest your eyes for 5 minutes.\n\n" +
                        "Your brain will thank you!",
                        "Break Time!", JOptionPane.INFORMATION_MESSAGE);
                }

                // time to drink water?
                if (waterMinutesLeft <= 0) {
                    waterMinutesLeft = 60;  // reset the 60-minute countdown
                    JOptionPane.showMessageDialog(null,
                        "DRINK SOME WATER!\n\n" +
                        "You've been working for an hour.\n" +
                        "Staying hydrated helps you focus and feel better!\n\n" +
                        "Go grab a glass now!",
                        "Hydration Reminder!", JOptionPane.INFORMATION_MESSAGE);
                }

                // update the countdown display at the bottom of the app
                reminderLabel.setText(
                    "  Next break in: " + breakMinutesLeft +
                    " min   |   Next water reminder in: " + waterMinutesLeft + " min");
            }
        });

        minuteTimer.start();
    }


    // -------------------------------------------------------
    // STATS & GOALS - update the display after something changes
    // -------------------------------------------------------
    private void updateStatsDisplay() {
        // update the top bar labels
        pointsLabel.setText("  Points: " + totalPoints);
        goalProgressLabel.setText("  Goal: " + tasksFinished + " / " + studyGoal + " tasks");

        // update the progress bar on the goals tab
        goalProgressBar.setMaximum(studyGoal);
        goalProgressBar.setValue(tasksFinished);
        goalProgressBar.setString(tasksFinished + " / " + studyGoal + " tasks completed");

        // refresh the badge area too
        updateBadgeDisplay();
    }

    private void checkForGoalAndBadges() {
        // did they just hit their daily goal?
        if (tasksFinished == studyGoal) {
            goalsCompleted++;
            String message = "YOU HIT YOUR GOAL!\n\n" +
                "You completed all " + studyGoal + " tasks for today!\n" +
                "That's amazing work - you earned the Goal Crusher badge!\n\n" +
                "Current points: " + totalPoints;

            if (goalsCompleted >= 3) {
                message += "\n\nYou've hit your goal " + goalsCompleted + " times! ON FIRE badge unlocked!";
            }

            JOptionPane.showMessageDialog(null, message, "GOAL REACHED!", JOptionPane.INFORMATION_MESSAGE);
        }

        // point milestone badges + rewards
        if (totalPoints >= 10 && totalPoints < 20) {
            JOptionPane.showMessageDialog(null,
                "Badge unlocked:  TASK STARTER\n\nYou completed your first task!",
                "New Badge!", JOptionPane.INFORMATION_MESSAGE);
        }

        // check if a new reward tier was just crossed
        if (totalPoints >= 50 && !reward50given) {
            reward50given = true;
            unclaimedRewards++;
            updateRewardNotice();
            JOptionPane.showMessageDialog(null,
                "Badge unlocked:  GETTING THINGS DONE\n\n" +
                "You've earned 50 points!\n\n" +
                "A Level 1 Reward is waiting for you!\n" +
                "Go to Goals & Rewards -> Claim Reward to get your gift card!",
                "New Badge + Reward Unlocked!", JOptionPane.INFORMATION_MESSAGE);
        }

        if (totalPoints >= 100 && !reward100given) {
            reward100given = true;
            unclaimedRewards++;
            updateRewardNotice();
            JOptionPane.showMessageDialog(null,
                "Badge unlocked:  PRODUCTIVITY PRO\n\n" +
                "You've earned 100 points!\n\n" +
                "A Level 2 Reward is waiting for you!\n" +
                "Go to Goals & Rewards -> Claim Reward to get your gift card!",
                "New Badge + Reward Unlocked!", JOptionPane.INFORMATION_MESSAGE);
        }

        if (totalPoints >= 200 && !reward200given) {
            reward200given = true;
            unclaimedRewards++;
            updateRewardNotice();
            JOptionPane.showMessageDialog(null,
                "Badge unlocked:  UNSTOPPABLE\n\n" +
                "You've earned 200 points!\n\n" +
                "A Level 3 Reward is waiting for you!\n" +
                "Go to Goals & Rewards -> Claim Reward to get your gift card!",
                "New Badge + Reward Unlocked!", JOptionPane.INFORMATION_MESSAGE);
        }

        if (totalPoints >= 300 && !reward300given) {
            reward300given = true;
            unclaimedRewards++;
            updateRewardNotice();
            JOptionPane.showMessageDialog(null,
                "Badge unlocked:  LEGENDARY\n\n" +
                "You've earned 300 points! That's incredible!\n\n" +
                "The Grand Reward is waiting for you!\n" +
                "Go to Goals & Rewards -> Claim Reward to get your gift card!",
                "Grand Reward Unlocked!", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateBadgeDisplay() {
        String locked   = "[ ]  ";
        String unlocked = "[X]  ";

        String text =
            "HOW TO EARN POINTS:\n" +
            "  Complete any task = +10 points\n\n" +
            "BADGES:\n" +
            (totalPoints >= 10  ? unlocked : locked) + "Task Starter          -  Earn 10 points\n" +
            (totalPoints >= 50  ? unlocked : locked) + "Getting Things Done   -  Earn 50 points  (+Level 1 Reward)\n" +
            (totalPoints >= 100 ? unlocked : locked) + "Productivity Pro      -  Earn 100 points (+Level 2 Reward)\n" +
            (totalPoints >= 200 ? unlocked : locked) + "Unstoppable           -  Earn 200 points (+Level 3 Reward)\n" +
            (totalPoints >= 300 ? unlocked : locked) + "Legendary             -  Earn 300 points (+Grand Reward)\n" +
            (goalsCompleted >= 1 ? unlocked : locked) + "Goal Crusher          -  Complete your daily goal\n" +
            (goalsCompleted >= 3 ? unlocked : locked) + "On Fire               -  Complete your daily goal 3 times\n\n" +
            "Current points:   " + totalPoints + "\n" +
            "Tasks finished:   " + tasksFinished + "\n" +
            "Goals completed:  " + goalsCompleted + "\n" +
            "Rewards to claim: " + unclaimedRewards;

        if (badgeStatusArea != null) {
            badgeStatusArea.setText(text);
        }
    }

    private void updateRewardNotice() {
        if (unclaimedRewards > 0) {
            rewardNoticeLabel.setText("  You have " + unclaimedRewards + " reward(s) ready to claim!");
            rewardNoticeLabel.setForeground(new Color(160, 80, 0));
            claimRewardBtn.setEnabled(true);
        } else {
            rewardNoticeLabel.setText("  No rewards to claim yet. Keep completing tasks!");
            rewardNoticeLabel.setForeground(new Color(100, 100, 100));
            claimRewardBtn.setEnabled(false);
        }
    }


    // -------------------------------------------------------
    // REWARD CLAIM DIALOG - shows the claim slip with proof
    // -------------------------------------------------------
    private void showRewardClaimDialog() {
        if (unclaimedRewards <= 0) {
            return;
        }

        // figure out which reward level this claim is for (highest unlocked tier)
        String rewardLevel;
        if (reward300given && totalPoints >= 300) {
            rewardLevel = "Grand Reward (300+ pts)";
        } else if (reward200given && totalPoints >= 200) {
            rewardLevel = "Level 3 Reward (200+ pts)";
        } else if (reward100given && totalPoints >= 100) {
            rewardLevel = "Level 2 Reward (100+ pts)";
        } else {
            rewardLevel = "Level 1 Reward (50+ pts)";
        }

        // generate a unique claim code using the date and their stats
        SimpleDateFormat codeFmt = new SimpleDateFormat("yyyyMMdd");
        String today = codeFmt.format(new Date());
        String claimCode = "GID-" + String.format("%03d", totalPoints)
                         + "-" + String.format("%02d", tasksFinished)
                         + "-" + today;

        // build the proof text - list all completed tasks from the list model
        StringBuilder proofBuilder = new StringBuilder();
        proofBuilder.append("COMPLETED TASKS AS PROOF:\n");
        proofBuilder.append("-------------------------------\n");
        if (completedListModel.getSize() == 0) {
            proofBuilder.append("(No completed tasks recorded in this session)\n");
        } else {
            for (int i = 0; i < completedListModel.getSize(); i++) {
                proofBuilder.append(completedListModel.getElementAt(i)).append("\n");
            }
        }
        proofBuilder.append("-------------------------------\n");
        proofBuilder.append("Total points: ").append(totalPoints).append("\n");
        proofBuilder.append("Total tasks finished: ").append(tasksFinished).append("\n");

        // build the full claim slip dialog
        JPanel claimPanel = new JPanel();
        claimPanel.setLayout(new BoxLayout(claimPanel, BoxLayout.Y_AXIS));
        claimPanel.setBackground(new Color(255, 252, 230));
        claimPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel congrats = new JLabel("CONGRATULATIONS! You earned a reward!");
        congrats.setFont(new Font("Segoe UI", Font.BOLD, 15));
        congrats.setForeground(new Color(150, 80, 0));
        congrats.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel levelLbl = new JLabel("Reward: " + rewardLevel);
        levelLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        levelLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel codeLbl = new JLabel("Your Claim Code:");
        codeLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        codeLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // the actual claim code displayed in a text field so they can copy it
        JTextField codeField = new JTextField(claimCode);
        codeField.setFont(new Font("Consolas", Font.BOLD, 16));
        codeField.setEditable(false);
        codeField.setBackground(new Color(255, 245, 200));
        codeField.setBorder(BorderFactory.createLineBorder(new Color(200, 150, 0), 2));
        codeField.setAlignmentX(Component.LEFT_ALIGNMENT);
        codeField.setMaximumSize(new Dimension(400, 36));

        JLabel instructionLbl = new JLabel("HOW TO CLAIM YOUR GIFT CARD:");
        instructionLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        instructionLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea instructions = new JTextArea(
            "1. Show THIS SCREEN to your parent or teacher.\n" +
            "2. Show them your completed_tasks.txt file as proof.\n" +
            "   (It's in the same folder where you run the app.)\n" +
            "3. They will verify your work and give you your reward!\n\n" +
            "Your claim code is unique to you - it includes your points,\n" +
            "tasks completed, and today's date."
        );
        instructions.setEditable(false);
        instructions.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instructions.setBackground(new Color(255, 252, 230));
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel proofTitle = new JLabel("YOUR PROOF OF COMPLETION:");
        proofTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        proofTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea proofArea = new JTextArea(proofBuilder.toString(), 6, 40);
        proofArea.setEditable(false);
        proofArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        proofArea.setBackground(new Color(242, 255, 242));
        proofArea.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane proofScroll = new JScrollPane(proofArea);
        proofScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        proofScroll.setMaximumSize(new Dimension(500, 130));

        claimPanel.add(congrats);
        claimPanel.add(Box.createVerticalStrut(10));
        claimPanel.add(levelLbl);
        claimPanel.add(Box.createVerticalStrut(10));
        claimPanel.add(codeLbl);
        claimPanel.add(Box.createVerticalStrut(4));
        claimPanel.add(codeField);
        claimPanel.add(Box.createVerticalStrut(14));
        claimPanel.add(instructionLbl);
        claimPanel.add(Box.createVerticalStrut(4));
        claimPanel.add(instructions);
        claimPanel.add(Box.createVerticalStrut(14));
        claimPanel.add(proofTitle);
        claimPanel.add(Box.createVerticalStrut(4));
        claimPanel.add(proofScroll);

        int result = JOptionPane.showConfirmDialog(this, claimPanel,
            "Your Reward Claim Slip",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

        // if they pressed OK (meaning they showed it to someone), reduce the unclaimed count
        if (result == JOptionPane.OK_OPTION) {
            unclaimedRewards--;
            updateRewardNotice();
            updateBadgeDisplay();
            JOptionPane.showMessageDialog(null,
                "Reward claimed! Great work!\n\nKeep completing tasks to earn more rewards!",
                "Claimed!", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    // -------------------------------------------------------
    // FILE OPERATIONS - save and load completed tasks
    // -------------------------------------------------------
    private void saveTaskToFile(Task t) {
        try {
            // true = append mode, so we don't overwrite old entries
            FileWriter fw   = new FileWriter("completed_tasks.txt", true);
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

        // if the file doesn't exist yet, just do nothing - that's fine
        if (!file.exists()) {
            return;
        }

        try {
            FileReader fr   = new FileReader(file);
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
    // HELPER METHODS - small utilities to keep things tidy
    // -------------------------------------------------------

    // creates a button with custom colors
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

    // creates a label with the default font
    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return label;
    }

    // creates a bold section title label
    private JLabel makeSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(new Color(45, 95, 155));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // creates a titled border for panels
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

    // invisible spacer component
    private Component makeSpacer(int width) {
        return Box.createHorizontalStrut(width);
    }


    // -------------------------------------------------------
    // MAIN METHOD - this is where the program starts running
    // -------------------------------------------------------
    public static void main(String[] args) {
        // Swing apps need to be started on the Event Dispatch Thread
        // this is the standard way to do it
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GetItDone app = new GetItDone();
                app.setVisible(true);
            }
        });
    }
}
