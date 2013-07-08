package com.agiac.filechunk.chunk;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * This class is designed to construct the file from the chunks once the download
 * is complete.  It waits based on a count down latch for all the download threads
 * to finish.
 * 
 * @author Adam Giacobbe
 */
public class ConstructFile extends Thread {
    final HashMap<Integer,byte[]> chunks;
    final String user;
    final String fname;
    final long filesize;
    final CountDownLatch cdl;

    @Override
    public void run() {
        try {
            cdl.await();
            constructFile();
        } catch (InterruptedException ex) {
            Logger.getLogger(ConstructFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ConstructFile(CountDownLatch cdl, String user, String filename, long filesize, HashMap<Integer, byte[]> chunks) {
        this.user = user;
        this.fname = filename;
        this.filesize = filesize;
        this.chunks = chunks;
        this.cdl = cdl;
    }

    private void constructFile() {
        System.out.println("Constructing file");
        try {
            File f = new File(user + "." + fname);
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            BufferedOutputStream fbos = new BufferedOutputStream(fos);
            byte[] b;
            byte[] buf = new byte[1];
            int fileSize = (int) filesize;

            for (int i = 0; i < chunks.size(); i++) {
                b = chunks.get(i);
                for (int j = 0; j < b.length; j++) {
                    buf[0] = b[j];
                    fbos.write(buf);
                    fileSize--;
                    if (fileSize == 0) {
                        fbos.flush();
                        fbos.close();
                        JOptionPane.showMessageDialog(null, "Download complete, File constructed to " + f.getAbsolutePath());
                        return;
                    }
                }
            }

            fbos.flush();
            fbos.close();
            JOptionPane.showMessageDialog(null, "Download complete, file constructed to " + f.getAbsolutePath());
            return;
        } catch (IOException ex) {
        }
    }

}
