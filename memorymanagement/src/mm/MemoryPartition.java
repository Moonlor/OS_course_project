package mm;

public class MemoryPartition {
    public int size;
    public int task_id;

    MemoryPartition(int t, int s){
        this.size = s;
        this.task_id = t;
    }

    public int getSize() {
        return size;
    }

    public int getTask_id() {
        return task_id;
    }
}
