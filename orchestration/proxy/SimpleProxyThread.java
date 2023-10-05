package proxy;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Arrays;
import java.util.List;

public class SimpleProxyThread extends Thread{
    private boolean VERBOSE = false;
    private int HOSTPORT = 8085;
    private List<String> HOSTS = Arrays.asList("dh2026pc01", "dh2026pc02", "dh2026pc03", "dh2026pc04");
    private Socket clientSocket;
    private String HOST = null;
    private Cache readcache;

    private String chooseHost() {
        Random rand = new Random();
        String chosen = this.HOSTS.get(rand.nextInt(this.HOSTS.size()));
        //System.out.println("host chosen: " + chosen);
        return chosen;
    }

    public SimpleProxyThread(Socket socket, Cache readcache) {
        this.clientSocket = socket;
        this.readcache = readcache;
        this.HOST = this.chooseHost();
    }

    private String getCacheForRequest(String requestText) {
        String method = HttpUtil.extractMethod(requestText);
        if (!method.equals("GET")) return null;

        String path = HttpUtil.extractPath(requestText);
        return this.readcache.get(path);
    }

    private void setCacheForRequest(String requestText, String responseText) {
        String method = HttpUtil.extractMethod(requestText);
        if (VERBOSE) System.out.println("METHOD: |" + method + "|");
        if (!method.equals("GET")) return;

        String path = HttpUtil.extractPath(requestText);
        String redirect = HttpUtil.extractRedirect(responseText);

        this.readcache.set(path, redirect);

        if (VERBOSE) System.out.println("CACHE" + this.readcache);
    }

    private Socket setupServerSocket() throws IOException {
        try {
            return new Socket(this.HOST, HOSTPORT);
        } catch (IOException e) {
            System.err.println("Proxy server cannot connect to " + this.HOST + ":" + HOSTPORT + ":\n" + e);
            throw e;
        }
    }

    public void run() {
        // profile test
        long startTime = System.nanoTime();

        Socket clientSocket = this.clientSocket;
        Socket serverSocket = null;

        try {  
            if (VERBOSE) System.out.println("Started thread for host: " + this.HOST);

            StreamUtil clientStream = StreamUtil.fromSocket(clientSocket);

            // read request - return cache if GET and already known
            if (VERBOSE) System.out.println("Attempting to readAll client");
            clientStream.readOneMessage();
            if (VERBOSE) System.out.println("Done reading");
            String clientRequestText = clientStream.inMessageString;
            String cacheHit = this.getCacheForRequest(clientRequestText);

            if (VERBOSE) System.out.println("Client request: " + clientRequestText);
            if (true && cacheHit != null) {
                if (VERBOSE) System.out.println("Cache hit, returning early");
                clientStream.write(HttpUtil.formatRedirect(cacheHit));
                return;
            }

            if (VERBOSE) System.out.println("NOT CACHED");

            // setup server socket
            serverSocket = this.setupServerSocket();
            StreamUtil serverStream = StreamUtil.fromSocket(serverSocket);

            // forward client request to server and forward server response to client
            if (VERBOSE) System.out.println("forwarding client to server");
            clientStream.pipeTo(serverStream);
            
            /*
                problem: client buffer won't close automatically
                server buffer closes on response
                we need to access client buffer to get request data before talking to server

                possible solution: keep reading until we see \r\n\r\n, indicating the end of the request
                rename readAll to readFullRequest and parse accordingly

            */

            // set cache for GET
            serverStream.readOneMessage();
            String serverResponseText = serverStream.inMessageString;
            if (VERBOSE) System.out.println("server response: " + serverResponseText);
            this.setCacheForRequest(clientRequestText, serverResponseText);

            // forward server response to client
            if (VERBOSE) System.out.println("forwarding server to client");
            serverStream.pipeTo(clientStream);

            
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            try {
                if (serverSocket != null) serverSocket.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
            }

            // profile test
            long endTime = System.nanoTime();
            System.out.println(String.format("Thread took: 2%f ms", ((double) endTime - startTime) / 1000000));
        }
    }
}