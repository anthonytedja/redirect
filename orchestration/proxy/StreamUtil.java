package proxy;

import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class StreamUtil {
    public BufferedReader in;
    public OutputStream out;

    public String firstLine = null;
    public String inMessageString = null;

    // build StreamUtil obj given a socket
    public static StreamUtil fromSocket(Socket socket) throws IOException {
        return new StreamUtil(
                new BufferedReader(
                        new InputStreamReader(socket.getInputStream())),
                socket.getOutputStream());
    }

    public StreamUtil(BufferedReader in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    // write to provided socket
    public void pipeTo(StreamUtil other) throws IOException {
        if (this.inMessageString == null) {
            throw new IOException("pipe before readOneMessage not allowed");
        }
        other.write(this.inMessageString);
    }

    public void readRequest(ParsedHttpRequest request) throws IOException {
        this.inMessageString = request.toString();
    }

    public void readResponse(ParsedHttpResponse response) throws IOException {
        this.inMessageString = response.toString();
    }

    // writes to out socket
    public void write(String text) throws IOException {
        this.out.write(text.getBytes(StandardCharsets.UTF_8));
        this.out.flush();
    }
}