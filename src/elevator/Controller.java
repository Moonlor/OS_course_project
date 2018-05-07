package elevator;

import javafx.scene.control.*;
import javafx.beans.value.*;
import com.jfoenix.controls.*;
import javafx.scene.control.ToggleGroup;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import org.omg.CORBA.Object;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.xml.stream.FactoryConfigurationError;


public class Controller {

    @FXML
    private StackPane root;
    @FXML
    private ToggleGroup toggleGroup;

    private JFXComboBox combo;

    private static final int REST = 0;
    private static final int UP = 1;
    private static final int DOWN = 2;

    private ObservableList<String> apiList=FXCollections.observableArrayList();

    private List<JFXButton> elevator_buttoon_list = new ArrayList<JFXButton>();
    private List<Slider>    slider_list = new ArrayList<Slider>();
    private List<BlockingQueue> inner_button_list = new ArrayList<BlockingQueue>();
    private List<Elevator> elevator_list = new ArrayList<Elevator>();
    private List<JFXButton> floor_up_button_list = new ArrayList<JFXButton>();
    private List<JFXButton> floor_down_button_list = new ArrayList<JFXButton>();
    private List<JFXButton> floor_dis_button_list = new ArrayList<JFXButton>();

    private int current_elevator;
    private int current_floor;

    private List<Integer> elevator_status = new ArrayList<Integer>();
    private List<Integer> elevator_floor = new ArrayList<Integer>();

    @PostConstruct
    public void init() {
        this.current_elevator = 1;
        ExecutorService service = Executors.newCachedThreadPool();

        for (int i = 0; i < 5; i++){
            //每台电梯都初始为静止
            elevator_status.add(REST);
            //每台电梯都初始位于1楼
            elevator_floor.add(1);
            //五个队列用于各个电梯进程间的通信
            LinkedBlockingQueue temp = new LinkedBlockingQueue(30);
            inner_button_list.add(temp);
            //五个电梯进程
            Elevator temp2 = new Elevator(i + 1, temp, elevator_buttoon_list,
                    slider_list, floor_dis_button_list, elevator_status, elevator_floor, floor_up_button_list, floor_down_button_list);
            elevator_list.add(temp2);
            service.execute(temp2);
        }

        init_elevator_buttons();
        init_slider();
        init_radio_button();
        init_combo_box();
        init_floor_buttons();
    }

