package server;

import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.net.Socket;

/**
 * Collection of data necessary for concurrent thread work.
 * Provides a central location for each thread to obtain configurations from.
 * 
 * Maintains a queue of sockets that threads can pull from as necessary.
 */
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

	class WriteBuffer {
		private HashMap<String, String> buffer;
		private int maxSize; // remove?

		WriteBuffer(int maxSize) {
			this.buffer = new HashMap<String, String>();
			this.maxSize = maxSize;
		}

		public synchronized int getSize() {
			return this.buffer.size();
		}

		public int getMaxSize() {
			return this.maxSize;
		}

		// dump and re-initialize buffer
		public synchronized HashMap<String, String> flush() throws InterruptedException { // called by write thread
			/*
			while (!isFull()) {
				wait();
			}
			*/
			HashMap<String, String> currBuffer = this.buffer;
			this.buffer = new HashMap<String, String>();

			return currBuffer;
		}

		public synchronized void put(String key, String value) throws InterruptedException {
			this.buffer.put(key, value);

			/*
			if (isFull()) {
				notifyAll(); // wake up write thread if necessary
			}
			*/
		}
	}

	private SocketQueue queue;
	private LRUCache cache;
	private WriteBuffer buffer;
	private UrlDao urlDao;

	public ThreadWork(int maxCacheSize, int maxBufferSize, String dbPath) {
		this.queue = new SocketQueue();
		this.cache = new LRUCache(maxCacheSize);
		this.buffer = new WriteBuffer(maxBufferSize);
		this.urlDao = new UrlDao(dbPath);
	}

	public SocketQueue getQueue() {
		return this.queue;
	}

	public LRUCache getCache() {
		return this.cache;
	}

	public WriteBuffer getWriteBuffer() {
		return this.buffer;
	}

	public UrlDao getUrlDao() {
		return this.urlDao;
	}
}