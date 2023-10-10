package proxy;

import java.util.List;

public interface HostPool {
	/**
	 * Return a collection of hosts that correspond to the provided key.
	 * Returned hosts are used for reads and writes.
	 */
	public List<String> getHosts(String key);

	/**
	 * Add a host to the system.
	 */
	public void addHost(String host);

	/**
	 * Remove a host from the system.
	 */
	public void removeHost(String host);
}