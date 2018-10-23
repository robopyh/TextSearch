package andrey.textsearch;

import andrey.textsearch.model.FileReaderTask;
import andrey.textsearch.model.FileSearcherTask;
import andrey.textsearch.model.MyFile;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;


public class TabController implements Initializable{

    @FXML
    private TextField folderText;

    @FXML
    private TextField inputText;

    @FXML
    private TextField extensionText;

    @FXML
    private Button searchBtn;

    @FXML
    private TreeView<MyFile> treeView;

    @FXML
    private ListView<String> listView;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressLabel;

    @FXML
    private Label matchLabel;

    // Number of a current highlighted fragment
    private int focusedLabel;
    // File we are working with now
    private MyFile currentFile;


    public TabController(){
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Allows to start search by pressing the Enter
        // searchBtn.setDefaultButton(true);

        // TreeView listener
        // We need to show file content if leaf node is selected
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isLeaf())
                showFileText(newValue.getValue());
        });

        // Set custom cell factory to the tree view, so its nodes will be containing MyFile objects
        treeView.setCellFactory(p -> new TreeCellFactory());

        // Set custom cell factory to the list view
        listView.setCellFactory(p -> new ListCellFactory());
    }

    // Need to use a custom CellFactory to determine, how the items are shown in the TreeView
    // It will be showing only filenames
    private final class TreeCellFactory extends TreeCell<MyFile> {

        TreeCellFactory() {
        }

        @Override
        public void updateItem(MyFile item, boolean empty) {
            super.updateItem(item, empty);

            if (empty)
                setText(null);
            else
                setText(item.getFilename());
        }
    }


    // Will be split a line into multiple Labels, if it contains search text
    // E.g. with one fragment: Label_1 (default style) | Label_2 (highlighted style) | Label_3 (default style)
    private final class ListCellFactory extends ListCell<String> {

        ListCellFactory() {
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                setGraphic(null);
                setText(null);
            } else {
                // if a line doesn't contain search text, set default text
                if (! item.contains(inputText.getText())) {
                    setText(item);
                    setGraphic(null);
                }
                // if a line contains search text, need to highlight that parts
                else {
                    // Container for Labels
                    HBox hbox = new HBox();
                    // Text line
                    String searchText = inputText.getText();
                    // Length of a line
                    int searchTextLength = searchText.length();
                    // Count for multiple matches on a line
                    int matchesOnTheLine = 0;

                    // build a line with Labels
                    while(item.contains(searchText)){
                        // Find index of a fragment we need to highlight
                        int searchTextIndex = item.indexOf(searchText);

                        // Part before highlighted
                        Label textBefore = new Label(item.substring(0, searchTextIndex));
                        hbox.getChildren().add(textBefore);

                        //Highlighted part
                        // Firstly, need to compare numbers of an actual line and of an expected one
                        //
                        // Next, comparing number of a current match and of an expected on a line.
                        // It's required for highlighting only the right fragment on the line with multiple matches.
                        Label textHighlighted = new Label(item.substring(searchTextIndex, searchTextIndex + searchTextLength));
                        if (getIndex() == currentFile.getMatchLine(focusedLabel) && matchesOnTheLine == currentFile.getMatchNumber(focusedLabel)) {
                            textHighlighted.getStyleClass().add("label-highlighted-focused");
                        } else
                            textHighlighted.getStyleClass().add("label-highlighted");
                        hbox.getChildren().add(textHighlighted);

                        // Part after highlighted
                        item = item.substring(searchTextIndex + searchTextLength);
                        // If there is only one match on a line
                        if (!item.contains(searchText)) {
                            Label textAfter = new Label(item);
                            hbox.getChildren().add(textAfter);
                        }

                        matchesOnTheLine++;
                    }
                    setText(null);
                    setGraphic(hbox);
                }
            }
        }
    }

    // Showing file content
    private void showFileText(MyFile file){
        // clean listview
        listView.setItems(null);

        focusedLabel = 0;
        currentFile = file;
        // Using the task to load large files
        FileReaderTask fileReaderTask = new FileReaderTask(file);
        progressBar.progressProperty().bind(fileReaderTask.progressProperty());
        fileReaderTask.setOnSucceeded(event -> {
                listView.setItems(fileReaderTask.getValue());
                matchLabel.setText(focusedLabel + 1 + " of " + currentFile.getMatchCount());
                });
        new Thread (fileReaderTask).start();
    }

    // "Open" button handler
    // Choosing the folder to search in
    @FXML
    private void chooseFolder(){
        DirectoryChooser dc = new DirectoryChooser();
        File choice = dc.showDialog(treeView.getScene().getWindow());
        if (choice != null && choice.isDirectory()) {
            folderText.setText(choice.getAbsolutePath());
        }
    }

    // "Search" button handler
    // Search files with the given text
    @FXML
    private void searchFiles(){
        // Check directory input
        // File needs to be a directory
        if (! new File(folderText.getText()).isDirectory()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please specify correct folder!");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        // Check search text input
        // Text must not be empty
        if ("".equals(inputText.getText())){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please enter the text to search!");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        String[] extension;
        // Check extensions input
        // If it's empty, *.log is default
        if ("".equals(extensionText.getText())){
            extension = new String[]{"log"};
        }
        else {
            // remove whitespaces and split into list
            extension = extensionText.getText().replaceAll("\\s+","").split(",");
        }

        // clean listview from current file
        listView.setItems(null);

        // Use Task to do a background search
        FileSearcherTask fileSearcherTask = new FileSearcherTask(new File(folderText.getText()), inputText.getText(), extension);
        treeView.rootProperty().bind(fileSearcherTask.valueProperty());
        progressBar.progressProperty().bind(fileSearcherTask.progressProperty());
        progressLabel.textProperty().bind(fileSearcherTask.messageProperty());
        new Thread (fileSearcherTask).start();
    }

    // "Previous" button handler
    // Highlight Previous text fragment
    @FXML
    private void previousLabel(){
        // check if a listview is not empty
        if (listView.getItems().isEmpty())
            return;

        // Get count of text matches in this file
        int matchesCount = currentFile.getMatchCount();

        // Modular subtraction: N = (N - 1) mod M
        // This construction allows to get positive values
        focusedLabel = ((--focusedLabel % matchesCount) + matchesCount) % matchesCount;

        // Change matchLabel text
        matchLabel.setText(focusedLabel + 1 + " of " + matchesCount);

        // Scroll to line with highlighted text
        // TODO: Horizontal scroll
        listView.scrollTo(currentFile.getMatchLine(focusedLabel));

        // Refreshing view to accept changes
        listView.refresh();
    }

    // "Next" button handler
    // Highlight next text fragment
    @FXML
    private void nextLabel(){
        if (listView.getItems().isEmpty())
            return;

        int matchesCount = currentFile.getMatchCount();

        // Modular addiction
        focusedLabel = ((++focusedLabel % matchesCount) + matchesCount) % matchesCount;

        matchLabel.setText(focusedLabel + 1 + " of " + matchesCount);

        listView.scrollTo(currentFile.getMatchLine(focusedLabel));

        listView.refresh();
    }
}
