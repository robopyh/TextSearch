package andrey.textsearch;

import andrey.textsearch.model.FileReaderTask;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main extends Application {

    // Create and show the window of the application
    @Override
    public void start(Stage primaryStage) throws Exception{

        // Create TabPane
        // Create and attach the addTab ("+")
        final TabPane tabPane = new TabPane();
        final Tab newTab = new Tab("+");
        newTab.setClosable(false);
        tabPane.getTabs().add(newTab);

        // Create the start tab
        createAndSelectNewTab(tabPane, "Tab 1");

        // Attach listener to the "+"-tab
        // Create new tab on action
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldSelectedTab, newSelectedTab) -> {
            if (newSelectedTab == newTab) {
                try {
                    createAndSelectNewTab(tabPane, "Tab " + (tabPane.getTabs().size()));
                } catch (IOException ex) {
                    Logger.getLogger(FileReaderTask.class.getName()).log(Level.ALL, ex.toString(), ex);
                }
            }
        });

        // Create the parent pane
        final BorderPane root = new BorderPane();
        root.setCenter(tabPane);

        // Create scene, load css
        Scene scene = new Scene(root, 1000, 600);
        String css = this.getClass().getResource("view/TabStyle.css").toExternalForm();
        scene.getStylesheets().add(css);

        // Show up app window
        primaryStage.setTitle("Text Search");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("view/myIcon.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Add the new tab to the TabPane
    private void createAndSelectNewTab(final TabPane tabPane, final String title) throws IOException {
        Tab tab = FXMLLoader.load(getClass().getResource("view/TabLayout.fxml"));
        tab.setText(title);
        final ObservableList<Tab> tabs = tabPane.getTabs();
        tab.closableProperty().bind(Bindings.size(tabs).greaterThan(2));
        tabs.add(tabs.size() - 1, tab);
        tabPane.getSelectionModel().select(tab);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
