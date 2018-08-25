package mm;

public class FrameItem {
    public int frame_number;
    public int page_number;
    public String add_range;

    public FrameItem(int frame_number, int page_number, String add_range){
        this.frame_number = frame_number;
        this.page_number = page_number;
        this.add_range = add_range;
    }

    public FrameItem(FrameItem item){
        this.frame_number = item.frame_number;
        this.page_number = item.page_number;
        this.add_range = item.add_range;
    }

    public int getFrame_number() {
        return frame_number;
    }

    public int getPage_number() {
        return page_number;
    }

    public String getAdd_range() {
        return add_range;
    }
}
