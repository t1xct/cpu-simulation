import java.io.*;
import java.util.*;

public class jobReaderThread extends Thread {

 private LinkedList<PCB> jobQueue;

 public jobReaderThread(LinkedList<PCB> jobQu) {
     jobQueue = jobQu;
 }

 
 public void run() {
     try {
    	 System.out.println("\nJob Reader Thread has Started To Read !!\n");
         BufferedReader br = new BufferedReader( new FileReader("job.txt"));
         String line;

         while ((line = br.readLine()) != null) {//read until file reach null

             String[] parts = line.split("[:;]");

             int id = Integer.parseInt(parts[0]);
             int burstTime = Integer.parseInt(parts[1]);
             int priority = Integer.parseInt(parts[2]);
             int memory = Integer.parseInt(parts[3]);

             PCB process = new PCB(id, burstTime, priority, memory);//new PCB
             
             synchronized (jobQueue) {//for thread safe
            	    jobQueue.add(process);
            	    jobQueue.notify();
            	}
         }

         br.close();
         System.out.println("\nreading is completed\n");

     } catch (IOException e) {
          System.err.println("Error reading" + e.getMessage());
     }
 }
}
