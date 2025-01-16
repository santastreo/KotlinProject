class MatrixGenerator {
    //"i" is loop variable for row index(normal list of tasks)
    //"j" is loop variable for column index(successors)

    //Singleton object (like static method in java)
    companion object {
        fun generateAdjacencyMatrix(tasks: List<Task>): Array<IntArray> {
            //determine size of matrix, which is the number of tasks
            val n = tasks.size
            //creates array using number of tasks (n), and sets it with all 0's
            val adjacencyMatrix = Array(n) { IntArray(n) { 0 } }
            //loop over all tasks using their indices
            for (i in tasks.indices) {
                //retrieves current task based on its index
                val task = tasks[i]
                //loops over each successor of the current task
                for (successor in task.getSuccessors()) {
                    //finds index of successor task in the list of tasks
                    val j = tasks.indexOf(successor)
                    // ensures if successor task is present in the list, then we set loop variable i and j as 1
                    //if j is equal to -1,no successor was found in the list, so we leave it as 0
                    if (j != -1) {
                        adjacencyMatrix[i][j] = 1
                    }
                }
            }
            return adjacencyMatrix // 2D array of integers
        }
    }
}

