package mbpmcsn.center;

import java.util.Queue;
import java.util.LinkedList;

import mbpmcsn.entity.Job;

public final class SssqStatus {
	private final Queue<Job> queue = new LinkedList<>();
	private boolean activeServer;

	public Queue<Job> getQueue() {
		return queue;
	}

	public boolean hasActiveServer() {
		return activeServer;
	}

	public void setActiveServer(boolean activeServer) {
		this.activeServer = activeServer;
	}
}
