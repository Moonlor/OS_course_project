package mm;

import com.jfoenix.controls.JFXButton;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.InputStream;
import java.math.RoundingMode;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;

import javafx.fxml.JavaFXBuilderFactory;

public class PagingController implements Runnable {

    private List<Integer> _code_list = new ArrayList<Integer>();
    private List<Integer> _visited_record = new ArrayList<Integer>();
    private ObservableList<PageTableItem> _page_table = FXCollections.observableArrayList();
    private ObservableList<Code> _current_code = FXCollections.observableArrayList();
    private ObservableList<FrameItem> _memory = FXCollections.observableArrayList();

    private TableView tview2;
    private TableColumn index;
    private TableColumn add;
    private TableColumn next_add;

    private TableView tview3;
    private TableColumn frame_number;
    private TableColumn page_number;
    private TableColumn add_range;

    private JFXButton start_FIFO;
    private JFXButton start_LRU;
    private JFXButton start_LFU;

    private JFXButton current_page_number;
    private JFXButton next_page_number;
    private JFXButton missing_page_number;
    private JFXButton missing_page_rate;

    private static final int DEFAULT_RANGE_FOR_SLEEP = 30;
    private static final int FIFO = 1;
    private static final int LRU = 2;
    private static final int LFU = 3;

    private int runtime_flag = FIFO;
    private int missing_page = 0;


    public void init(Map<String, Object> map, int flag){
        generateCode();
        this.tview2 = (TableView)map.get("tview2");
        this.add = (TableColumn)map.get("add");
        this.next_add = (TableColumn)map.get("next_add");
        this.index = (TableColumn)map.get("index");

        this.tview3 = (TableView)map.get("tview3");
        this.frame_number = (TableColumn)map.get("frame_number");
        this.page_number = (TableColumn)map.get("page_number");
        this.add_range = (TableColumn)map.get("add_range");

        this.start_FIFO = (JFXButton)map.get("start_FIFO");
        this.start_LRU = (JFXButton)map.get("start_LRU");
        this.start_LFU = (JFXButton)map.get("start_LFU");

        this.current_page_number = (JFXButton)map.get("current_page_number");
        this.next_page_number = (JFXButton)map.get("next_page_number");
        this.missing_page_number = (JFXButton)map.get("missing_page_number");
        this.missing_page_rate = (JFXButton)map.get("missing_page_rate");

        this.runtime_flag = flag;
    }

