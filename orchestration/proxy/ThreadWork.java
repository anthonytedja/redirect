package proxy;

import java.util.List;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class ThreadWork {

	/**
	 * Collection of hosts that fully replicate each others' data.
	 * Selects the next host within the cluster using round robin.
	 * 
	 * See the report for details regarding design decisions.
	 */
	class Cluster {
		private List<String> hosts;
		private int totalHosts;
		private int currHost;

		public Cluster() {
			this.hosts = new ArrayList<>();
			this.totalHosts = 0;
			this.currHost = 0;
		}

		public synchronized void add(String host) {
			this.hosts.add(host);
			this.totalHosts++;
		}

		public synchronized void remove(String host) {
			this.hosts.remove(host);
			this.totalHosts--;
			this.currHost = (currHost) % this.totalHosts;
		}

		// select next host using round robin
		public synchronized String getNextHost() {
			String nextHost = hosts.get(currHost);
			this.currHost = (this.currHost + 1) % this.totalHosts;
			return nextHost;
		}

		public synchronized List<String> getAllHosts() {
			return this.hosts;
		}

		public synchronized int getSize() {
			return this.hosts.size();
		}

		public String toString() {
			return this.hosts.toString();
		}
	}

	/**
	 * Data is partioned across clusters (data is replicated within a cluster).
	 * Load balances using a static hashing method. Currently only maps to
	 * two clusters as the number of available hosts is constrained.
	 * 
	 * See the report for details regarding design decisions.
	 */
	class HostPool {
		private final static int NUM_CLUSTERS = 2; // configurable?
		private Cluster[] clusters;

		public HostPool() {
			this.clusters = new Cluster[NUM_CLUSTERS];
			this.clusters[0] = new Cluster();
			this.clusters[1] = new Cluster();
		}

		// hashing strategy used for load balancing
		private int hash(String key) {
			return key.hashCode() % NUM_CLUSTERS;
		}

		// get a host from the cluster that is is the target of the next read
		public synchronized String getNextHostForRead(String key) {
			int mappedCluster = hash(key);
			return this.clusters[mappedCluster].getNextHost();
		}

		// get hosts from the cluster that is the target of the next write
		public synchronized List<String> getNextHostsForWrite(String key) {
			int mappedCluster = hash(key);
			return this.clusters[mappedCluster].getAllHosts();
		}

		// add a host to the smallest cluster
		public void addHost(String host) {
			Cluster minCluster = this.clusters[0];
			int minSize = minCluster.getSize();

			for (Cluster cluster : clusters) {
				int size = cluster.getSize();
				if (size < minSize) {
					minCluster = cluster;
					minSize = size;
				}
			}

			minCluster.add(host);
		}

		public void removeHost(String host) {
			for (Cluster cluster : clusters) {
				cluster.remove(host);
			}
		}

		public String toString() {
			StringBuilder output = new StringBuilder();
			for (int i = 0; i < clusters.length; i++) {
				output.append("Cluster " + i + ": " + clusters[i] + "\n");
			}
			return output.toString();
		}
	}

	class SocketQueue {
		private List<Socket> queue;

		SocketQueue() {
			this.queue = new LinkedList<Socket>();
		}

		public synchronized void enqueue(Socket client) throws InterruptedException { // no limit?
			this.queue.add(client);
			notifyAll();
		}

		public synchronized Socket dequeue() throws InterruptedException {
			while (!isAvailable()) {
				wait();
			}
			Socket client = this.queue.remove(0);
			notifyAll();
			return client;
		}

		private boolean isAvailable() {
			return this.queue.size() > 0;
		}
	}

	class LRUCache { // TODO
		private HashMap<String, String> cache;
		private int maxSize;

		LRUCache(int maxSize) {
			this.cache = new HashMap<String, String>();
			this.maxSize = maxSize;
		}

		public synchronized String get(String key) {
			return cache.get(key);
		}

		public synchronized void put(String key, String value) {
			cache.put(key, value);
		}

		public int getMaxSize() {
			return this.maxSize;
		}
	}

	private HostPool hostPool;
	private SocketQueue queue;
	private LRUCache cache;

	public ThreadWork(int maxCacheSize) {
		//this.buffer = new WriteBuffer(maxBufferSize);
		this.hostPool = new HostPool();
		this.queue = new SocketQueue();
		this.cache = new LRUCache(maxCacheSize);
	}

	public HostPool getHostPool() {
		return this.hostPool;
	}

	public SocketQueue getQueue() {
		return this.queue;
	}

	public LRUCache getCache() {
		return this.cache;
	}

}