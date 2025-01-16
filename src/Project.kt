import java.io.Serializable

// serializable, allowing instances to be saved to and loaded from a file

// project class represents a project which contains a collection of tasks.

data class Project(
    val name: String) : Serializable {

    // data structure: tasks: a mutable list which is a dynamic list structure for storing Task objects
    // done to manage a collection of tasks within a project

    val tasks: MutableList<Task> = mutableListOf()


    @Synchronized
    fun addTask(task: Task) { // adding a task to the selected project
        tasks.add(task)
    }

    @Synchronized
    fun getAllTasks(): List<Task> { //Retrieves a list containing all the tasks
        return tasks.toList()
    }


    @Synchronized
    fun removeTask(task: Task) { // removes the selected task from the current project
        tasks.remove(task)
    }

    @Synchronized
    override fun toString(): String { // returns a string that contains the project name on the GUI
        return name
    }

}