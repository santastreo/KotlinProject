import kotlin.Pair;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
public class ProjectManager {

    // These are the GUI Components
    private JFrame frame;
    private JTree tree;

    // For the tree view of the projects and tasks
    private DefaultMutableTreeNode rootNode;
    private DefaultTreeModel treeModel;

    public ProjectManager() {
        initialize();
        loadFromJSON();
    }

    // setting the style of the application using nimbus look and feel from the javax swing library
    private void initialize() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame();
        frame.setTitle("Group 16 Project Management App");
        frame.setBounds(100, 100, 700, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Sets up the project and task tree view
        rootNode = new DefaultMutableTreeNode("List of All Projects");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        frame.add(new JScrollPane(tree), BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        // adding all the buttons and their respective action listeners to the bottom of the gui frame

        addButton(panel, "Add Project", e -> addProject());
        addButton(panel, "Delete Project", e -> deleteProject());
        addButton(panel, "Add Task", e -> addTask());
        addButton(panel, "Delete Task", e -> removeTask());
        addButton(panel, "View Matrix", e -> showMatrix());
        addButton(panel, "Set Successor Task", e -> setSuccessor());

        frame.add(panel, BorderLayout.SOUTH);

        frame.pack();
    }

    // Function for creating all 6 buttons
    private void addButton(JPanel panel, String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        panel.add(button);
        button.addActionListener(actionListener);
    }

    // function that allows the user to input a new project name,
    // creating a project with that name, adding it to the GUI,
    // and saving this addition so that it's remembered later.
    private void addProject() {
        String projectName = JOptionPane.showInputDialog(frame, "Please Enter The Project Name:");
        if (projectName != null && !projectName.trim().isEmpty()) { //not null and no empty spaces
            Project project = new Project(projectName); //calling the primary constructor of the Project.kt class, creating an object called project and giving it user input attributes such as name
            DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(project); // we create a tree node for the project
            rootNode.add(projectNode); // we add it to the rootNode
            treeModel.reload();
            saveToJSON();
        }
    }

    // This is the method to delete a project
    private void deleteProject() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (selectedNode != null && selectedNode.getUserObject() instanceof Project) {
            rootNode.remove(selectedNode);
            treeModel.reload();
            saveToJSON();
        }
    }

