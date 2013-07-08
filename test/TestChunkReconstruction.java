package bitace;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import javax.swing.JFileChooser;

/**
 * This class was created to test the chunking and reconstruction of files
 * 
 * @author Adam Giacobbe
 */

public class TestChunkReconstruction {

    public static HashMap<Integer, byte[]> cFile(File f, int chunkSize, int fileSize) {
        HashMap<Integer, byte[]> file = new HashMap<Integer, byte[]>();

        System.out.println("f.length() = " + f.length());

        FileInputStream fis;
        try {
            fis = new FileInputStream(f);
            BufferedInputStream bfis = new BufferedInputStream(fis);

            byte[] chunk = new byte[chunkSize];

            int chunkID = 0;
            int bytesRead = 0;

            byte[] buf = new byte[1];

            int numChunks = ((fileSize / chunkSize) + 1);
            System.out.println("numChunks = " + numChunks);
            for (chunkID = 0; chunkID < numChunks; chunkID++) {
                chunk = new byte[chunkSize];
                for (int i = 0; i < chunkSize; i++) {
                    int result = bfis.read(buf);
                    if (result == -1) {
                        System.out.println("end of stream reached");
                        System.out.println("bytesRead = " + bytesRead);
                        file.put(chunkID, chunk);
                        return file;
                    }
                    chunk[i] = buf[0];
                    bytesRead += 1;
                    fileSize--;
                }
                file.put(chunkID, chunk);
                if (fileSize == 0) {
                    System.out.println("bytesRead = " + bytesRead);
                    return file;
                }
            }

            System.out.println("bytesRead = " + bytesRead);
            //file.put(chunkID, chunk);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return file;
    }

    public static void conFile(String user, String fname, HashMap<Integer, byte[]> chunks, int fileSize) {
        System.out.println("Constructing File");
        try {
            File f = new File(user + "." + fname);
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            BufferedOutputStream fbos = new BufferedOutputStream(fos);
            byte[] b;
            byte[] buf = new byte[1];

            for (int i = 0; i < chunks.size(); i++) {
                b = chunks.get(i);
                for (int j = 0; j < b.length; j++) {
                    buf[0] = b[j];
                    fbos.write(buf);
                    fileSize--;
                    if (fileSize == 0) {
                        fbos.flush();
                        fbos.close();
                        return;
                    }
                }
            }

            fbos.flush();
            fbos.close();
        } catch (IOException ex) {
        }
    }

    public static void main(String[] args) {
        HashMap<Integer, byte[]> file = new HashMap<Integer, byte[]>();

        JFileChooser jf = new JFileChooser();
        jf.showOpenDialog(jf);
        File openFile = jf.getSelectedFile();
        if (openFile == null) {
            return;
        }

        int chunkSize = 2;

        long fsize = openFile.length();
        file = cFile(openFile, chunkSize, (int) fsize);
        long temp = (openFile.length() / chunkSize) + 1;
        int numChunks = (int) temp;
        Random r = new Random();

        conFile("testFile" + r.nextInt() + " - ", openFile.getName(), file, (int) fsize);

    }

    TestChunkReconstruction() {
    }
}
