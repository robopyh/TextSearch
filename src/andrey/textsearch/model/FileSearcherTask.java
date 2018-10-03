package andrey.textsearch.model;

import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.DosFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;


public class FileSearcherTask extends Task<TreeItem<MyFile>>{

    private final File directory;
    private final String searchText;
    private final String[] extensions;

    private int filesCount;
    private int workDone;


    public FileSearcherTask(File directory, String searchText, String[] extensions){
        this.directory = directory;
        this.extensions = extensions;
        this.searchText = searchText;
        filesCount = directory.list().length;
        workDone = 0;
    }

    @Override
    protected TreeItem<MyFile> call() {
        return createFilesTree(directory);
    }

    // build the files tree
    private TreeItem<MyFile> createFilesTree(File path){
        TreeItem<MyFile> root = new TreeItem<>(new MyFile(path));
        TreeItem<MyFile> temp;

        // if a file is a directory - explore it recursively
        // else search the text in this file
        for (File f : path.listFiles()){
            // skip Windows system files
            try {
                DosFileAttributes dfa = Files.readAttributes(f.toPath(), DosFileAttributes.class);
                if (dfa.isSystem())
                    continue;
            } catch (IOException ex) {
                Logger.getLogger(FileReaderTask.class.getName()).log(Level.ALL, ex.toString(), ex);
            }

            if (f.isDirectory()) {
                // update a files count
                filesCount += f.list().length;

                // Explore directory
                temp = createFilesTree(f);

                updateProgress(++workDone, filesCount);

                // If there are any appropriate files (directory must have the child), add this directory to the view
                if (! temp.isLeaf())
                    root.getChildren().add(temp);
            }
            else{
                updateProgress(++workDone, filesCount);
                // Check if a file extension is in the list
                if (Arrays.asList(extensions).contains(FilenameUtils.getExtension(f.getName()))) {
                    // Check if a file contains a search text
                    MyFile myFile = isTextInFile(f);
                    if (myFile != null)
                        root.getChildren().add(new TreeItem<>(myFile));
                }
            }
        }
        return root;
    }

    // Search the given text in a file
    private MyFile isTextInFile(File file){
        // Count of matches in a file
        int matchCount = 0;
        // Count of lines in a file
        int linesCount = 0;
        // Line number and a count of matches in it
        Map<Integer, Integer> countOfMatchesInLine = new LinkedHashMap<>();

        try {
            // initialize reader
            FileInputStream fileInputStream = new FileInputStream(file);
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(fileInputStream));

            // check each line of a document, increment a count of lines
            String line;
            while((line = reader.readLine()) != null) {
                linesCount++;
                // if a line contains a search text
                // check a count of matches
                // save <line number, count of matches>
                if((line.contains(searchText))) {
                    matchCount = StringUtils.countMatches(line, searchText);
                    countOfMatchesInLine.put(reader.getLineNumber(), matchCount);
                }
            }
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(FileReaderTask.class.getName()).log(Level.ALL, ex.toString(), ex);
        }

        return matchCount > 0 ? new MyFile(file, linesCount, countOfMatchesInLine) : null;
    }
}