    // This is the method to add a task to the selected project
    private void addTask() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent(); //gets the currently selected item (node)(project) in the project tree and display in the GUI.
        //checks if user object "selectedNode" is not null and if it is an instance of the "Project" class. if so, it is cast to a Project type and stores it in the "selectedProject" variable
        if (selectedNode != null && selectedNode.getUserObject() instanceof Project selectedProject) {
            String taskName = JOptionPane.showInputDialog(frame, "Please Enter The Task Name:");
            if (taskName != null && !taskName.trim().isEmpty()) {
                String durationString = JOptionPane.showInputDialog(frame, "Please Enter The Duration For The Task (in hours):");
                if (durationString != null && !durationString.trim().isEmpty()) {
                    try {
                        int duration = Integer.parseInt(durationString);
                        //calling the primary constructor of the Task.kt class, creating an object called task and giving it attributes such as name and duration
                        Task task = new Task(taskName, duration);
                        //we call method addTask from Project.kt on the selectedProject variable, and "task" object is an instance of the Task.kt class
                        selectedProject.addTask(task); // adds the task to the collection of tasks managed by selectedProject
                        DefaultMutableTreeNode taskNode = new DefaultMutableTreeNode(task);
                        selectedNode.add(taskNode);
                        treeModel.reload();
                        saveToJSON();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid duration. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    // This is the method to delete a task from the selected project
    private void removeTask() {

        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();


        if (selectedNode != null && selectedNode.getUserObject() instanceof Task) {

            DefaultMutableTreeNode parentProjectNode = (DefaultMutableTreeNode) selectedNode.getParent();
            if(parentProjectNode != null && parentProjectNode.getUserObject() instanceof Project parentProject) {
                Task selectedTask = (Task) selectedNode.getUserObject();


                parentProject.removeTask(selectedTask);


                parentProjectNode.remove(selectedNode);
                treeModel.reload();


                saveToJSON();
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a task to remove.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // This is the method to call the matrix and create it to a gui output representation
    private void showMatrix() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (selectedNode != null && selectedNode.getUserObject() instanceof Project selectedProject) {

            // Gets the task from the selected project
            List<Task> tasks = selectedProject.getAllTasks();

            // Uses the MatrixGenerator function in Kotlin to generate the adjacency matrix
            int[][] adjacencyMatrix = MatrixGenerator.Companion.generateAdjacencyMatrix(tasks);


            String[] columnNames = new String[tasks.size() + 1];

            columnNames[0] = ""; // The first column is used for row headers (task names)
            for (int i = 0; i < tasks.size(); i++) {

                columnNames[i + 1] = tasks.get(i).getName(); // Shifts the task names to the right by one
            }

            Object[][] matrixData = new Object[tasks.size()][tasks.size() + 1];
            for (int i = 0; i < tasks.size(); i++) {

                matrixData[i][0] = tasks.get(i).getName(); // Sets the task names as the first column of each row

                for (int j = 0; j < tasks.size(); j++) {

                    matrixData[i][j + 1] = adjacencyMatrix[i][j]; // nested for loop, shifts the adjacency matrix to the right by one
                }
            }

            JTable table = new JTable(matrixData, columnNames);

            table.getTableHeader().setReorderingAllowed(false); // Prevent reordering of columns
            JScrollPane scrollPane = new JScrollPane(table);

            JFrame matrixFrame = new JFrame("Adjacency Matrix for " + selectedProject.getName());
            matrixFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            matrixFrame.add(scrollPane);
            matrixFrame.setSize(600, 600);
            matrixFrame.setLocationRelativeTo(null);
            matrixFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a project to view its adjacency matrix.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    // Method to set or update the successor tasks for a selected task in the GUI. involves displaying a list of
    // possible successor tasks, letting the user select from this list, and then saving the user's selections
    // as the new set of successors for the task

    private void setSuccessor() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent(); // Get the currently selected node from the tree (GUI).
        if (selectedNode != null && selectedNode.getUserObject() instanceof Task selectedTask) { // selectedTask is a variable that is set as an instance of Task class
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent(); // Get the parent node of the selected task, which should be a project.
            Project parentProject = (Project) parentNode.getUserObject(); // Retrieve the project object from the parent node.

            // retrieves a pair containing a list of potential successor tasks and their corresponding indices for a selected task within a given project.
            Pair<List<Task>, List<Integer>> data = selectedTask.fetchTaskSuccessors(parentProject);
            List<Task> tasks = data.getFirst(); //get the first element of the pair data, which is a list of tasks, excluding the selected task.
            List<Integer> currentSuccessorsIndices = data.getSecond(); // get the second element of the pair data, which is a list of indices representing the current successors of the selected task.

            int[] indicesArray = currentSuccessorsIndices.stream().mapToInt(Integer::intValue).toArray(); // Converts List<Integer> to int[]

            DefaultListModel<Task> listModel = new DefaultListModel<>(); // Creates a DefaultListModel and populates it with tasks
            for (Task task : tasks) {
                listModel.addElement(task);
            }
            JList<Task> taskJList = new JList<>(listModel); // we create a JList for task selection, setting the selected indices to current successors.
            taskJList.setSelectedIndices(indicesArray);

            int result = JOptionPane.showConfirmDialog(
                    frame,
                    new JScrollPane(taskJList),
                    "Choose successor tasks for " + selectedTask.getName() + ":",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (result == JOptionPane.OK_OPTION) {
                List<Task> chosenTasks = taskJList.getSelectedValuesList();
                selectedTask.updateTaskSuccessors(chosenTasks);
                saveToJSON();
            }
        }
    }


    // calling primary constructor of PersistenceManager class and creating an instance of the class
    private final PersistenceManager persistenceManager = new PersistenceManager();

    private void saveToJSON() {
        persistenceManager.saveToJSON(rootNode);
    }

    private void loadFromJSON() {
        boolean success = persistenceManager.loadFromJSON(rootNode);
        if (success) {
            treeModel.reload();
        } else {
            JOptionPane.showMessageDialog(null,
                    "Error loading data from file!",
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Main method to run the application
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ProjectManager window = new ProjectManager();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
