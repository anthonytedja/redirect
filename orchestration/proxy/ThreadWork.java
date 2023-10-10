package proxy;

import java.util.List;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

public class ThreadWork {

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
		private Map<String, String> cache;
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

	public ThreadWork(int replicationFactor, int maxCacheSize) {
		this.hostPool = new ConsistentRingPool(replicationFactor);
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