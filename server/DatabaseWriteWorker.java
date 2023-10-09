package server;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.sql.SQLException;

class DatabaseWriteWorker implements Runnable {
	private ThreadWork work;
	private int sleepDuration;

	private boolean verbose;
	
	public DatabaseWriteWorker(ThreadWork work, int sleepDuration, boolean verbose) {
		this.work = work;
		this.sleepDuration = sleepDuration;
		
		this.verbose = verbose;
	}
	
	public void run() {
		while (true) {
			try {
				while (work.getWriteBuffer().getSize() == 0) {
					Thread.sleep(sleepDuration);
				}

				Map<String, String> buffer = work.getWriteBuffer().flush();
				flushBuffer(buffer);
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

		if (verbose) {
			System.out.println(new Date() + ": Wrote " + buffer.keySet().size() + " entries to database");
		}
	}
}