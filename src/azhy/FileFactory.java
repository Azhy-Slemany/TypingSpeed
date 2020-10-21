package azhy;

import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class FileFactory {

    public static String readResourceFile(Class tClass, String filePath){
        try{
            InputStream s = tClass.getResourceAsStream(filePath);
            return new Scanner(s, "UTF-8").useDelimiter("\\A").next();
        }catch (Exception e){
            System.out.println("Error happened reading file: " + filePath);
            return null;
        }
    }

    public static void writeResourceFile(Class tClass, String filePath, String text){
        try{
            String file = tClass.getResource(filePath).toURI()
                    .toString().replace("%20", " ").
                            replace("file:/", "");
            Path path = Paths.get(file);
            Writer out = Files.newBufferedWriter(path, Charset.defaultCharset(),
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            out.write(text);
            out.flush();
            out.close();
        }catch (Exception e){
            System.out.println("Error happened writing to file: " + filePath);
        }
    }
}
