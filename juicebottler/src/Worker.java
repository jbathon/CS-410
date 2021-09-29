
public class Worker implements Runnable{

	private final Thread thread;
	private Orange o;
	private volatile boolean timeToWork;
	private boolean finished;
	
	
	Worker(int threadNum) {
		o = null;
		thread = new Thread(this, "Worker[" + threadNum + "]");
	}
	
	public void startWorking() {
		timeToWork = true;
        thread.start();
	}
	
	public void stopWorking() {
		timeToWork = false;
	}
	
    public void waitToStop() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println(thread.getName() + " stop malfunction");
        }
    }
    
    public synchronized void aquireOrange(Orange o) {
    	if ( this.o == null) {
        	this.o = o;
        	finished = false;
    	}
    }
    
    public synchronized Orange releaseOrange() {
    	if ( o == null) throw new NullPointerException("Tired to release an Orange that did not exist!");
    	
    	try {
    		return o;
    	}
    	finally {
    		o = null;
    	}
    }
    
    public String getName() {
    	return thread.getName();
    }
    
    public boolean hasOrange() {
    	return (o != null);
    }
    
    public boolean isFinished() {
    	return finished;
    }

	public void run() {
		while(timeToWork) {
			if(hasOrange() && !finished) {
				o.runProcess();
				finished = true;
			}
		}
	}
}
