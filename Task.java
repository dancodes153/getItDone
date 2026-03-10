// Task.java
// This class represents a single task the user wants to complete

public class Task {

    // I'm using public fields so I can access them easily from the main app
    public String name;
    public String deadline;
    public boolean completed;

    // constructor - creates a new task with a name and deadline
    public Task(String name, String deadline) {
        this.name = name;
        this.deadline = deadline;
        this.completed = false;  // new tasks are not completed yet
    }

    // this is what shows up in the task list on screen
    @Override
    public String toString() {
        return name + "   |   Due: " + deadline;
    }
}
