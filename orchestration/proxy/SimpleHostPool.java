package proxy;

import java.util.ArrayList;
import java.util.List;

/*
 * A simple host pool to use during debugging.
 */
public class SimpleHostPool implements HostPool {

	/**
	 * Collection of hosts that fully replicate each others' data.
	 * Selects the next host within the cluster using round robin.
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
	 */
	private final static int NUM_CLUSTERS = 2; // configurable?
	private final static int MODULO = NUM_CLUSTERS - 1;
	private Cluster[] clusters;

	public SimpleHostPool() {
		this.clusters = new Cluster[NUM_CLUSTERS];
		this.clusters[0] = new Cluster();
		this.clusters[1] = new Cluster();
	}

	// hashing strategy used for load balancing
	private int hash(String key) {
		return key.hashCode() % MODULO;
	}

	// get hosts from the cluster that is is the target of the next read/write
	public synchronized List<String> getHosts(String key) {
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
