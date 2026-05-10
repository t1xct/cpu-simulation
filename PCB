package OS;
public class PCB {
	    enum ProcessState { NEW, READY, RUNNING, TERMINATED }
	
	    ProcessState state = ProcessState.NEW;//default
	     int ProcessID;
	     int BurstTime;//cpu time that the process need to acomp
	     int RemainingTime;
	     int Priority;
	     int origPriority;
	     int Memory;
       
	    int startTime = -1;
	    int CompletionTime;
	    int arrivalTime = 0;
	    int waitingTime;//in the queue list
	    int turnaroundTime;//completion time - arrival time
        boolean Starved = false;
	    public PCB(int proID, int burTime, int Prio, int memo) {
	    	ProcessID = proID;
	        BurstTime = burTime;
	        RemainingTime = burTime;
			origPriority=Prio;
	        Priority = Prio;
	       Memory = memo;
	    }

		
	}
