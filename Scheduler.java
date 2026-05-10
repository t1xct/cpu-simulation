import java.util.*;

class Scheduler {

    private LinkedList<PCB> readyQueue;
    private ArrayList<PCB> completed;
    private SysCalls systemCalls;

    private int totalProcesses;
    private int time = 0;
    
 // stores execution order for gantt chart display
    private ArrayList<GanttEntry> ganttChart = new ArrayList<>();

    public Scheduler(LinkedList<PCB> readyQueue,
                     ArrayList<PCB> completed,
                     SysCalls systemCalls,
                     int totalProcesses) {

        this.readyQueue = readyQueue;
        this.completed = completed;
        this.systemCalls = systemCalls;
        this.totalProcesses = totalProcesses;
    }

    public ArrayList<GanttEntry> getGanttChart() {
        return ganttChart;
    }
   
    //shortest job first

    public void sjf() {

        time = 0;

        ArrayList<PCB> local = new ArrayList<>();

        System.out.println("\n[Time " + time +
                "ms] === SJF Scheduling Started ===\n");

        while (completed.size() < totalProcesses) {

            moveProcesses(local);

            if (local.isEmpty()) {
                sleepALittle();
                continue;
            }

            // shortest burst first
            // if equal burst -> arrival order
            local.sort((a, b) -> {

                if (a.BurstTime == b.BurstTime)
                    return a.arrivalTime - b.arrivalTime;

                return a.BurstTime - b.BurstTime;
            });

            PCB current = local.remove(0);

            executeProcess(current);
        }

        System.out.println("[Time " + time +
                "ms] === SJF Scheduling Completed ===");
    }

   
    // round robin
    // Q = 5 ms

    public void roundRobin() {

        time = 0;

        int quantum = 5;

        Queue<PCB> localRQ = new LinkedList<>();

        System.out.println("\n[Time " + time + "ms] === Round-Robin Scheduling Started (Quantum: " + quantum + "ms) ===\n");
        while (completed.size() < totalProcesses) {

            synchronized (readyQueue) {

                while (!readyQueue.isEmpty()) {
                    localRQ.add(readyQueue.remove());
                }
            }

            if (localRQ.isEmpty()) {
                sleepALittle();
                continue;
            }

            PCB current = localRQ.poll();

            // first execution
            if (current.startTime == -1) {

                current.startTime = time;

                systemCalls.setTime(time);
                systemCalls.createProcess(current.ProcessID);
            }

            current.state = PCB.ProcessState.RUNNING;

            int execTime =
                    Math.min(quantum, current.RemainingTime);

            System.out.println("[Time " + time +
                    "ms] Process " +
                    current.ProcessID +
                    " executing for " +
                    execTime + "ms");

            int start = time;

            time += execTime;

            int end = time;

            ganttChart.add(new GanttEntry(current.ProcessID, start, end));

            current.RemainingTime -= execTime;

            // process finished
            if (current.RemainingTime == 0) {

                finishProcess(current);

            } else {

                current.state = PCB.ProcessState.READY;

                localRQ.add(current);
            }
        }

        System.out.println("[Time " + time +
                "ms] === Round Robin Scheduling Completed ===");
    }

    
    // priorty scheduling
    // starvation + aging included
    

    public void priority() {

        time = 0;

        ArrayList<PCB> local = new ArrayList<>();

        System.out.println("\n[Time " + time +
                "ms] === Priority Scheduling Started ===\n");

        while (completed.size() < totalProcesses) {

            moveProcesses(local);

            if (local.isEmpty()) {
            sleepALittle();
                continue;
            }

            applyAgingAndDetectStarvation(local);

            // smaller priority value = higher priority
            // if equal priority -> arrival order
            local.sort((a, b) -> {

                if (a.Priority == b.Priority)
                    return a.arrivalTime - b.arrivalTime;

                return a.Priority - b.Priority;
            });

            PCB current = local.remove(0);

            executeProcess(current);
        }

        System.out.println("[Time " + time +
                "ms] === Priority Scheduling Completed ===");
    }

   
    // execute process

    private void executeProcess(PCB pcb) {

        if (pcb.startTime == -1) {
            pcb.startTime = time;
        }

        pcb.state = PCB.ProcessState.RUNNING;

        systemCalls.setTime(time);
        systemCalls.createProcess(pcb.ProcessID);

        System.out.println("[Time " + time + "ms] Process " + pcb.ProcessID + " executing for " + pcb.RemainingTime + "ms");

        int start = time;

        time += pcb.RemainingTime;

        int end = time;

        ganttChart.add(new GanttEntry(pcb.ProcessID, start, end));

        pcb.RemainingTime = 0;

        finishProcess(pcb);
    }

    
    // finish process

    private void finishProcess(PCB pcb) {

        pcb.CompletionTime = time;

        pcb.turnaroundTime =
                pcb.CompletionTime - pcb.arrivalTime;

        pcb.waitingTime =
                pcb.turnaroundTime - pcb.BurstTime;

        pcb.state = PCB.ProcessState.TERMINATED;


        systemCalls.setTime(time);

        System.out.println("\n---------- PROCESS " + pcb.ProcessID + " FINISHED ----------");

        systemCalls.terminateProcess(pcb.ProcessID);

        systemCalls.deallocateMemory(
                pcb.ProcessID,
                pcb.Memory
        );

        completed.add(pcb);


        System.out.println("-----------------------------------------------");
        System.out.println();
    }

   
    // starvation if waiting > (N * 5)
    // aging every 4 ms
   

    private void applyAgingAndDetectStarvation(
            ArrayList<PCB> processes) {

        int N = processes.size();

        for (PCB p : processes) {

            int waitTime = time - p.arrivalTime;

            // starvation detection
            if (waitTime > (N * 5)) {

                if (!p.Starved) {

                    p.Starved = true;

                    System.out.println(
                            "[Time " + time +
                                    "ms] STARVATION DETECTED: Process "
                                    + p.ProcessID
                    );
                }
            }

            // aging every 4 ms
            if (waitTime > 0 && waitTime % 4 == 0) {

                // smaller number = higher priority
                if (p.Priority > 1) {

                    int oldPriority = p.Priority;

                    p.Priority--;

                    System.out.println(
                            "[Time " + time +
                                    "ms] AGING: Process "
                                    + p.ProcessID +
                                    " priority changed from "
                                    + oldPriority +
                                    " to "
                                    + p.Priority
                    );
                }
            }
        }
    }

   
    // move process from ready queue
  

    private void moveProcesses(ArrayList<PCB> local) {

        synchronized (readyQueue) {

            while (!readyQueue.isEmpty()) {

                local.add(readyQueue.remove());
            }
        }
    }
    // wait
    private void sleepALittle() {

        try {

            Thread.sleep(50);

        } catch (InterruptedException ignored) {
        }
    }
}
