package proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Date;

public class SimpleProxyThread extends Thread {
    private boolean VERBOSE;
    private boolean DEBUG = true; // use for debugging load balancing
    private int HOST_PORT;
    
    private int threadId;
    private ThreadWork work;


    public SimpleProxyThread(int threadId, ThreadWork work, int hostport, boolean verbose) {
        this.VERBOSE = verbose;
        this.HOST_PORT = hostport;
        
        this.threadId = threadId;
        this.work = work;
    }

    private Socket setupServerSocket(String host) throws IOException {
        try {
            return new Socket(host, this.HOST_PORT);
        } catch (IOException e) {
            System.err.println("Proxy server cannot connect to " + host + ":" + this.HOST_PORT + ":\n" + e);
            throw e;
        }
    }

    public void run() {
        // wait for socket to become available - check socket queue
        while (true) {
            Socket newConn = null;
            try {
                newConn = work.getQueue().dequeue();
                StreamUtil requestStream = StreamUtil.fromSocket(newConn);

                // parse request
                ParsedHttpRequest request = new ParsedHttpRequest(requestStream.in);
                Map<String, String> reqParams = request.getParams();
                requestStream.readRequest(request);
                if (DEBUG) {
                    System.out.println(new Date() + ": Thread " + this.threadId);
                }
                if (VERBOSE) {
                    Date currTime = new Date();
                    System.out.println(currTime + ": Thread " + this.threadId);
                    System.out.println(currTime + " Client request:\n--------------\n" + request.toString() + "--------------");
                }

                // request from orchestration
                if (reqParams.containsKey(ParsedHttpRequest.KEY_NEWHOST)) {
                    handleAddHost(reqParams);
                }
                if (reqParams.containsKey(ParsedHttpRequest.KEY_OLDHOST)) {
                    handleRemoveHost(reqParams);
                }

                // request from client
                if (ParsedHttpRequest.METHOD_GET.equals(request.getHttpMethod())
                    && reqParams.containsKey(ParsedHttpRequest.KEY_SHORT)) {
                    handleClientGet(requestStream, request, reqParams);
                }
                if (ParsedHttpRequest.METHOD_PUT.equals(request.getHttpMethod())
                    && reqParams.containsKey(ParsedHttpRequest.KEY_SHORT)) {
                    handleClientPut(requestStream, request, reqParams);
                }

            } catch (Exception e) {
                System.err.println(e);
            } finally {
                try {
                    if (newConn != null) newConn.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
		}
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

    private void handleClientGet(StreamUtil clientStream, ParsedHttpRequest clientRequest, Map<String, String> clientReqParams) throws IOException {
        Socket serverSocket = null;
        try {
            String shortURL = clientReqParams.get(ParsedHttpRequest.KEY_SHORT);
            // determine server based on shortURL ???
            String host = this.work.getHostPool().getNextHostForRead(shortURL);
            System.out.println("Selected host " + host);

            // return if already cached
            String cacheHit = this.work.getCache().get(clientReqParams.get(ParsedHttpRequest.KEY_SHORT));
            if (cacheHit != null) {
                if (VERBOSE) System.out.println("Cache hit, returning early");
                clientStream.write(cacheHit); // TODO: avoid caching entire server response
                return;
            }
            if (VERBOSE) System.out.println("NOT CACHED");

            // not in cache - forward client request to server
            serverSocket = setupServerSocket(host);
            StreamUtil serverStream = StreamUtil.fromSocket(serverSocket);

            if (DEBUG) System.out.println("forwarding client to server " + host);
            clientStream.pipeTo(serverStream);
            
            // parse server response
            ParsedHttpResponse serverResponse = new ParsedHttpResponse(serverStream.in);
            serverStream.readResponse(serverResponse);
            if (VERBOSE) {
                System.out.println(new Date() + " Server response:\n--------------\n" + serverResponse.toString() + "--------------");
            }

            // cache if client GET was successful
            if (ParsedHttpResponse.STATUS_307.equals(serverResponse.getStatusCode())) {
                this.work.getCache().put(
                    clientReqParams.get(ParsedHttpRequest.KEY_SHORT),
                    serverResponse.toString()); // TODO: avoid caching entire server response
                if (VERBOSE) {
                    System.out.println("ADDED TO CACHE");
                }
            }

            // forward server response to client
            if (DEBUG) System.out.println("forwarding server " + host + " to client");
            serverStream.pipeTo(clientStream);
        } finally {
            if (serverSocket != null) serverSocket.close();
        }
    }

    private void handleClientPut(StreamUtil clientStream, ParsedHttpRequest clientRequest, Map<String, String> clientReqParams) throws IOException {
        Socket serverSocket = null;
        try {
            String shortURL = clientReqParams.get(ParsedHttpRequest.KEY_SHORT);
            // determine server based on shortURL ???
            String host = this.work.getHostPool().getNextHostForRead(shortURL);
            System.out.println("Selected host " + host);

            // forward client request to server
            serverSocket = setupServerSocket(host);
            StreamUtil serverStream = StreamUtil.fromSocket(serverSocket);

            if (DEBUG) System.out.println("forwarding client to server " + host);
            clientStream.pipeTo(serverStream);
            
            // parse server response
            ParsedHttpResponse serverResponse = new ParsedHttpResponse(serverStream.in);
            serverStream.readResponse(serverResponse);
            if (VERBOSE) {
                System.out.println(new Date() + " Server response:\n--------------\n" + serverResponse.toString() + "--------------");
            }

            // forward server response to client
            if (DEBUG) System.out.println("forwarding server " + host + " to client");
            serverStream.pipeTo(clientStream);
        } finally {
            if (serverSocket != null) serverSocket.close();
        }
    }
}