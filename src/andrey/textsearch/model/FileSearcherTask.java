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
        TreeItem<MyFile> treeItem = createFilesTree(directory);
        // if a given directory contains unreadable folders/files, they'll not be counted
        // so we need manually update progress on the end
        updateProgress(100, 100);
        updateMessage("Done!");
        return treeItem.getChildren().size() == 0 ? null : treeItem;
    }

    // build the files tree
    private TreeItem<MyFile> createFilesTree(File path){
        TreeItem<MyFile> root = new TreeItem<>(new MyFile(path));
        TreeItem<MyFile> temp;

        // if a file is a directory - explore it recursively
        // else search the text in this file
        for (File f : path.listFiles()){
            try {
                // skip unreadable files
                if (!f.canRead())
                    continue;
                // skip Windows system files
                DosFileAttributes dfa = Files.readAttributes(f.toPath(), DosFileAttributes.class);
                if (dfa.isSystem())
                    continue;
            } catch (Exception ex) {
                Logger.getLogger(FileReaderTask.class.getName()).log(Level.ALL, ex.toString(), ex);
            }

            if (f.isDirectory()) {
                // list() may return null
                String[] files = f.list();
                if (files == null)
                    continue;

                // update a files count
                filesCount += files.length;

                // Explore directory
                temp = createFilesTree(f);

                updateProgress(++workDone, filesCount);
                updateMessage(workDone + " of " + filesCount);

                // If there are any appropriate files (directory must have the child), add this directory to the view
                if (! temp.isLeaf())
                    root.getChildren().add(temp);
            }
            else{
                updateProgress(++workDone, filesCount);
                updateMessage(workDone + " of " + filesCount);
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
