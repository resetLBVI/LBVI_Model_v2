package lbvi.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class FilePath {
    public static String getFileName(String fileName, boolean mustExist) throws IOException {
        try {
            File codeSource = new File(
                    OutputWriter.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            );

            boolean runningFromJar = codeSource.isFile();
            File baseDir;

            if (runningFromJar) {
                // 🎯 Running from JAR: just use the JAR’s containing directory
                baseDir = codeSource.getParentFile();
            } else {
                // 🧠 Running in IDE: use working directory and walk up to find "src"
                baseDir = new File(System.getProperty("user.dir"));
                while (baseDir != null && !containSrc(baseDir)) {
                    baseDir = baseDir.getParentFile();
                }
                if (baseDir == null) {
                    throw new IOException("Couldn't find project root with 'src' folder.");
                }
                baseDir = baseDir.getParentFile(); // One level above root
            }
            //Wherever you're getting the file name for writing, set mustExist = false
            //if you are reading a file, like input config, set mustExist = true
            File target = new File(baseDir, fileName);
            if (mustExist && !target.exists()) {
                throw new IOException("File not found: " + target.getAbsolutePath());
            }
            if(!mustExist) {
                File parent = target.getParentFile();
                if(parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
            }

            return target.getAbsolutePath();

        } catch (URISyntaxException e) {
            throw new IOException("Failed to resolve path", e);
        }
    }


    //Helper: detects if a folder is the project root by looking for "src"
    private static boolean containSrc(File dir) {
        File[] childern = dir.listFiles();
        if(childern == null) {return false;}
        for(File child : childern) {
            if(child.getName().equals("src") && child.isDirectory()) {
                return true;
            }
        }
        return false;
    }
}