    public int find_best_elevator(int status, int t_floor){
        int target = 404;
        int differ = 999;
        //从上升状态的电梯寻找
        if (status == UP){
            for (int i = 0; i < 5; i++){
                if (elevator_status.get(i) == UP){
                    int temp = current_floor - elevator_floor.get(i);
                    if (temp > 0 && temp < differ){
                        target = i;
                        differ = temp;
                    }
                }
            }
        }//从下降状态的电梯中寻找
        else if (status == DOWN){
            for (int i = 0; i < 5; i++){
                if (elevator_status.get(i) == DOWN){
                    int temp = elevator_floor.get(i) - current_floor;
                    if (temp > 0 && temp < differ){
                        target = i;
                        differ = temp;
                    }
                }
            }
        }
        //没找到时，分别从上升、下降或悬停状态的电梯中寻找，得到距离目标楼层最近的电梯
        if (target == 404){
            for (int i = 0; i < 5; i++){
                if (elevator_status.get(i) == REST){
                    int temp = Math.abs(elevator_floor.get(i) - current_floor);
                    if (temp < differ){
                        target = i;
                        differ = temp;
                    }
                }
            }

            if (status == DOWN){
                for (int i = 0; i < 5; i++){
                    if (elevator_status.get(i) == UP){
                        int temp = (current_floor - elevator_floor.get(i));
                        if (temp > 0 && temp < differ && temp < differ){
                            boolean break_flag  = false;
                            for (java.lang.Object each : elevator_list.get(i).task_set) {
                                if((int)each > current_floor){
                                    break_flag = true;
                                    break;
                                }
                            }
                            if(break_flag){
                                break;
                            }

                            target = i;
                            differ = temp;
                        }
                    }
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    if (elevator_status.get(i) == UP) {
                        int temp = (current_floor - elevator_floor.get(i));
                        if (temp > 0 && temp < differ && temp < differ) {
                            boolean break_flag  = false;
                            for (java.lang.Object each : elevator_list.get(i).task_set) {
                                if((int)each < current_floor){
                                    break_flag = true;
                                    break;
                                }
                            }
                            if(break_flag){
                                break;
                            }

                            target = i;
                            differ = temp;
                        }
                    }
                }
            }

        }



        //都没找到时，那过一会儿再查找
        if (target == 404){
            try {
                Thread.sleep(500);
                return find_best_elevator(status, t_floor);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return target;

    }

    public void distribute_floor_task(int btn_type){
        int e_index = find_best_elevator(btn_type, current_floor);
        inner_button_list.get(e_index).add(String.valueOf(current_floor));
        inner_button_list.get(e_index).add("floor|" + String.valueOf(btn_type) + "|" + String.valueOf(current_floor) );
    }

    public int elevator_name_to_index(String name) {
        return Integer.parseInt(name.split("\\s+")[1]);
    }

    public void init_floor_buttons() {

        for (int i = 1; i < 6; i++){
            floor_up_button_list.add((JFXButton) root.lookup("#up" + String.valueOf(i)));
            floor_down_button_list.add((JFXButton) root.lookup("#down" + String.valueOf(i)));
            floor_dis_button_list.add((JFXButton) root.lookup("#dis" + String.valueOf(i)));
        }

        for (int i = 0; i < 5; i++) {
            JFXButton up_btn = floor_up_button_list.get(i);
            JFXButton down_btn = floor_down_button_list.get(i);
            up_btn.setOnAction(action -> {
                for (JFXButton e : floor_up_button_list) {
                    e.setStyle("-fx-background-color: #0f9d58;");
                    distribute_floor_task(UP);
                }
            });
            down_btn.setOnAction(action -> {
                for (JFXButton e : floor_down_button_list) {
                    e.setStyle("-fx-background-color: #0f9d58;");
                    distribute_floor_task(DOWN);
                }
            });
        }

    }

    public void init_combo_box() {
        combo = (JFXComboBox) root.lookup("#combo");
        combo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> selected, String oval, String nval)
            {
                if (nval != null) {
                    current_floor = Integer.valueOf(nval);
                    System.out.println("当前楼层为：" + nval);
                    for (JFXButton e : floor_up_button_list) {
                        e.setStyle("-fx-background-color: #ed9139");
                    }
                    for (JFXButton e : floor_down_button_list) {
                        e.setStyle("-fx-background-color: #ed9139");
                    }
                }
            }
        });
    }

    public void init_radio_button() {
        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            @Override
            public void changed(ObservableValue<? extends Toggle> changed, Toggle oldVal, Toggle newVal)
            {
                RadioButton temp_rb=(RadioButton)newVal;
                System.out.println("当前运行的电梯是：" + temp_rb.getText());
                current_elevator = elevator_name_to_index(temp_rb.getText());
                re_init_elevator_buttons();
            }
        });
    }

    public void init_slider() {

        for (int i = 1; i < 6; i++){
            slider_list.add((Slider) root.lookup("#slider" + String.valueOf(i)));
        }
        for (Slider each : slider_list){
            each.setValue(1);
        }
    }

    public void init_elevator_buttons() {

        for (int i = 1; i < 21; i++){
            elevator_buttoon_list.add((JFXButton) root.lookup("#e" + String.valueOf(i)));
        }

        for (JFXButton each : elevator_buttoon_list) {
            each.setOnAction(action -> {
                each.setStyle("-fx-background-color: #5bed62;");
                try {
                    inner_button_list.get(current_elevator - 1).put(each.getText());
                }
                catch (InterruptedException ex) {
                    System.out.println("电梯按钮未成功发出消息");
                }

            });
        }
    }

    public void re_init_elevator_buttons() {
        for (JFXButton each : elevator_buttoon_list) {
            each.setStyle("-fx-background-color: #edb239;");
        }
    }
}
