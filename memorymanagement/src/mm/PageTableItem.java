package mm;

public class PageTableItem {
    public int page_number;
    public int visited;

    public PageTableItem(int page_number){
        this.page_number = page_number;
        this.visited = 0;
    }
}
