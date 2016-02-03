package org.wso2.carbon.registry.samples.populator.utils;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.*;


public class Utils {

    /**
     *This method is used to back up existing RXTs.
     *
     * @param registry      registry instance.
     * @param path          path of the rxt.
     * @param fileName      file name of backed up rxt files.
     * @throws RegistryException
     */
    public static void backUpRXTs(Registry registry, String path, String fileName) throws RegistryException{
        Resource resource = registry.get(path);
        try {
            RXTContentToFile(resource.getContentStream(), fileName);
        } catch (FileNotFoundException e){
            System.out.println("Could not read rxt content");
        }
    }

    /**
     *This method is used to write rxt content to text file.
     *
     * @param is        rxt content as a input stream
     * @param filename  file name of backed up rxt file.
     * @throws FileNotFoundException
     */
    private static void RXTContentToFile(InputStream is, String filename) throws FileNotFoundException {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

        } catch (IOException e) {
            System.out.println("Could not read rxt content");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("Could not close input stream");
                }
            }
        }
        PrintWriter out = new PrintWriter("resources/" + filename);
        out.println(sb.toString());
        out.flush();
        out.close();

    }
}
