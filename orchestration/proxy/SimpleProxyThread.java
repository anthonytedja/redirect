package proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Date;
import java.util.List;

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
                else if (ParsedHttpRequest.METHOD_PUT.equals(request.getHttpMethod())
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
            System.out.println("Thread " + this.threadId + ": Adding host " + addHost);
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
            System.out.println("Thread " + this.threadId + ": Removing host " + removeHost);
            System.out.println(this.work.getHostPool());
        }
        this.work.getHostPool().removeHost(removeHost);
        if (DEBUG) {
            System.out.println(this.work.getHostPool());
        }
    }

    private void handleClientGet(StreamUtil clientStream, ParsedHttpRequest clientRequest, Map<String, String> clientReqParams) throws IOException {
        String shortURL = clientReqParams.get(ParsedHttpRequest.KEY_SHORT);

        // return if already cached
        String cacheHit = this.work.getCache().get(clientReqParams.get(ParsedHttpRequest.KEY_SHORT));
        if (cacheHit != null) {
            if (VERBOSE) System.out.println("Thread " + this.threadId + ": CACHE HIT");
            clientStream.write(cacheHit); // TODO: avoid caching entire server response
            return;
        }
        if (VERBOSE) System.out.println("Thread " + this.threadId + ": CACHE MISS");

        // check for data across replicated hosts
        List<String> hosts = this.work.getHostPool().getHosts(shortURL);
        if (DEBUG) System.out.println("Thread " + this.threadId + ": Selected hosts for read " + hosts);
        ParsedHttpResponse successRes = null;
        ParsedHttpResponse failRes = null;

        for (String host : hosts) {
            Socket serverSocket = null;
            try {
                // forward client request to server
                serverSocket = setupServerSocket(host);
                StreamUtil serverStream = StreamUtil.fromSocket(serverSocket);

                if (DEBUG) System.out.println("Thread " + this.threadId + ": forwarding client to server " + host);
                clientStream.pipeTo(serverStream);
                
                // parse server response
                ParsedHttpResponse serverResponse = new ParsedHttpResponse(serverStream.in);
                switch (serverResponse.getStatusCode()) {
                    case ParsedHttpResponse.STATUS_307:
                        successRes = serverResponse;

                        // cache if client GET was successful
                        this.work.getCache().put(
                            clientReqParams.get(ParsedHttpRequest.KEY_SHORT),
                            serverResponse.toString()); // TODO: avoid caching entire server response
                        if (VERBOSE) {
                            System.out.println("Thread " + this.threadId + ": CACHE UPDATE");
                        }
                        break;
                    case ParsedHttpResponse.STATUS_404:
                        failRes = serverResponse;
                        break;
                }
                if (VERBOSE) {
                    System.out.println(new Date() + " Server response:\n--------------\n" + serverResponse.toString() + "--------------");
                }
                serverSocket.close();
            } catch (Exception e) {
                System.err.println("Thread " + this.threadId + ": socket error for host " + host);
                System.err.println(e);
            } finally {
                if (serverSocket != null) serverSocket.close();
                
                // return first successful response
                if (successRes != null) {
                    break;
                }
            }
        }

        // forward success response to client if at least one succeeds
        ParsedHttpResponse sendToClient = null;
        if (successRes != null) {
            sendToClient = successRes;
        } else if (failRes != null) {
            sendToClient = failRes;
        }
        if (sendToClient != null) {
            if (DEBUG) System.out.println("Thread " + this.threadId + ": forwarding a server response to client");
            clientStream.write(sendToClient.toString());
        }
    }

    private void handleClientPut(StreamUtil clientStream, ParsedHttpRequest clientRequest, Map<String, String> clientReqParams) throws IOException {
        String shortURL = clientReqParams.get(ParsedHttpRequest.KEY_SHORT);
        
        // perform write across replicated hosts
        List<String> hosts = this.work.getHostPool().getHosts(shortURL);
        if (DEBUG) System.out.println("Thread " + this.threadId + ": Selected hosts for write: " + hosts);
        ParsedHttpResponse successRes = null;
        ParsedHttpResponse failRes = null;

        for (String host : hosts) {
            Socket serverSocket = null;
            try {
                // forward client request to server
                serverSocket = setupServerSocket(host);
                StreamUtil serverStream = StreamUtil.fromSocket(serverSocket);

                if (DEBUG) System.out.println("Thread " + this.threadId + ": forwarding client to server " + host);
                clientStream.pipeTo(serverStream);
                
                // parse server response
                ParsedHttpResponse serverResponse = new ParsedHttpResponse(serverStream.in);
                switch (serverResponse.getStatusCode()) {
                    case ParsedHttpResponse.STATUS_200:
                        successRes = serverResponse;
                        break;
                    case ParsedHttpResponse.STATUS_404: // not triggered?
                        failRes = serverResponse;
                        break;
                }
                if (VERBOSE) {
                    System.out.println(new Date() + " Server " + host + " response:\n--------------\n" + serverResponse.toString() + "--------------");
                }
                serverSocket.close();
            } catch (Exception e) {
                System.err.println("Thread " + this.threadId + ": socket error for host " + host);
                System.err.println(e);
            } finally {
                if (serverSocket != null) serverSocket.close();
            }
        }

        // forward success response to client if at least one succeeds
        ParsedHttpResponse sendToClient = null;
        if (successRes != null) {
            sendToClient = successRes;
        } else if (failRes != null) {
            sendToClient = failRes;
        }
        if (sendToClient != null) {
            if (DEBUG) System.out.println("Thread " + this.threadId + ": forwarding a server response to client");
            clientStream.write(sendToClient.toString());
        }
    }
}