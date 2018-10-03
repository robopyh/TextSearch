package andrey.textsearch.model;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.Map;


// Class representing files that contain a given text
public class MyFile{
    private final File file;
    private int linesCount;
    private Map<Integer, Integer> countOfMatchesInLine;

    MyFile(File file){
        this.file = file;
    }

    MyFile(File file, int linesCount, Map<Integer, Integer> countOfMatchesInLine){
        this.file = file;
        this.linesCount = linesCount;
        this.countOfMatchesInLine = countOfMatchesInLine;
    }

    public String getFilename(){
        return FileSystemView.getFileSystemView().getSystemDisplayName (file);
    }

    public File getFile(){
        return file;
    }

    public int getLinesCount(){
        return linesCount;
    }

    // return a number of line based on an overall match number
    public int getMatchLine(int matchNumber) {
        int sum = 0;
        for (Map.Entry<Integer,Integer> entry : countOfMatchesInLine.entrySet()) {
            sum += entry.getValue();
            if (sum > matchNumber)
                return entry.getKey() - 1;
        }
        return -1;
    }

    // return a count of matches
    public int getMatchCount() {
        return countOfMatchesInLine.values().stream().mapToInt(Integer::intValue).sum();
    }

    // return a number of match in a line
    public int getMatchNumber(int matchNumber) {
        // if it's a first line
        if (matchNumber < countOfMatchesInLine.entrySet().iterator().next().getValue())
            return matchNumber;

        int sum = 0;
        for (Map.Entry<Integer,Integer> entry : countOfMatchesInLine.entrySet()) {
            sum += entry.getValue();
            if (sum >= matchNumber)
                return sum - matchNumber;
        }
        return -1;
    }
}
