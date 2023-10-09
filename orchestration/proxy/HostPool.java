package proxy;

import java.util.List;

public interface HostPool {
	/**
	 * Get a collection of hosts for an operation, such as a read or a write.
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