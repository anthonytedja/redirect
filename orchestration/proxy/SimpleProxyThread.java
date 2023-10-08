package proxy;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.HashMap;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleProxyThread extends Thread {
    private boolean VERBOSE = true;
    private int HOSTPORT = 8085;
    
    private Socket clientSocket;
    private String HOST = null;
    private Cache readcache;
    private ThreadWork work;


    public SimpleProxyThread(Socket socket, ThreadWork work, Cache readcache) {
        this.clientSocket = socket;
        this.readcache = readcache;

        this.work = work;

        this.HOST = this.work.getHostPool().getNextHostForRead("key");
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
        //long startTime = System.nanoTime();

        Socket clientSocket = this.clientSocket;
        Socket serverSocket = null;

        try {  
            if (VERBOSE) System.out.println("Started thread for host: " + this.HOST);

            StreamUtil clientStream = StreamUtil.fromSocket(clientSocket);

            // read request - return if GET and already cached
            ParsedHttpRequest clientRequest = new ParsedHttpRequest(clientStream.in);
            HashMap<String, String> clientReqParams = clientRequest.getParams();
            clientStream.readRequest(clientRequest);
            if (VERBOSE) {
                System.out.println(new Date() + " Client request:\n--------------\n" + clientRequest.toString() + "--------------");
            }

            String cacheHit = ParsedHttpRequest.METHOD_GET.equals(clientRequest.getHttpMethod())
                ? this.readcache.get(clientReqParams.get(ParsedHttpRequest.KEY_SHORT))
                : null;
            if (cacheHit != null) {
                if (VERBOSE) System.out.println("Cache hit, returning early");
                clientStream.write(cacheHit); // TODO: avoid caching entire server response
                return;
            }
            if (VERBOSE) System.out.println("NOT CACHED");

            // not in cache - need to contact server
            serverSocket = this.setupServerSocket();
            StreamUtil serverStream = StreamUtil.fromSocket(serverSocket);

            // forward client request to server and forward server response to client
            if (VERBOSE) System.out.println("forwarding client to server " + this.HOST);
            clientStream.pipeTo(serverStream);
            
            ParsedHttpResponse serverResponse = new ParsedHttpResponse(serverStream.in);
            serverStream.readResponse(serverResponse);
            if (VERBOSE) {
                System.out.println(new Date() + " Server response:\n--------------\n" + serverResponse.toString() + "--------------");
            }

            // cache if client GET was successful
            if (ParsedHttpRequest.METHOD_GET.equals(clientRequest.getHttpMethod())
                && ParsedHttpResponse.STATUS_307.equals(serverResponse.getStatusCode())) {
                this.readcache.set(
                    clientReqParams.get(ParsedHttpRequest.KEY_SHORT),
                    serverResponse.toString()); // TODO: avoid caching entire server response
                if (VERBOSE) {
                    System.out.println("CACHED");
                }
            }

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
            //long endTime = System.nanoTime();
            //System.out.println(String.format("Thread took: 2%f ms", ((double) endTime - startTime) / 1000000));
        }
    }

    private void handleClient() {
        //
    }

    private void handleOrchestration() {
        //
    }
}