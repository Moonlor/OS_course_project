package mm;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

public class AllocationController implements Runnable {

    private TableView tview;
    private TableColumn memory_size;
    private TableColumn occupation_task;
    private JFXButton current_application;

//    public List<MemoryPartition> memory = new ArrayList<MemoryPartition>();
    public List<String> application = new ArrayList<String>();
    public List<String> application_word = new ArrayList<String>();
    public ObservableList<MemoryPartition> memory = FXCollections.observableArrayList();

    private int _momory_size;

    private static final int UNALLOCATED = -1;
    private static final int APPLICATION = 1;
    private static final int RELEASE     = 0;
    private static final int DEFAULT_RANGE_FOR_SLEEP = 100;

    private boolean run_ff = false;

    @PostConstruct
    public void init(TableView tv, TableColumn t, TableColumn s, boolean ff, JFXButton current_application){
        this._momory_size = 640;
        this.memory_size = s;
        this.occupation_task = t;
        this.tview = tv;
        this.run_ff = ff;
        this.current_application =current_application;

        memory.add(new MemoryPartition(UNALLOCATED, _momory_size));
        try {
            String path = this.getClass().getResource("/mm/application.txt").getPath();
            File file_name = new File(path);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file_name));
            BufferedReader br = new BufferedReader(reader);
            String l = br.readLine();
            while (l != null){
                application.add(l);
                l = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String path = this.getClass().getResource("/mm/application_word.txt").getPath();
            File file_name = new File(path);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file_name));
            BufferedReader br = new BufferedReader(reader);
            String l = br.readLine();
            while (l != null){
                application_word.add(l);
                l = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void run() {
        if (run_ff){
            runFF();
        } else {
            runBF();
        }
    }

    public void changeButtonText(int i){
        Platform.runLater(new Runnable() { @Override public void run() {
            current_application.setText(application_word.get(i));
        } });
    }

    private void sleep(int sec){
        try {
            Thread.sleep( sec * DEFAULT_RANGE_FOR_SLEEP);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void merge(){
        int end_flag = memory.size() - 1;
        for (int i = 0; i < end_flag; i++) {
            if(memory.get(i).task_id == UNALLOCATED && memory.get(i + 1).task_id == UNALLOCATED){
                memory.get(i + 1).size += memory.get(i).size;
                memory.remove(i);
                end_flag -= 1;
            }
        }
    }

    private Map<String, Integer> parseApplication(String s){
        Map<String, Integer> map = new HashMap<String, Integer>();
        String r[] = s.split("\\|");
        map.put("task_id", Integer.parseInt(r[0]));
        map.put("action", Integer.parseInt(r[1]) == APPLICATION ? APPLICATION: RELEASE);
        map.put("size", Integer.parseInt(r[2]));
        return map;
    }

    public void runFF(){

        memory_size.setCellValueFactory(new PropertyValueFactory("size"));
        occupation_task.setCellValueFactory(new PropertyValueFactory("task_id"));
        tview.setItems(memory);

        int index = 0;

        for (String s : application){
            changeButtonText(index);
            index += 1;
            Map<String, Integer> m = parseApplication(s);
            if((int)m.get("action") == APPLICATION){
                for (int i = 0; i < memory.size(); i++) {
                    if(memory.get(i).task_id == UNALLOCATED && memory.get(i).size >= (int)m.get("size")){
                        memory.add(i, new MemoryPartition((int)m.get("task_id"), (int)m.get("size")));
                        memory.get(i + 1).size -= (int)m.get("size");
                        break;
                    }
                }
            } else {
                for (int i = 0; i < memory.size(); i++) {
                    if(memory.get(i).task_id == (int)m.get("task_id")){
                        MemoryPartition temp = new MemoryPartition(UNALLOCATED, memory.get(i).size);
                        memory.remove(i);
                        memory.add(i, temp);
                        merge();
                        break;
                    }
                }
            }
            System.out.println("==================================");
            for (MemoryPartition m2 : memory){
                System.out.printf("%d\t : \t%d \n", m2.task_id, m2.size);
            }
            sleep(10);
        }
    }

    private void runBF(){
        memory_size.setCellValueFactory(new PropertyValueFactory("size"));
        occupation_task.setCellValueFactory(new PropertyValueFactory("task_id"));
        tview.setItems(memory);

        int index = 0;

        for (String s : application){

            List<Integer> empty_block = new ArrayList<Integer>();
            for (int i = 0; i < memory.size(); i++) {
                if (memory.get(i).task_id == UNALLOCATED){
                    empty_block.add(memory.get(i).size);
                }
            }

            Collections.sort(empty_block);
            int k = 0;
            for (int i = 0; i < memory.size(); i++) {
                if (memory.get(i).task_id == UNALLOCATED){
                    MemoryPartition temp = new MemoryPartition(UNALLOCATED, empty_block.get(k));
                    memory.remove(i);
                    memory.add(i, temp);
                    k += 1;
                }
            }

            changeButtonText(index);
            index += 1;
            Map<String, Integer> m = parseApplication(s);
            if((int)m.get("action") == APPLICATION){
                for (int i = 0; i < memory.size(); i++) {
                    if(memory.get(i).task_id == UNALLOCATED && memory.get(i).size >= (int)m.get("size")){
                        memory.add(i, new MemoryPartition((int)m.get("task_id"), (int)m.get("size")));
                        memory.get(i + 1).size -= (int)m.get("size");
                        break;
                    }
                }
            } else {
                for (int i = 0; i < memory.size(); i++) {
                    if(memory.get(i).task_id == (int)m.get("task_id")){
                        MemoryPartition temp = new MemoryPartition(UNALLOCATED, memory.get(i).size);
                        memory.remove(i);
                        memory.add(i, temp);
                        merge();
                        break;
                    }
                }
            }
            System.out.println("==================================");
            for (MemoryPartition m2 : memory){
                System.out.printf("%d\t : \t%d \n", m2.task_id, m2.size);
            }
            sleep(10);
        }
    }
}
