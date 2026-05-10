import java.util.*;

class ResultsTable {

	
    public static void display(ArrayList<PCB> completed,
                               ArrayList<GanttEntry> ganttChart,
                               String algorithmName) {

        System.out.println("\n============================================================================================");
        System.out.println("Execution Order - " + algorithmName);
        System.out.println("============================================================================================");
        System.out.println("\n Time        | Process");
        System.out.println("--------------------------------------------------------------------------------------------");

        
     // loop  to print execution order using Gantt chart entries
        for (GanttEntry g : ganttChart) {
            System.out.printf(" %-10s | P%d\n",
                    g.startTime + "-" + g.endTime,
                    g.processID);
        }

        System.out.println("============================================================================================");

        System.out.println("\n============================================================================================");
        System.out.println("Process Statistics - " + algorithmName);
        System.out.println("============================================================================================");
        System.out.println("\n PID | Burst | Priority | Memory | Start | Completion | Turnaround | Waiting | Starved");
        System.out.println("--------------------------------------------------------------------------------------------");

        double avgWait = 0;
        double avgTurnaround = 0;
        
        
        // loop to print the information of all completed processes
        for (PCB p : completed) {
            System.out.printf(" %-3d | %-5d | %-8d | %-6d | %-5d | %-10d | %-10d | %-7d | %s\n",
                    p.ProcessID,
                    p.BurstTime,
                    p.origPriority,
                    p.Memory,
                    p.startTime,
                    p.CompletionTime,
                    p.turnaroundTime,
                    p.waitingTime,
                    p.Starved ? "YES" : "NO");

            avgWait += p.waitingTime;
            avgTurnaround += p.turnaroundTime;
        }

        System.out.println("============================================================================================");

        // calculate and print the average waiting and turnaround times
        if (!completed.isEmpty()) {
            System.out.printf("\nAverage Waiting Time: %.2f ms\n", avgWait / completed.size());
            System.out.printf("Average Turnaround Time: %.2f ms\n", avgTurnaround / completed.size());
        }

        System.out.println("============================================================================================\n");
    }
}
