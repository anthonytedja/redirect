package proxy;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;
import java.net.*;

public class StreamUtil {
    public BufferedReader in;
    public OutputStream out;

    public String inMessageString = null;

    public static StreamUtil fromSocket(Socket socket) throws IOException {
        return new StreamUtil(
            new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            ),
            socket.getOutputStream()
        );
    }

    public StreamUtil(
        BufferedReader in,
        OutputStream out
    ) {
        this.in = in;
        this.out = out;
    }

    // private void transferInputToOutput(InputStream in, OutputStream out) throws IOException {
    //     byte[] buffer = new byte[8192];
    //     int bytesRead;

    //     while ((bytesRead = in.read(buffer)) != -1) {
    //         out.write(buffer, 0, bytesRead);
    //     }
    // }

    public void pipeTo(StreamUtil other) throws IOException {
        if (this.inMessageString == null) throw new IOException("pipe before readOneMessage not allowed");
        //InputStream inStringAsStream = new ByteArrayInputStream(this.inRequestString.getBytes());
        
        other.write(this.inMessageString);
        // replace this with string thing idk //this.transferInputToOutput(this.in, other.out);
        other.out.flush();
        //other.out.close(); // maybe enable this for performance but i dont think it'll do anything
    }

    public void readOneMessage() throws IOException {
        // String readString = new String(this.in.readAllBytes(), StandardCharsets.UTF_8);
        // System.out.println(readString);
        // this.inString = readString;
        List<String> requestLines = new ArrayList<String>();
        String inputLine;
        while (!(inputLine = this.in.readLine()).equals("")){
            requestLines.add(inputLine);
            //System.out.println("read: " + inputLine);
        }

        this.inMessageString = String.join("\n", requestLines);
    }

    public void write(String text) throws IOException {
        this.out.write(text.getBytes(StandardCharsets.UTF_8));        
    }
}