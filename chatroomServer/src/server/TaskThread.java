package server;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TaskThread extends Thread {
	private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(); 
	
	public void post(Runnable task){
		queue.add(task);
	}
	
	@Override
	public synchronized void start() {
		super.start();
	}
	
	@Override
	public void run() {
		while(!isInterrupted()){
			try {
				queue.take().run();
			} catch (InterruptedException e) {
				break;
			}
		}
	}

}
