package mm;

public class Code {
    public int index;
    public String next_add;
    public String add;

    public Code(int index, String add, String next_add){
        this.index = index;
        this.add = add;
        this.next_add = next_add;
    }

    public String getAdd() {
        return add;
    }

    public int getIndex() {
        return index;
    }

    public String getNext_add() {
        return next_add;
    }
}
