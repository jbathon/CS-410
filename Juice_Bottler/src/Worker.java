
public class Worker implements Runnable {

	private final Thread thread;
	private Orange o;
	private volatile boolean timeToWork;
	private boolean finished;

	Worker(int threadNum) {
		o = null;
		thread = new Thread(this, "Worker[" + threadNum + "]");
	}

	// Has the workers start working
	public void startWorking() {
		timeToWork = true;
		thread.start();
	}

	// Stops the workers from working
	public void stopWorking() {
		timeToWork = false;
	}

	// Waits till the thread finishes the run method and rejoins it with the main
	// program
	public void waitToStop() {
		try {
			thread.join();
		} catch (InterruptedException e) {
			System.err.println(thread.getName() + " stop malfunction");
		}
	}

	// Gives a worker an Orange
	public synchronized void aquireOrange(Orange o) {
		if (this.o == null) {
			this.o = o;
			setFinished(false);
		}
	}

	// Releases a Orange from a Worker to the plant
	public synchronized Orange releaseOrange() {
		if (o == null)
			throw new NullPointerException("Tired to release an Orange that did not exist!");

		try {
			return o;
		} finally {
			o = null;
		}
	}

	// Gets the name of the current worker
	public String getName() {
		return thread.getName();
	}

	// Checks if a Worker already has an Orange
	public synchronized boolean hasOrange() {
		return (o != null);
	}

	// Checks if the Worker has processed an Orange to the next State
	public synchronized boolean isFinished() {
		return finished;
	}

	// Sets if a Worker's Orange has been processed or not
	public synchronized void setFinished(boolean bool) {
		finished = bool;
	}

	/*
	 * If the worker is working and has an Orange the Worker will Process the next
	 * State of the Orange and then set finished to true to indicate that the Orange
	 * has be processed.
	 */
	public void run() {
		while (timeToWork) {
			if (hasOrange() && !finished) {
				o.runProcess();
				setFinished(true);
			}
		}
	}
}
