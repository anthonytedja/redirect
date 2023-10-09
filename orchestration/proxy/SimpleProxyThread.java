package proxy;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleProxyThread extends Thread {
    private boolean VERBOSE = true;
    private boolean DEBUG = true;
    
    private int hostport;
    private Socket clientSocket;
    private String HOST = null;
    private Cache readcache;
    private ThreadWork work;


    public SimpleProxyThread(Socket socket, int hostport, ThreadWork work, Cache readcache) {
        this.clientSocket = socket;
        this.hostport = hostport;
        this.readcache = readcache;

        this.work = work;
    }

    private Socket setupServerSocket() throws IOException {
        try {
            return new Socket(this.HOST, this.hostport);
        } catch (IOException e) {
            System.err.println("Proxy server cannot connect to " + this.HOST + ":" + this.hostport + ":\n" + e);
            throw e;
        }
    }

    public void run() {
        // profile test
        //long startTime = System.nanoTime();

        Socket clientSocket = this.clientSocket;
        try {  
            StreamUtil requestStream = StreamUtil.fromSocket(clientSocket);

            // read request - return if GET and already cached
            ParsedHttpRequest request = new ParsedHttpRequest(requestStream.in);
            Map<String, String> reqParams = request.getParams();
            requestStream.readRequest(request);
            if (VERBOSE) {
                System.out.println(new Date() + " Client request:\n--------------\n" + request.toString() + "--------------");
            }

            // for now, parsing is done by examining the first line of the request
            // refer to documentation for the API

            // request from orchestration
            if (reqParams.containsKey(ParsedHttpRequest.KEY_NEWHOST)) {
                handleAddHost(reqParams);
            }
            if (reqParams.containsKey(ParsedHttpRequest.KEY_OLDHOST)) {
                handleRemoveHost(reqParams);
            }

            // request from client
            if (reqParams.containsKey(ParsedHttpRequest.KEY_SHORT)) {
                handleClient(requestStream, request, reqParams);
            }
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            try {
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e);
            }
        }

        // profile test
        //long endTime = System.nanoTime();
        //System.out.println(String.format("Thread took: 2%f ms", ((double) endTime - startTime) / 1000000));
    }

    private void handleAddHost(Map<String, String> reqParams) {
        String addHost = reqParams.get(ParsedHttpRequest.KEY_NEWHOST);
        if (DEBUG) {
            System.out.println("Adding host " + addHost);
            System.out.println(this.work.getHostPool());
        }
        this.work.getHostPool().addHost(addHost);
        if (DEBUG) {
            System.out.println(this.work.getHostPool());
        }
    }

    private void handleRemoveHost(Map<String, String> reqParams) {
        String removeHost = reqParams.get(ParsedHttpRequest.KEY_OLDHOST);
        if (DEBUG) {
            System.out.println("Removing host " + removeHost);
            System.out.println(this.work.getHostPool());
        }
        this.work.getHostPool().removeHost(removeHost);
        if (DEBUG) {
            System.out.println(this.work.getHostPool());
        }
    }

    private void handleClient(StreamUtil clientStream, ParsedHttpRequest clientRequest, Map<String, String> clientReqParams) {
        Socket serverSocket = null;
        try {
            String shortURL = clientReqParams.get(ParsedHttpRequest.KEY_SHORT);
            // determine server based on shortURL ???
            this.HOST = this.work.getHostPool().getNextHostForRead(ParsedHttpRequest.KEY_SHORT);
            System.out.println("Selected host " + this.HOST);

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
            if (DEBUG) System.out.println("forwarding client to server " + this.HOST);
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
            if (DEBUG) System.out.println("forwarding server " + this.HOST + " to client");
            serverStream.pipeTo(clientStream);
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            try {
                if (serverSocket != null) serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e);
            }
        }
    }
}