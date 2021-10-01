import java.util.*; //importing the stack class

public class Plant implements Runnable {
	// How long do we want to run the juice processing
	public static final long PROCESSING_TIME = 5 * 1000;
	private static final int NUM_PLANTS = 2;
	private static final int NUM_WORKERS = 4;

	public static void main(String[] args) {
		// Startup the plants
		Plant[] plants = new Plant[NUM_PLANTS];
		for (int i = 0; i < NUM_PLANTS; i++) {
			plants[i] = new Plant(i);
			plants[i].startPlant();
		}

		// Give the plants time to do work
		delay(PROCESSING_TIME, "Plant malfunction");

		// Stop the workers, and wait for them to leave the plant

		for (Plant p : plants) {
			for (Worker w : p.workers) {
				w.stopWorking();
			}
		}
		for (Plant p : plants) {
			for (Worker w : p.workers) {
				w.waitToStop();
			}
		}

		// Stop the plant, and wait for it to shutdown

		for (Plant p : plants) {
			p.stopPlant();
		}
		for (Plant p : plants) {
			p.waitToStop();
		}

		// Summarize the results
		int totalProvided = 0;
		int totalProcessed = 0;
		int totalBottles = 0;
		int totalWasted = 0;
		for (Plant p : plants) {
			totalProvided += p.getProvidedOranges();
			totalProcessed += p.getProcessedOranges();
			totalBottles += p.getBottles();
			totalWasted += p.getWaste();
		}
		System.out.println("Total provided/processed = " + totalProvided + "/" + totalProcessed);
		System.out.println("Created " + totalBottles + ", wasted " + totalWasted + " oranges");
	}

	private static void delay(long time, String errMsg) {
		long sleepTime = Math.max(1, time);
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			System.err.println(errMsg);
		}
	}

	public final int ORANGES_PER_BOTTLE = 3;

	private final Thread thread;
	private int orangesProvided;
	private int orangesProcessed;
	private volatile boolean timeToWork;

	private Queue<Orange> processingQueue;
	private Worker[] workers;

	Plant(int threadNum) {
		orangesProvided = 0;
		orangesProcessed = 0;
		thread = new Thread(this, "Plant[" + threadNum + "]");
		processingQueue = new LinkedList<Orange>();

		workers = new Worker[NUM_WORKERS];
		for (int i = 0; i < NUM_WORKERS; i++) {
			workers[i] = new Worker(i);
			workers[i].startWorking();
		}

	}

	// Starts the Plant
	public void startPlant() {
		timeToWork = true;
		thread.start();
	}

	// Stops the Plant
	public void stopPlant() {
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

	public void run() {
		Worker w;
		Orange o;
		System.out.println(Thread.currentThread().getName() + " Online");
		/*
		 * While the thread is a online give oranges to workers with out a orange. If a
		 * Worker has finished there task and it the orange is not bottled put it back
		 * in the processingQueue else add one to orangesProcessed.
		 */
		while (timeToWork) {
			w = getWorker();
			if (processingQueue.isEmpty() && w != null) {
				System.out.println(Thread.currentThread().getName() + " " + w.getName() + " Fetched");
				w.aquireOrange(new Orange());
				orangesProvided++;
			}
			if (!processingQueue.isEmpty() && w != null) {
				w.aquireOrange(processingQueue.remove());
			}
			w = getFinishedWorker();
			if (w != null) {
				o = w.releaseOrange();
				System.out.println(Thread.currentThread().getName() + " " + w.getName() + " " + o.getState());
				if (o.getState() != Orange.State.Bottled) {
					processingQueue.add(o);
				} else {
					System.out.println(Thread.currentThread().getName() + " " + w.getName() + " Proccesed");
					orangesProcessed++;
				}
			}
		}
		System.out.println(Thread.currentThread().getName() + " Offline");
	}

// Gets a worker who doesn't have an orange
	private Worker getWorker() {
		for (Worker w : workers) {
			if (!w.hasOrange()) {
				return w;
			}
		}
		return null;
	}

	// Gets a worker who has finished their task
	private Worker getFinishedWorker() {
		for (Worker w : workers) {
			if (w.isFinished()) {
				return w;
			}
		}
		return null;
	}

	public int getProvidedOranges() {
		return orangesProvided;
	}

	public int getProcessedOranges() {
		return orangesProcessed;
	}

	public int getBottles() {
		return orangesProcessed / ORANGES_PER_BOTTLE;
	}

	public int getWaste() {
		return orangesProvided - orangesProcessed;
	}
}