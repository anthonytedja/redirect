package proxy;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * A load balancing system that utilized consistent hashing.
 * Currently hashes without evenly spaced partitions between servers.
 */
public class ConsistentRingPool implements HostPool {
	private static final int NUM_SLOTS = 360; // number of virtual partitions/slots on the ring
	private static final int SLOTS_PER_HOST = 3; // number of slots each host occupies

	MessageDigest md;
	List<List<String>> ring; // map number within interval [0, NUM_SLOTS] to a list of hosts
	int replicationFactor; // number of hosts to store data on (1 = no replication, 2 = two replicas, ...)

	public ConsistentRingPool(int replicationFactor) {
		try {
			this.md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e);
		}
		this.replicationFactor = replicationFactor;

		this.ring = new ArrayList<>();
		for (long i = 0; i < NUM_SLOTS; i++) {
			ring.add(new ArrayList<String>());
		}
	}

	private long hash(String key) {
		md.reset();
		md.update(key.getBytes());
		byte[] digest = md.digest();

		long hash = ((long) digest[3] & 0xFF << 24) |
				((long) digest[2] & 0xFF << 16) |
				((long) digest[1] & 0xFF << 8) |
				((long) digest[0] & 0xFF);
		return hash;
	}

	public synchronized List<String> getHosts(String key) {
		long hash = hash(key);
		int currSlot = (int) Math.abs(hash % NUM_SLOTS);
		List<String> hosts = new ArrayList<>();

		// go through ring and add consecutive hosts
		while (hosts.size() != this.replicationFactor) {
			int nextSlot = Math.abs((currSlot + 1) % NUM_SLOTS);

			// multiple hosts can be present in one slot
			List<String> slotList = this.ring.get(nextSlot);
			for (String host : slotList) {
				hosts.add(host);

				if (hosts.size() == this.replicationFactor) {
					break;
				}
			}
			currSlot = nextSlot;
		}
		return hosts;
	}

	public synchronized void addHost(String host) {
		for (int i = 0; i < SLOTS_PER_HOST; i++) {
			int slot = (int) Math.abs(hash(host + i) % NUM_SLOTS);
			this.ring.get(slot).add(host);
		}
	}

	public synchronized void removeHost(String host) {
		for (int i = 0; i < SLOTS_PER_HOST; i++) {
			int slot = (int) Math.abs(hash(host + i) % NUM_SLOTS);
			this.ring.get(slot).remove(host);
		}
	}

	@Override
	public String toString() {
		return this.ring.toString();
	}
}
