package os;

public class Memory {
	public final int TOTAL = 2048;
    private int available;

    public Memory() {
        available = TOTAL;
    }


    public synchronized void deallocateMemo (int size) {
            available = available + size;
    }

  public synchronized boolean allocateMemo ( int size) {
        if (available >= size) {
            available = available - size;
            return true;
        }
        return false;
    }

    public synchronized int getAvailable() {
        return available;
    }
}


