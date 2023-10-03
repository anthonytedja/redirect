package proxy;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Arrays;
import java.util.List;

public class SimpleProxyThread extends Thread{
    private int HOSTPORT = 8085;
    private List<String> HOSTS = Arrays.asList("dh2026pc01", "dh2026pc02", "dh2026pc03", "dh2026pc04");
    private Socket socket = null;
    private String HOST = null;

    private String chooseHost() {
        Random rand = new Random();
        String chosen = this.HOSTS.get(rand.nextInt(this.HOSTS.size()));
        //System.out.println("host chosen: " + chosen);

        return chosen;
    }

    // read request
    // cache if already known (early return)
    // transfer client in to host in
    // wait for host response
    // transfer host out to client out
    // record host out in cache
    // return

    public SimpleProxyThread(Socket socket) {
        this.socket = socket;
        this.HOST = this.chooseHost();
    }

    public void run() {
        final byte[] request = new byte[1024];
        byte[] reply = new byte[4096];
        Socket server = null;
        try {
            final InputStream streamFromClient = this.socket.getInputStream();
            final OutputStream streamToClient = this.socket.getOutputStream();

            // Make a connection to the real server.
            // If we cannot connect to the server, send an error to the
            // client, disconnect, and continue waiting for connections.
            try {
                server = new Socket(this.HOST, HOSTPORT);
            } catch (IOException e) {
                PrintWriter out = new PrintWriter(streamToClient);
                out.print("Proxy server cannot connect to " + this.HOST + ":"
                    + HOSTPORT + ":\n" + e + "\n");
                out.flush();
                this.socket.close();
                return;
            }

            // Get server streams.
            final InputStream streamFromServer = server.getInputStream();
            final OutputStream streamToServer = server.getOutputStream();

            // a thread to read the client's requests and pass them
            // to the server. A separate thread for asynchronous.
            Thread t = new Thread() {
            public void run() {
                int bytesRead;
                try {
                    while ((bytesRead = streamFromClient.read(request)) != -1) {
                        streamToServer.write(request, 0, bytesRead);
                        streamToServer.flush();
                    }
                } catch (IOException e) {
                }

                // the client closed the connection to us, so close our
                // connection to the server.
                try {
                    streamToServer.close();
                } catch (IOException e) {
                }
            }
            };

            // Start the client-to-server request thread running
            t.start();

            // Read the server's responses
            // and pass them back to the client.
            int bytesRead;
            try {
                while ((bytesRead = streamFromServer.read(reply)) != -1) {
                    streamToClient.write(reply, 0, bytesRead);
                    streamToClient.flush();
                }
            } catch (IOException e) {
            }

            // The server closed its connection to us, so we close our
            // connection to our client.
            streamToClient.close();
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            try {
                if (server != null)
                    server.close();
                if (this.socket != null)
                    this.socket.close();
            } catch (IOException e) {
            }
        }
    }
}