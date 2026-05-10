package os;

	import java.text.SimpleDateFormat;
	import java.util.*;

	class SysCalls {
		private int currentTime = 0;
	    private Date systemStartDate;
	    private Memory memo;
	    

	    public SysCalls(Memory m) {
	        memo = m;
	        systemStartDate = new Date();
	    }

	    public void setTime(int time) {
	        currentTime = time;
	    }


	    public void createProcess(int processId) {
	        System.out.println("Process " + processId + " created at: " + currentTime + "ms");
	    }

	    public void terminateProcess(int processId) {
	        System.out.println("Process " + processId + " terminated at: " + currentTime + "ms");
	    }

	    
	    public boolean allocateMemory(int processId, int size) {
	        int beforeAllocation = memo.getAvailable();
	        boolean allocation = memo.allocateMemo(size);
	        if (allocation) {
	            int afterAllocation = memo.getAvailable();
	            System.out.println(
	                    "Allocated " + size + "MB to Process " + processId +
	                    " at: " + currentTime + "ms\n" +
	                    "(Before: " + beforeAllocation + "MB, After: " +
	                    afterAllocation + "MB)"
	            );
	            
	        } else {
	        	 System.out.println(
	                     "Failed to allocate " + size + "MB to Process " +
	                     processId + " at: " + currentTime + "ms\n" +
	                     "Only"+ beforeAllocation +"  is currently available"
	             );

	        }
	        return allocation;
	    }

	    public void deallocateMemory(int processId, int size) {
	        int beforeDeallocation = memo.getAvailable();
	        memo.deallocateMemo (size);
	        int afterDeallocation = memo.getAvailable();
	        System.out.println(
	                "Deallocated " + size + "MB from Process " + processId +
	                " at: " + currentTime + "ms\n" +
	                "(Before: " + beforeDeallocation + "MB, After: " +
	                afterDeallocation + "MB)"
	        );
	        synchronized(JobLoaderThread.jobQueue) {
	        JobLoaderThread.jobQueue.notifyAll();}
	    }

	  
	    public int getSystemTime() {
	        return currentTime;
	    }

	    public String getSystemDate() {
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        return dateFormat.format(systemStartDate);
	    }

	    public int getAvailableMemory() {
	        return memo.getAvailable();
	    }

	}
