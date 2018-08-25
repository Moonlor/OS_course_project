package elevator;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.List;

import com.jfoenix.controls.JFXButton;

import javafx.application.*;
import javafx.scene.control.*;

public class Elevator implements Runnable {

    public TreeSet task_set = new TreeSet();

    private int elevator_id;
    private BlockingQueue queue;

    private List<JFXButton> elevator_buttoon_list;
    private List<Slider>    slider_list;
    private List<JFXButton> floor_dis_button_list;
    private List<Integer> elevator_status;
    private List<Integer> elevator_floor;
    private List<JFXButton> floor_up_button_list;
    private List<JFXButton> floor_down_button_list;

    private static final int DEFAULT_RANGE_FOR_SLEEP = 1000;
    private static final int NO_MORE_TASK = 404;
    private static final int REST = 0;
    private static final int UP = 1;
    private static final int DOWN = 2;
    private static final String REST_ICON = "-";
    private static final String UP_ICON = "↑";
    private static final String DOWN_ICON = "↓";

    private int status_code; //0为静止，1为上升，2为下降
    private int floor;
    private int remove_floor_btn = 0;
    private int remove_floor = 0;


    public Elevator(int id, BlockingQueue q, List<JFXButton> btn_ls, List<Slider> sld,
                    List<JFXButton> dis_btn, List<Integer> e_status, List<Integer> e_floor,
                    List<JFXButton> u_btn, List<JFXButton> d_btn)
    {
        this.elevator_id = id;
        this.status_code = REST;
        this.floor = 1;
        this.queue = q;
        this.elevator_buttoon_list = btn_ls;
        this.slider_list = sld;
        this.floor_dis_button_list = dis_btn;
        this.elevator_status = e_status;
        this.elevator_floor = e_floor;
        this.floor_up_button_list = u_btn;
        this.floor_down_button_list = d_btn;
    }

    public void sleep(int sec){
        try {
            Thread.sleep( sec * DEFAULT_RANGE_FOR_SLEEP);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public int get_higher_nearest_task(){
        if (task_set.size() == 0){
            return NO_MORE_TASK;
        }
        for(Object each : task_set){
            if ((int)each >= floor){
                return (int)each;
            }
        }
        return NO_MORE_TASK;
    }

    public int get_lower_nearest_task(){
        if (task_set.size() == 0){
            return NO_MORE_TASK;
        }
        int find_task = floor;
        for(Object each : task_set){
            if ((int)each > floor){
                return find_task >= floor? NO_MORE_TASK : find_task;
            }
            find_task = (int)each;
        }
        return find_task;
    }

    public int get_nearest_task(){
        if (task_set.size() == 0){
            return NO_MORE_TASK;
        }
        int find_task = floor;
        int differ = 9999;
        for(Object each : task_set){
            if (Math.abs((int)each - floor) < differ){
                find_task = (int)each;
                differ = Math.abs((int)each - floor);
            }
        }
        return find_task;
    }

    public void move(){

        boolean sleep_flag = false;

        if (task_set.size() == 0){
            status_code = REST;
            return;
        }

        if (floor == 1 || floor == 20){
            status_code = REST;
        }

        System.out.println(task_set);

        if (status_code == REST){
            int c_task = get_nearest_task();
            System.out.println("从静止状态前往：" + String.valueOf(c_task));
            task_set.remove(c_task);
            status_code = (c_task - floor) > 0 ? UP : DOWN;

            int divisor = Math.abs(c_task - floor);
            floor += (c_task - floor) / (divisor != 0 ? divisor : 1);
            if (floor != c_task){
                task_set.add(c_task);
            } else {
                sleep_flag = true;
            }
        }
        else if (status_code == UP){
            int c_task = get_higher_nearest_task();
            if (c_task == NO_MORE_TASK){
                status_code = DOWN;
                move();
                return;
            }
            System.out.println("从上升状态前往：" + String.valueOf(c_task));
            task_set.remove(c_task);

            int divisor = Math.abs(c_task - floor);
            floor += (c_task - floor) / (divisor != 0 ? divisor : 1);
            if (floor != c_task){
                task_set.add(c_task);
            } else {
                sleep_flag = true;
            }
            status_code = task_set.isEmpty() ? REST : UP;
        }
        else if (status_code == DOWN){
            int c_task = get_lower_nearest_task();
            if (c_task == NO_MORE_TASK){
                status_code = UP;
                move();
                return;
            }
            System.out.println("从下降状态前往：" + String.valueOf(c_task));
            task_set.remove(c_task);

            floor += (c_task - floor)/Math.abs(c_task - floor);
            if (floor != c_task){
                task_set.add(c_task);
            } else {
                sleep_flag = true;
            }
            status_code = task_set.isEmpty() ? REST : DOWN;
        }

        Platform.runLater(new Runnable() { @Override public void run() {
            if (task_set.size() == 0){
                status_code = REST;
            }
            //设置电梯当前到达楼层
            elevator_floor.set(elevator_id - 1, floor);
            elevator_buttoon_list.get(floor - 1).setStyle("-fx-background-color: #edb239;");

            slider_list.get(elevator_id - 1).setValue(floor);

            floor_dis_button_list.get(elevator_id - 1).setText(
                    String.valueOf(floor) + (status_code == UP? UP_ICON : (status_code == DOWN? DOWN_ICON : REST_ICON)));
            elevator_status.set(elevator_id - 1, new Integer(status_code));
            if (remove_floor != 0){
                if (floor == remove_floor){
                    remove_floor = 0;
                    if (remove_floor_btn == UP){
                        for (JFXButton e : floor_up_button_list) {
                            e.setStyle("-fx-background-color: #ed9139");
                        }
                    }
                    else{
                        for (JFXButton e : floor_down_button_list) {
                            e.setStyle("-fx-background-color: #ed9139");
                        }
                    }
                }
            }
        } });
        if (sleep_flag){
            slider_list.get(elevator_id - 1).setStyle("-jfx-default-thumb: #c5c5c5");
            sleep(2);
            slider_list.get(elevator_id - 1).setStyle("-jfx-default-thumb: #0f9d58");
        }
    }

    public void run() {
        System.out.println("启动电梯" + String.valueOf(elevator_id) + "线程！");
        Random r = new Random();
        boolean isRunning = true;
        while (isRunning) {
            while (true){
                String data = (String) queue.poll();
                if (null != data) {
//                    System.out.println("电梯" + String.valueOf(elevator_id) + "拿到数据：" + data);
                    if (data.length() > 3){
                        remove_floor_btn = Integer.valueOf(data.split("\\|")[1]);
                        remove_floor = Integer.valueOf(data.split("\\|")[2]);
                    }
                    else{
                        task_set.add(Integer.valueOf(data));
                    }
                } else {
                    break;
                }
            }
            move();
            sleep(1);

        }

    }

}
