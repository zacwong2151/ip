package duke;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Scanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * class where main is run
 */
public class Duke extends Application implements Serializable {
    protected static String dukeFilePath = "data/duke.txt";
    protected static String tempFilePath = "data/temp.txt";
    private static Storage storage;
    private static TaskList tasks;
    private Ui ui;
    private ScrollPane scrollPane;
    private VBox dialogContainer;
    private TextField userInput;
    private Button sendButton;
    private Scene scene;
    private Image user = new Image(this.getClass().getResourceAsStream("/images/DaUser.png"));
    private Image duke = new Image(this.getClass().getResourceAsStream("/images/DaDuke.png"));

    public Duke() {}

    /**
     * constructor to initialise the Ui, Storage and TaskList objects
     * @param filePath the relative path to the file that acts as a temporary storage for the ArrayList(Task) object
     */
    public Duke(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
        try {
            tasks = new TaskList(storage.load());
        } catch (DukeException | IOException | ClassNotFoundException e) {
            Ui.printWithIndent("Hi! You do not have any tasks at the moment");
            createTxtFile();
            tasks = new TaskList();
        }
    }
    @Override
    public void start(Stage stage) {
        //Step 1. Setting up required components

        //The container for the content of the chat to scroll.
        scrollPane = new ScrollPane();
        dialogContainer = new VBox();
        scrollPane.setContent(dialogContainer);

        userInput = new TextField();
        sendButton = new Button("Send");

        AnchorPane mainLayout = new AnchorPane();
        mainLayout.getChildren().addAll(scrollPane, userInput, sendButton);

        scene = new Scene(mainLayout);

        stage.setScene(scene);
        stage.show();

        //Step 2. Formatting the window to look as expected
        stage.setTitle("Duke");
        stage.setResizable(false);
        stage.setMinHeight(600.0);
        stage.setMinWidth(400.0);

        mainLayout.setPrefSize(400.0, 600.0);

        scrollPane.setPrefSize(385, 535);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        scrollPane.setVvalue(1.0);
        scrollPane.setFitToWidth(true);

        // You will need to import `javafx.scene.layout.Region` for this.
        dialogContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);

        userInput.setPrefWidth(325.0);

        sendButton.setPrefWidth(55.0);

        AnchorPane.setTopAnchor(scrollPane, 1.0);

        AnchorPane.setBottomAnchor(sendButton, 1.0);
        AnchorPane.setRightAnchor(sendButton, 1.0);

        AnchorPane.setLeftAnchor(userInput , 1.0);
        AnchorPane.setBottomAnchor(userInput, 1.0);

        //Part 3. Add functionality to handle user input.
        sendButton.setOnMouseClicked((event) -> {
            handleUserInput();
        });

        userInput.setOnAction((event) -> {
            handleUserInput();
        });

        //Scroll down to the end every time dialogContainer's height changes.
        dialogContainer.heightProperty().addListener((observable) -> scrollPane.setVvalue(1.0));
    }

    /**
     * Iteration 2:
     * Creates two dialog boxes, one echoing user input and the other containing Duke's reply and then appends them to
     * the dialog container. Clears the user input after processing.
     */
    private void handleUserInput() {
        String string = userInput.getText();
        Label userText = new Label(string);
        Label dukeText = new Label(Parser.parse(string, tasks, storage));
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(userText, new ImageView(user)),
                DialogBox.getDukeDialog(dukeText, new ImageView(duke))
        );
        userInput.clear();
    }
    public TaskList getTasks() {
        return this.tasks;
    }
    public Storage getStorage() {
        return this.storage;
    }
    /**
     * The exception caught above is likely the IOException thrown at duke.Storage.java line 11. Catching
     * this exception means that duke.TaskList is empty (I think), which means that the duke.txt file might
     * not exist yet, so it is created in this function
     */
    public static void createTxtFile() {
        try {
            FileWriter fw = new FileWriter(dukeFilePath);
            fw.close();
        } catch (IOException e) {
            System.out.println("shag");
        }
    }
    /**
     * Runs the program
     */
    public void run() {
        ui.showWelcome();
        boolean isExit = Parser.isExit();
        Scanner scanner = new Scanner(System.in);

        while (!isExit) {
            String userInput = ui.readCommand(scanner);
            ui.showLine();
            Parser.parse(userInput, tasks, storage);
            isExit = Parser.isExit();
            if (isExit) {
                ui.showExit();
            }
            ui.showLine();
        }
    }
    public static void main(String[] args) {
        new Duke(tempFilePath).run();
    }
}
