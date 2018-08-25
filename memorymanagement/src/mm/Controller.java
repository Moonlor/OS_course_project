package mm;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller {

    @FXML
    private TableView tview;
    @FXML
    private TableColumn memory_1;
    @FXML
    private TableColumn occupation_1;

    @FXML
    private JFXButton start_FF;
    @FXML
    private JFXButton start_BF;
    @FXML
    private JFXButton current_application;

    @FXML
    private TableView tview2;
    @FXML
    private TableColumn index;
    @FXML
    private TableColumn add;
    @FXML
    private TableColumn next_add;

    @FXML
    private TableView tview3;
    @FXML
    private TableColumn frame_number;
    @FXML
    private TableColumn page_number;
    @FXML
    private TableColumn add_range;

    @FXML
    private JFXButton start_FIFO;
    @FXML
    private JFXButton start_LRU;
    @FXML
    private JFXButton start_LFU;

    @FXML
    private JFXButton current_page_number;
    @FXML
    private JFXButton next_page_number;
    @FXML
    private JFXButton missing_page_number;
    @FXML
    private JFXButton missing_page_rate;

    private static final boolean FF = true;
    private static final boolean BF = false;

    private static final int FIFO = 1;
    private static final int LRU = 2;
    private static final int LFU = 3;

    @PostConstruct
    public void init() {
        init_FF_button();
        init_BF_button();
        init_FIFO_button();
        init_LFU_button();
        init_LRU_button();
    }

    public void init_FF_button() {
        start_FF.setOnAction(action -> {
            System.out.printf("!!!!!!!!");
            AllocationController a = new AllocationController();
            a.init(tview, occupation_1, memory_1, FF, current_application);
            Thread t = new Thread(a);
            t.start();
        });
    }

    public void init_BF_button() {
        start_BF.setOnAction(action -> {
            System.out.printf("!!!!!!!!");
            AllocationController a = new AllocationController();
            a.init(tview, occupation_1, memory_1, BF, current_application);
            Thread t = new Thread(a);
            t.start();
        });
    }

    public void init_FIFO_button() {
        start_FIFO.setOnAction(action -> {
            System.out.printf("!!!!!!!!");
            PagingController t = new PagingController();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("tview2", tview2);
            map.put("index",index);
            map.put("add", add);
            map.put("next_add", next_add);
            map.put("tview3", tview3);
            map.put("frame_number",frame_number);
            map.put("page_number",page_number);
            map.put("add_range",add_range);
            map.put("start_FIFO",start_FIFO);
            map.put("start_LRU",start_LRU);
            map.put("current_LFU",start_LFU);
            map.put("current_page_number",current_page_number);
            map.put("next_page_number",next_page_number);
            map.put("missing_page_number",missing_page_number);
            map.put("missing_page_rate",missing_page_rate);
            t.init(map, FIFO);
            Thread t2 = new Thread(t);
            t2.start();
        });
    }

    public void init_LRU_button() {
        start_LRU.setOnAction(action -> {
            System.out.printf("!!!!!!!!");
            PagingController t = new PagingController();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("tview2", tview2);
            map.put("index",index);
            map.put("add", add);
            map.put("next_add", next_add);
            map.put("tview3", tview3);
            map.put("frame_number",frame_number);
            map.put("page_number",page_number);
            map.put("add_range",add_range);
            map.put("start_FIFO",start_FIFO);
            map.put("start_LRU",start_LRU);
            map.put("current_LFU",start_LFU);
            map.put("current_page_number",current_page_number);
            map.put("next_page_number",next_page_number);
            map.put("missing_page_number",missing_page_number);
            map.put("missing_page_rate",missing_page_rate);
            t.init(map, LRU);
            Thread t2 = new Thread(t);
            t2.start();
        });
    }

    public void init_LFU_button() {
        start_LFU.setOnAction(action -> {
            System.out.printf("!!!!!!!!");
            PagingController t = new PagingController();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("tview2", tview2);
            map.put("index",index);
            map.put("add", add);
            map.put("next_add", next_add);
            map.put("tview3", tview3);
            map.put("frame_number",frame_number);
            map.put("page_number",page_number);
            map.put("add_range",add_range);
            map.put("start_FIFO",start_FIFO);
            map.put("start_LRU",start_LRU);
            map.put("current_LFU",start_LFU);
            map.put("current_page_number",current_page_number);
            map.put("next_page_number",next_page_number);
            map.put("missing_page_number",missing_page_number);
            map.put("missing_page_rate",missing_page_rate);
            t.init(map, LFU);
            Thread t2 = new Thread(t);
            t2.start();
        });
    }
}
