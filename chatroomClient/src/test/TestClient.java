package test;

import java.util.concurrent.CountDownLatch;

public class TestClient {

	public static void main(String[] args) throws InterruptedException {
		int count = 1000;
		CountDownLatch countDownLatch = new CountDownLatch(count);
		
		for(int i=1;i<=count;i++){
			new Thread(new TestThread(countDownLatch,i)).start();
			countDownLatch.countDown();
		}
		
	}

}
