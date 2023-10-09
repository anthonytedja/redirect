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
			// synchronization mechanisms force threads to wait until a socket becomes available
			try {
				Socket newClient = work.getQueue().dequeue();
				handle(newClient);
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
    }

    public void handle(Socket clientSocket) {
        // profile test
        //long startTime = System.nanoTime();
        try {  
            StreamUtil requestStream = StreamUtil.fromSocket(clientSocket);

            // read request - return if GET and already cached
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
            String host = this.work.getHostPool().getNextHostForRead(shortURL);
            System.out.println("Selected host " + host);

            String cacheHit = ParsedHttpRequest.METHOD_GET.equals(clientRequest.getHttpMethod())
                ? this.work.getCache().get(clientReqParams.get(ParsedHttpRequest.KEY_SHORT))
                : null;
            if (cacheHit != null) {
                if (VERBOSE) System.out.println("Cache hit, returning early");
                clientStream.write(cacheHit); // TODO: avoid caching entire server response
                return;
            }
            if (VERBOSE) System.out.println("NOT CACHED");

            // not in cache - need to contact server
            serverSocket = setupServerSocket(host);
            StreamUtil serverStream = StreamUtil.fromSocket(serverSocket);

            // forward client request to server and forward server response to client
            if (DEBUG) System.out.println("forwarding client to server " + host);
            clientStream.pipeTo(serverStream);
            
            ParsedHttpResponse serverResponse = new ParsedHttpResponse(serverStream.in);
            serverStream.readResponse(serverResponse);
            if (VERBOSE) {
                System.out.println(new Date() + " Server response:\n--------------\n" + serverResponse.toString() + "--------------");
            }

            // cache if client GET was successful
            if (ParsedHttpRequest.METHOD_GET.equals(clientRequest.getHttpMethod())
                && ParsedHttpResponse.STATUS_307.equals(serverResponse.getStatusCode())) {
                this.work.getCache().put(
                    clientReqParams.get(ParsedHttpRequest.KEY_SHORT),
                    serverResponse.toString()); // TODO: avoid caching entire server response
                if (VERBOSE) {
                    System.out.println("CACHED");
                }
            }

            // forward server response to client
            if (DEBUG) System.out.println("forwarding server " + host + " to client");
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