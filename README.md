# GetItDone

A simple Java desktop app to help you stay productive, manage tasks, hit study goals, and take care of yourself.

## Features

- **Add tasks** with a name and deadline
- **Delete tasks** you no longer need
- **Mark tasks complete** — completed tasks are saved to `completed_tasks.txt`
- **View all completed tasks** any time on the Completed tab
- **Set a daily study goal** (e.g. complete 5 tasks today)
- **Points & badges** — earn points for every task you finish and unlock badges as you go
- **Break reminders** — a popup every 25 minutes telling you to take a 5-minute break
- **Water reminders** — a popup every 60 minutes reminding you to drink water

## Files

| File | What it does |
|------|-------------|
| `Task.java` | Represents a single task (name, deadline, completed status) |
| `GetItDone.java` | The main application window and all the logic |
| `completed_tasks.txt` | Created automatically when you complete your first task |

## How to Run

You need Java installed. Open a terminal in this folder and run:

**Step 1 — Compile:**
```
javac Task.java GetItDone.java
```

**Step 2 — Run:**
```
java GetItDone
```

That's it! The app window will open.

## Points & Badges

| Badge | How to earn it |
|-------|---------------|
| Task Starter | Reach 10 points |
| Getting Things Done | Reach 50 points |
| Productivity Pro | Reach 100 points |
| Unstoppable | Reach 200 points |
| Goal Crusher | Complete your daily goal |
| On Fire | Complete your daily goal 3 times |

Each completed task gives you **+10 points**.
