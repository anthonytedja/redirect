package server;

import java.util.Map;
import java.util.Date;
import java.sql.SQLException;

/**
 * Thread responsible for flushing the write buffer to database.
 * Checks every <SLEEP_DURATION> whether the buffer is at least <MAX_BUFFER_SIZE> and forces a flush every <MAX_CHECKS> checks.
 */
class DatabaseWriteWorker implements Runnable {
	private boolean VERBOSE;
	private int SLEEP_DURATION;
	private int MAX_BUFFER_SIZE;
	private static final int MAX_CHECKS = 1;

	private ThreadWork work;
	
	public DatabaseWriteWorker(ThreadWork work,  boolean verbose, int sleepDuration, int maxBufferSize) {
		this.VERBOSE = verbose;
		this.SLEEP_DURATION = sleepDuration;
		this.MAX_BUFFER_SIZE = maxBufferSize;

		this.work = work;
	}
	
	public void run() {
		while (true) {
			try {
				int numChecks = 0;
				while (work.getWriteBuffer().getSize() <= MAX_BUFFER_SIZE || numChecks < MAX_CHECKS) {
					numChecks++;
					Thread.sleep(SLEEP_DURATION);
				}

				Map<String, String> buffer = work.getWriteBuffer().flush();
				flushBuffer(buffer);
				numChecks = 0;
			} catch (InterruptedException e) {
				System.out.println(e);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	// flush write cache to database
	private void flushBuffer(Map<String, String> buffer) throws SQLException {
		this.work.getUrlDao().saveBatch(buffer);

		if (VERBOSE) {
			System.out.println(new Date() + ": Wrote " + buffer.keySet().size() + " entries to database");
		}
	}
}