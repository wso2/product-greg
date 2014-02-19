package org.wso2.carbon.registry.governance.api.test.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileManagerUtil {
    private static final Log log = LogFactory.getLog(FileManagerUtil.class);

    public static String readFile(String filePath) throws IOException {
        BufferedReader reader;
        StringBuilder stringBuilder;
        String line;
        String ls;
        log.debug("Path to file : " + filePath);
        reader = new BufferedReader(new FileReader(filePath));
        stringBuilder = new StringBuilder();
        ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        reader.close();
        return stringBuilder.toString();
    }

    public static String readFile(File file) throws IOException {
        BufferedReader reader;
        StringBuilder stringBuilder;
        String line;
        String ls;

        reader = new BufferedReader(new FileReader(file));
        stringBuilder = new StringBuilder();
        ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        reader.close();
        return stringBuilder.toString();
    }

    public static void writeToFile(String filePath, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
        writer.write(content);
        writer.newLine();
        writer.flush();
        writer.close();

    }

    public static void copyFile(File sourceFile, String destinationPath) throws IOException {
        File destinationFile = new File(destinationPath);

        FileReader in = new FileReader(sourceFile);
        FileWriter out = new FileWriter(destinationFile);
        int c;

        while ((c = in.read()) != -1) {
            out.write(c);
        }

        in.close();
        out.close();
    }
}
