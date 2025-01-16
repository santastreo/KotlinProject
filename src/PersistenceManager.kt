//Necessary libraries
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.tree.DefaultMutableTreeNode

//This class deals with saving our project data and loading it back, as JSON objects
//It provides functionalities to convert tree nodes into a JSON representation and vice versa

class PersistenceManager {

    // This method converts the provided root tree node into a JSON representation
    // and writes it to a file named "projects.json"
    // main parameter is "rootNode", which is the root tree node representing a collection of projects

    fun saveToJSON(rootNode: DefaultMutableTreeNode) {
        val projectsArray = JSONArray()

        for (i in 0 until rootNode.childCount) { // Loops through all the project nodes and save their details
            val projectNode = rootNode.getChildAt(i) as DefaultMutableTreeNode
            val project = projectNode.userObject as Project
            val projectObject = JSONObject().apply {
                put("name", project.name)
            }

            val tasksArray = JSONArray()

            for (j in 0 until projectNode.childCount) { // Loops through all the task nodes within a project node, saving each task in the project
                val taskNode = projectNode.getChildAt(j) as DefaultMutableTreeNode
                val task = taskNode.userObject as Task
                val taskObject = JSONObject().apply {
                    put("name", task.name)
                    put("duration", task.duration)

                    //used ChatGPT to try help with saving successor relationships to json
                    val successorsArray = JSONArray()
                    task.getSuccessors().forEach { successor ->
                        successorsArray.put(successor.name)
                    }
                    put("successors", successorsArray)
                    //end of ChatGPT code
                }

                tasksArray.put(taskObject)
            }

            projectObject.put("tasks", tasksArray)
            projectsArray.put(projectObject)
        }

        try { // Writes the JSON representation to a file.
            FileWriter("projects.json").use { file -> file.write(projectsArray.toString()) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadFromJSON(rootNode: DefaultMutableTreeNode): Boolean { // Loads projects and tasks from a file named "projects.json" and reconstructs the tree representation
        try {
            val content = String(Files.readAllBytes(Paths.get("projects.json")))
            val projectsArray = JSONArray(content)

            rootNode.removeAllChildren()

            for (i in 0 until projectsArray.length()) { // Loops through each project in the JSON array and build the tree representation
                val projectObject = projectsArray.getJSONObject(i)
                val projectName = projectObject.getString("name")
                val project = Project(projectName)
                val projectNode = DefaultMutableTreeNode(project)

                val tasksMap = mutableMapOf<String, Task>()

                val tasksArray = projectObject.getJSONArray("tasks")
                for (j in 0 until tasksArray.length()) { // Loops through each task within a project and add it to the project node
                    val taskObject = tasksArray.getJSONObject(j)
                    val taskName = taskObject.getString("name")
                    val taskDuration = taskObject.getInt("duration")
                    val task = Task(taskName, taskDuration)
                    tasksMap[taskName] = task
                    val taskNode = DefaultMutableTreeNode(task)

                    projectNode.add(taskNode)
                }
                //ChatGPT again for saving successor
                tasksArray.forEach { taskObject ->
                    val task = tasksMap[(taskObject as JSONObject).getString("name")]!!
                    val successorsArray = taskObject.getJSONArray("successors")
                    for (k in 0 until successorsArray.length()) {
                        val successorName = successorsArray.getString(k)
                        task.addSuccessor(tasksMap[successorName]!!)
                    }
                }//end of ChatGPT code

                rootNode.add(projectNode)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
