package os;
import java.util.*;
public class JobLoaderThread extends Thread{


	private volatile boolean running = true;
    public LinkedList<PCB> readyQueue;
    public static LinkedList<PCB> jobQueue;
    private SysCalls systemCall;

   public JobLoaderThread(LinkedList<PCB> jq, LinkedList<PCB> rq, SysCalls sc) {
       readyQueue = rq;
       jobQueue = jq;
		systemCall = sc;
   }


   public void run() {
       System.out.println("Job loader thread started at: 0 ms");

       while (running) {
//we synchronize it so only one thread can enter
           synchronized (jobQueue) {
//if it is empty we wait otherwise we load the next job
               if (jobQueue.isEmpty()) {
                   try {
                    	jobQueue.wait(100);
       			} 
			catch (InterruptedException e) {
           			running = false;
       			}
               } else {
                      PCB job = jobQueue.peek(); //look at the first item
//if we have enough memory we add it to ready queue otherwise we wait for memory space
       		    if (enoughMemory(job)) {
           				addJobToReadyQueue(job);
       		    } else {
           				waitForMemorySpace();
       }
               }
           }
       }

       System.out.println("Job Loader Thread terminated at:" + systemCall.getSystemTime() + "ms");
}

   private boolean enoughMemory(PCB job) {
       return systemCall.allocateMemory(job.ProcessID, job.Memory);
   }

   private void addJobToReadyQueue(PCB job) {
       jobQueue.remove();
       job.state = PCB.ProcessState.READY;
       synchronized (readyQueue) {
           readyQueue.add(job);
           readyQueue.notify();
       }
   }

   private void waitForMemorySpace() {
       try {
    	   synchronized(jobQueue) {
           jobQueue.wait();}
       } catch (InterruptedException e) {
           running = false;
       }
   }

   public void terminate() {
       running = false;
   }

	
}
