package proxy;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ConsistentRingPool implements HostPool {
	MessageDigest md;
	NavigableMap<Long, String> ring;
	int replicationFactor;
	
	public ConsistentRingPool(int replicationFactor) throws NoSuchAlgorithmException {
		this.md = MessageDigest.getInstance("MD5");
		this.ring = new TreeMap<>();
		this.replicationFactor = replicationFactor;
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

	public List<String> getHosts(String key) {
		return null;
	}

	public void addHost(String host) {
		return;
	}

	public void removeHost(String host) {
		return;
	}

	@Override
	public String toString() {
		return null;
	}
}
