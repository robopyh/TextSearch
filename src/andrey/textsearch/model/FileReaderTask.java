package andrey.textsearch.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FileReaderTask extends Task<ObservableList<String>> {

    private final File file;

    private final int linesCount;
    private int workDone;

    public FileReaderTask(MyFile file){
        this.file = file.getFile();
        linesCount = file.getLinesCount();
        workDone = 0;
    }

    @Override
    protected ObservableList<String> call() {
        return read(file);
    }

    private ObservableList<String> read(File file) {
        Instant start = Instant.now();
        ObservableList<String> lines = FXCollections.observableArrayList();
        String line;
        try {
            // initialize reader
            FileInputStream fileInputStream = new FileInputStream(file);
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(fileInputStream));

            // read lines
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                updateProgress(++workDone, linesCount);
            }
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(FileReaderTask.class.getName()).log(Level.ALL, ex.toString(), ex);
        }
        Instant end = Instant.now();
        System.out.println(Duration.between(start, end));
        return lines;
    }
}
