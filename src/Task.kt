import java.io.Serializable

// serializable, allowing instances to be saved to and loaded from a file

// task class represents a task with a name and duration. also tracks successor tasks


data class Task(
    val name: String,
    val duration: Int) : Serializable {

    // data structure: successors: a private mutable list that tracks tasks that depend on the completing of the current task.

    private val successors: MutableList<Task> = mutableListOf()


    @Synchronized
    fun addSuccessor(task: Task) { // task is the task to be added as a successor
        if (task != this && !successors.contains(task)) {
            successors.add(task)
        }
    }

    @Synchronized
    fun clearSuccessors() { // Clears the list of successor tasks, removing all successors
        successors.clear()
    }

    @Synchronized
    fun getSuccessors(): List<Task> { //Retrieves a list containing all successor tasks
        return successors.toList()
    }

    @Synchronized
    //fetches the successor tasks of a given task within a project and returns a pair containing the list of tasks
    // and a list of indices indicating which tasks are the current successors
    fun fetchTaskSuccessors(parentProject: Project): Pair<List<Task>, List<Int>> {
        val tasks = parentProject.tasks.filterNot { it == this }
        val currentSuccessors = this.successors
        val indices = tasks.mapIndexed { index, task ->
            if (currentSuccessors.contains(task)) index else null
        }.filterNotNull()

        return Pair(tasks, indices)
    }

    @Synchronized
    // updates the successors for a chosen task
    // "chosenTasks" is a list of tasks that should be set as successor for the selected task.
    fun updateTaskSuccessors(chosenTasks: MutableList<Task>) { // takes a list of tasks, named chosenTasks, which are the tasks you want to follow after the current one
        this.clearSuccessors() // clears the current task's existing "next-in-line" tasks
        for (task in chosenTasks) {
            this.addSuccessor(task) // For each task in the new list, it adds it to the current task's "next-in-line" tasks
        }
    }

    @Synchronized
    override fun toString(): String { // returns a string that contains the tasks name and duration, on the GUI
        return "$name ($duration hours)"
    }
}