    private void sleep(int sec){
        try {
            Thread.sleep( sec * DEFAULT_RANGE_FOR_SLEEP);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public String intToBinary(int num){
        return Integer.toBinaryString(num);
    }

    boolean inMemory(int page_number){
        for (PageTableItem item : _page_table) {
            if (page_number == item.page_number){
                return true;
            }
        }
        return false;
    }

    void update_status(int count){
        int index = _current_code.get(0).getIndex();
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.UP);

        Platform.runLater(new Runnable() { @Override public void run() {
            current_page_number.setText("当前指令页号:" + Integer.toString(index / 10));
            next_page_number.setText("下一条指令页号:" + Integer.toString(_code_list.get(index) / 10));

            missing_page_number.setText("缺页数:" + Integer.toString(missing_page));
            missing_page_rate.setText("缺页率:" + nf.format((float)missing_page / (float)count));
        } });
    }

    int getPageNumberIndex(int page_number){
        for (int i = 0; i < _page_table.size(); i++){
            if (page_number == _page_table.get(i).page_number){
                return i;
            }
        }
        return -1;
    }

    void sortLRU(int recently_usedd_page_number){
        int index = getPageNumberIndex(recently_usedd_page_number);
        PageTableItem temp = new PageTableItem(recently_usedd_page_number);
        _page_table.remove(index);
        _page_table.add(temp);
    }

    void sortLFU(int recently_usedd_page_number){
        int index = getPageNumberIndex(recently_usedd_page_number);
        _page_table.get(index).visited += 1;
        Comparator c = new Comparator<PageTableItem>() {
            @Override
            public int compare(PageTableItem o1, PageTableItem o2) {
                if (o1.visited < o2.visited) return -1;
                else if (o1.visited > o2.visited) return 1;
                else return 0;
            }
        };
        Collections.sort(_page_table, c);
    }

    void updateVisitedRecord(int index){
        _visited_record.set(index, _visited_record.get(index) + 1);

        if (_visited_record.get(index) >= 3){
            Random random = new Random();
            _code_list.set(index, random.nextInt(280) + 20);
        }

    }

    public void changeFrame(int old_page_number, int new_page_number){
        for (int i = 0; i < _memory.size(); i++){
            if (_memory.get(i).page_number == old_page_number){
                _memory.remove(i);
                String range = intToBinary(new_page_number * 10) + " ~ " + intToBinary(new_page_number * 10 + 10);
                _memory.add(i, new FrameItem(i, new_page_number, range));
            }
        }
        for (int i = 0; i < _memory.size(); i++){
            FrameItem temp = new FrameItem(_memory.get(i));
            temp.frame_number = i;
            _memory.remove(i);
            _memory.add(i, temp);
        }
        missing_page += 1;
    }

    public void changePage(int old_page_number, int new_page_number){
        for (int i = 0; i < _page_table.size(); i++){
            if (_page_table.get(i).page_number == old_page_number){
                _page_table.remove(i);
                _page_table.add(new PageTableItem(new_page_number));
            }
        }
    }

    public void addFrame(int new_page_number){
        String range = intToBinary(new_page_number * 10) + " ~ " + intToBinary(new_page_number * 10 + 10);
        _memory.add(_memory.size(), new FrameItem(_memory.size(), new_page_number, range));
        missing_page += 1;
    }

    public void generateCode(){
        for (int i = 0; i < 320; i++){
            _code_list.add(i + 1);
            _visited_record.add(0);
        }

        _code_list.set(_code_list.size() - 1, 1);

        Random random = new Random();
        for (int i = 0; i < 320; i++){
            if (i > 300) break;
            if (i < 20) continue;
            int temp = random.nextInt(100) + 1;
            if (temp <= 25){
                _code_list.set(i, random.nextInt(i));
            } else if (temp > 75){
                _code_list.set(i, random.nextInt(320 - i - 1) + i + 1);
            }
        }
        for (Integer each : _code_list){
            System.out.println(each);
        }
    }

    public void run(){
        switch (runtime_flag){
            case FIFO:
                runFIFO();
                break;
            case LRU:
                runLRU();
                break;
            case LFU:
                runLFU();
        }
    }

    private void runFIFO(){

        add.setCellValueFactory(new PropertyValueFactory("add"));
        next_add.setCellValueFactory(new PropertyValueFactory("next_add"));
        index.setCellValueFactory(new PropertyValueFactory("index"));
        tview2.setItems(_current_code);

        frame_number.setCellValueFactory(new PropertyValueFactory("frame_number"));
        page_number.setCellValueFactory(new PropertyValueFactory("page_number"));
        add_range.setCellValueFactory(new PropertyValueFactory("add_range"));
        tview3.setItems(_memory);

        Random random = new Random();
        int current_code_index = random.nextInt(319) + 1;
        int count = 0;
        while (count < 320){
            _current_code.clear();
            Code temp = new Code(current_code_index, intToBinary(current_code_index), intToBinary(_code_list.get(current_code_index)));
            _current_code.add(temp);

            if (!inMemory(current_code_index / 10)){
                if (_page_table.size() > 3){
                    changeFrame(_page_table.get(0).page_number, current_code_index / 10);
                    changePage(_page_table.get(0).page_number, current_code_index / 10);
                } else {
                    _page_table.add(new PageTableItem(current_code_index / 10));
                    addFrame(current_code_index / 10);
                }

            }

            current_code_index = _code_list.get(current_code_index);
            updateVisitedRecord(current_code_index);

            count += 1;
            update_status(count);
            sleep(3);

        }

    }

    private void runLRU(){

        add.setCellValueFactory(new PropertyValueFactory("add"));
        next_add.setCellValueFactory(new PropertyValueFactory("next_add"));
        index.setCellValueFactory(new PropertyValueFactory("index"));
        tview2.setItems(_current_code);

        frame_number.setCellValueFactory(new PropertyValueFactory("frame_number"));
        page_number.setCellValueFactory(new PropertyValueFactory("page_number"));
        add_range.setCellValueFactory(new PropertyValueFactory("add_range"));
        tview3.setItems(_memory);

        Random random = new Random();
        int current_code_index = random.nextInt(319) + 1;
        int count = 0;
        while (count < 320){
            _current_code.clear();
            Code temp = new Code(current_code_index, intToBinary(current_code_index), intToBinary(_code_list.get(current_code_index)));
            _current_code.add(temp);

            if (!inMemory(current_code_index / 10)){
                if (_page_table.size() > 3){
                    changeFrame(_page_table.get(0).page_number, current_code_index / 10);
                    changePage(_page_table.get(0).page_number, current_code_index / 10);
                } else {
                    _page_table.add(new PageTableItem(current_code_index / 10));
                    addFrame(current_code_index / 10);
                }
            }

            sortLRU(current_code_index / 10);

            current_code_index = _code_list.get(current_code_index);
            updateVisitedRecord(current_code_index);

            count += 1;
            update_status(count);
            sleep(3);

        }

    }

    private void runLFU(){

        add.setCellValueFactory(new PropertyValueFactory("add"));
        next_add.setCellValueFactory(new PropertyValueFactory("next_add"));
        index.setCellValueFactory(new PropertyValueFactory("index"));
        tview2.setItems(_current_code);

        frame_number.setCellValueFactory(new PropertyValueFactory("frame_number"));
        page_number.setCellValueFactory(new PropertyValueFactory("page_number"));
        add_range.setCellValueFactory(new PropertyValueFactory("add_range"));
        tview3.setItems(_memory);

        Random random = new Random();
        int current_code_index = random.nextInt(319) + 1;
        int count = 0;
        while (count < 320){
            _current_code.clear();
            Code temp = new Code(current_code_index, intToBinary(current_code_index), intToBinary(_code_list.get(current_code_index)));
            _current_code.add(temp);

            if (!inMemory(current_code_index / 10)){
                if (_page_table.size() > 3){
                    changeFrame(_page_table.get(0).page_number, current_code_index / 10);
                    changePage(_page_table.get(0).page_number, current_code_index / 10);
                } else {
                    _page_table.add(new PageTableItem(current_code_index / 10));
                    addFrame(current_code_index / 10);
                }
            }

            sortLFU(current_code_index / 10);

            current_code_index = _code_list.get(current_code_index);
            updateVisitedRecord(current_code_index);

            count += 1;
            update_status(count);
            sleep(3);

        }

    }
}
