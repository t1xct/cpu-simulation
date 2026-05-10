mport java.util.*;

public class MainThread {
	 static LinkedList<PCB> jobQueue = new LinkedList<>();
	    static LinkedList<PCB> readyQueue = new LinkedList<>();
	    static ArrayList<PCB> completed = new ArrayList<>();
	    static SysCalls systemCalls;
	    static Memory memory;
	    static int totalProcesses = 0;

	    public static void main(String[] args) throws Exception {
	        memory = new Memory();
	        systemCalls = new SysCalls(memory);

	        
	        System.out.println("\n" + "**************************************************");
	        System.out.println("CPU SCHEDULER SIMULATOR");
	        System.out.println("**************************************************");
	        System.out.println("System Date: " + systemCalls.getSystemDate());
	        System.out.println("System Start Time: " + systemCalls.getSystemTime() + "ms");
	        System.out.println("Available Memory: " + systemCalls.getAvailableMemory() + "MB");
	        System.out.println("**************************************************");

	        jobReaderThread fileReader = new jobReaderThread(jobQueue);
	        JobLoaderThread jobLoader = new JobLoaderThread(jobQueue, readyQueue, systemCalls);

	        fileReader.start();
	        jobLoader.start();
	        
	        
	        fileReader.join();
	        Thread.sleep(200);

	        synchronized (jobQueue) {
	            totalProcesses = jobQueue.size();
	        }
	        synchronized (readyQueue) {
	            totalProcesses += readyQueue.size();
	        }

	        Scanner input = new Scanner(System.in);
	        //Menu
	        System.out.println("\n -----Choose the scheduling algorithm to be applied:-----");
	        System.out.println("1- Shortest Job First (SJF)");
	        System.out.println("2- Round-Robin (Quantum: 5ms)");
	        System.out.println("3- Priority Scheduling");
	        System.out.print("Select an option:(1-3) ");
	        
	        
	

	        int choice = input.nextInt();
	        input.close();
	        

	        Scheduler scheduler = new Scheduler(readyQueue, completed, systemCalls, totalProcesses);

	        String algorithmName = "";
	        if (choice == 1) {
	            algorithmName = "Shortest Job First (SJF)";
	            scheduler.sjf();
	        } else if (choice == 2) {
	            algorithmName = "Round-Robin (Quantum: 5ms)";
	            scheduler.roundRobin();
	        } else if (choice == 3) {
	            algorithmName = "Priority Scheduling";
	            scheduler.priority();
	        }
	        	else {
	        	    System.out.println("Invalid choice.");
	        	    jobLoader.terminate();
	        	    return;
	        	}

	        	jobLoader.terminate();

	        

	          // show remaining available memory after scheduling completes
	        System.out.println("\nThe available Memory After Execution: " + systemCalls.getAvailableMemory() + " MB");

	        
	     // display execution results and performance statistics
	        ResultsTable.display(completed,scheduler.getGanttChart(), algorithmName);
	        System.out.println("\nJob Loader Thread terminated at: "
	                + systemCalls.getSystemTime() + "ms");
	    }
	}
