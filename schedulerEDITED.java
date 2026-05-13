package os;

import java.util.*;

class Scheduler {

    private LinkedList<PCB> readyQueue;
    private ArrayList<PCB> completed;
    private SysCalls systemCalls;

    private int totalProcesses;
    public static int time = 0;
    
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

            executeProcess(current,local);
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

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            moveProcesses(local);

            applyAgingAndDetectStarvation(local);

            local.sort((a, b) -> {
                if (a.Priority == b.Priority)
                    return a.readyQueueEntryTime - b.readyQueueEntryTime;
                return a.Priority - b.Priority;
            });

            PCB current = local.remove(0);
            executeProcess(current, local);
        }

        System.out.println("[Time " + time +
                "ms] === Priority Scheduling Completed ===");
    }
    private void executeProcess(PCB current, ArrayList<PCB> local) {
        if (current.startTime == -1) current.startTime = time;
        current.state = PCB.ProcessState.RUNNING;

        systemCalls.setTime(time);
        systemCalls.createProcess(current.ProcessID);

        System.out.println("[Time " + time + "ms] Process " +
                current.ProcessID + " executing for " +
                current.RemainingTime + "ms");

        int start = time;
        int burst = current.RemainingTime;

        for (int ms = 0; ms < burst; ms++) {
            moveProcesses(local);
            applyAgingAndDetectStarvation(local);
            time++;
        }

        current.RemainingTime = 0;
        ganttChart.add(new GanttEntry(current.ProcessID, start, time));
        finishProcess(current);
    }

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
   

    private void applyAgingAndDetectStarvation(ArrayList<PCB> processes) {
        int N = processes.size();

        for (PCB p : processes) {
            int waitTime = time - p.readyQueueEntryTime;

            if (waitTime > (N * 5)) {
                if (!p.Starved) {
                    p.Starved = true;
                    p.lastAgedAt = time;
                    System.out.println("[Time " + time +
                            "ms] STARVATION DETECTED: Process " + p.ProcessID);
                }
            }

            if (p.Starved && p.lastAgedAt != -1 && (time - p.lastAgedAt) >= 4) {
                if (p.Priority > 1) {
                    int old = p.Priority;
                    p.Priority--;
                    p.lastAgedAt = time;
                    System.out.println("[Time " + time +
                            "ms] AGING: Process " + p.ProcessID +
                            " priority changed from " + old +
                            " to " + p.Priority);
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
