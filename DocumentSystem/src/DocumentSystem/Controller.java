package DocumentSystem;

import com.jfoenix.controls.*;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;

import javax.annotation.PostConstruct;
import javax.swing.text.TabableView;
import java.awt.datatransfer.FlavorEvent;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller {

    public static final String CONTENT_PANE = "ContentPane";
    public static final String DIR = "文件夹";
    public static final String FILE = "文本文件";
    public static final int NOT_SELECTED = -1;
    public static final int EDIT_MODE = 1;
    public static final int CREATE_MODE = 2;
    private static final int SIZE_PER_BLOCK = 1024;

    @FXML
    private JFXButton path;
    @FXML
    private TableView ls;
    @FXML
    private TableColumn name;
    @FXML
    private TableColumn type;

    @FXML
    private StackPane root;

    @FXML
    private JFXButton format;
    @FXML
    private JFXButton delete;
    @FXML
    private JFXButton mkfile;
    @FXML
    private JFXButton mkdir;
    @FXML
    private JFXButton back;
    @FXML
    private JFXButton cd;
    @FXML
    private JFXButton edit;
    @FXML
    private JFXButton info;
    @FXML
    private JFXButton exit;

    @FXML
    private JFXDialog mkfile_dialog;
    @FXML
    private JFXTextField mkfile_name_area;
    @FXML
    private JFXTextArea mkfile_content_area;
    @FXML
    private JFXButton mkfile_cancel_button;
    @FXML
    private JFXButton mkfile_save_button;

    @FXML
    private JFXDialog mkdir_dialog;
    @FXML
    private JFXTextField mkdir_name_area;
    @FXML
    private JFXButton mkdir_cancel_button;
    @FXML
    private JFXButton mkdir_save_button;

    @FXML
    private JFXDialog info_dialog;
    @FXML
    private JFXButton f_name;
    @FXML
    private JFXButton f_type;
    @FXML
    private JFXButton f_owner;
    @FXML
    private JFXButton f_inode_index;
    @FXML
    private JFXButton f_address;
    @FXML
    private JFXButton f_created_at;
    @FXML
    private JFXButton f_updated_at;
    @FXML
    private JFXButton f_actual_size;
    @FXML
    private JFXButton f_occupied_size;
    @FXML
    private JFXButton f_blocks;
    @FXML
    private JFXButton info_accept_button;

    public ObservableList<FCB> files = FXCollections.observableArrayList();

    public Map<String, Object> component = new HashMap<String, Object>();
    public FileSystem fs;

    public int selected_index = -1;
    public int edit_mode = CREATE_MODE;

    public boolean file_not_found_flag = false;

    void alert_dialog(String head,String content){
        JFXAlert alert = new JFXAlert((Stage) root.getScene().getWindow());
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setOverlayClose(false);
        JFXDialogLayout layout = new JFXDialogLayout();
        JFXButton h = new JFXButton(head);
        h.setStyle("-fx-background-color: #eda451;\n" +
                "    -fx-font-weight: BOLD;\n" +
                "    -fx-font-size: 17px;\n" +
                "    -fx-text-fill:WHITE;");
        layout.setHeading(h);
        layout.setBody(new Label(content));
        JFXButton closeButton = new JFXButton("确定");
        closeButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> alert.hideWithAnimation());
        layout.setActions(closeButton);
        alert.setContent(layout);
        alert.show();
    }

    void init(){
        component.put("ls", ls);
        component.put("name", name);
        component.put("type", type);
        component.put("format", format);
        component.put("delete", delete);
        component.put("mkfile", mkfile);
        component.put("mkdir", mkdir);
        component.put("back", back);
        component.put("cd", cd);
        component.put("edit", edit);
        component.put("info", info);

        component.put("mkfile_dialog", mkfile_dialog);
        component.put("mkfile_name_area", mkfile_name_area);
        component.put("mkfile_content_area", mkfile_content_area);
        component.put("mkfile_cancel_button", mkfile_cancel_button);
        component.put("mkfile_save_button", mkfile_save_button);

        component.put("mkdir_dialog", mkdir_dialog);
        component.put("mkdir_name_area", mkdir_name_area);
        component.put("mkdir_cancel_button", mkdir_cancel_button);
        component.put("mkdir_save_button", mkdir_save_button);


        Map<String, FCB> data = getObjFromFile();

        if (file_not_found_flag){
            fs = new FileSystem();
        } else {
            fs = new FileSystem(data);
            re_index();
        }

        init_control_button();
        init_table();
        filtering();

        //退出程序
        root.getScene().getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                saveObjToFile(fs.path_tree);
                System.exit(0);
            }
        });
    }

    void re_index(){
        Map<String, FCB> dir = fs.ls();

        files.removeAll(files);

        for (Map.Entry<String, FCB> item : dir.entrySet()){
            files.add(item.getValue());
        }

        edit.setDisable(false);
        cd.setDisable(false);
        selected_index = NOT_SELECTED;
        filtering();

        String p = "当前路径 : /";
        for (String each : fs.pwd){
            p = p + each + "/";
        }
        path.setText(p);

    }

    void init_table(){
        name.setCellValueFactory(new PropertyValueFactory("name"));
        type.setCellValueFactory(new PropertyValueFactory("type"));
        ls.setItems(files);


        ls.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        ls.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                int index = ls.getSelectionModel().getSelectedIndex();
                if (index < 0){
                    return;
                }
                System.out.println(files.get(index).name);
                selected_index = index;
                filtering();
            }
        });
    }

    void init_dialog(){
        mkfile_name_area.clear();
        mkfile_content_area.clear();

        mkdir_name_area.clear();
    }

    void init_control_button(){

        //创建文本
        mkfile.setOnAction(action -> {

            init_dialog();
            edit_mode = CREATE_MODE;

            mkfile_dialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
            mkfile_dialog.show(root);
        });

        mkfile_save_button.setOnAction(action -> {
            String name = mkfile_name_area.getText().isEmpty() ? "新建文件" : mkfile_name_area.getText();
            String text = mkfile_content_area.getText();
            FCB temp;

            if (edit_mode == EDIT_MODE){
                temp = files.get(selected_index);
                temp.name = name;
                temp.content = text;
                temp.updated_at = new Date();
                temp.update_size();
            } else {

                Map<String, FCB> dir = fs.ls();
                int index = 1;
                String test_name = name;
                while (dir.containsKey(test_name)){
                    test_name = name + Integer.toString(index);
                    index += 1;
                }
                name = test_name;

                temp = new FCB(name, FILE, "root", fs.pwd);
                temp.setContent(text);
                fs.mkfile(temp);
                files.add(temp);
            }

            edit_mode = CREATE_MODE;

            mkfile_dialog.close();
            re_index();

        });

        mkfile_cancel_button.setOnAction(action -> {
            mkfile_dialog.close();
        });

        //编辑文本
        edit.setOnAction(action -> {
            FCB temp = files.get(selected_index);
            edit_mode = EDIT_MODE;

            init_dialog();
            mkfile_dialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
            mkfile_dialog.show(root);
            mkfile_name_area.setText(temp.name);
            mkfile_content_area.setText(temp.content);

        });

        //创建文件夹
        mkdir.setOnAction(action -> {

            init_dialog();

            mkdir_dialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
            mkdir_dialog.show(root);
        });

        mkdir_save_button.setOnAction(action -> {
            String name = mkdir_name_area.getText().isEmpty() ? "新建文件夹" : mkdir_name_area.getText();

            Map<String, FCB> dir = fs.ls();
            int index = 1;
            String test_name = name;
            while (dir.containsKey(test_name)){
                test_name = name + Integer.toString(index);
                index += 1;
            }
            name = test_name;

            FCB temp = new FCB(name, DIR, "root", fs.pwd);
            fs.mkdir(temp);
            files.add(temp);

            mkdir_dialog.close();
        });

        mkdir_cancel_button.setOnAction(action -> {
            mkdir_dialog.close();
        });

        //删除选中项
        delete.setOnAction(action -> {
            FCB temp = files.get(selected_index);
            fs.delete(temp.name);
            files.remove(temp);
            re_index();
            filtering();
        });

        //格式化
        format.setOnAction(action -> {
            fs.format();
            re_index();
        });

        //进入子目录
        cd.setOnAction(action -> {
            fs.pwd.add(files.get(selected_index).name);
            re_index();
        });

        //返回上一级目录
        back.setOnAction(action -> {
            if (fs.pwd.size() <= 0){
                alert_dialog("错误", "已经位于根目录，无上一级目录");
                return;
            }

            fs.pwd.remove(fs.pwd.size() - 1);
            re_index();
        });

        //查看文件信息
        info.setOnAction(action -> {
            info_dialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
            info_dialog.show(root);
            display_info();
        });

        info_accept_button.setOnAction(action -> {
            info_dialog.close();
        });

        //关闭退出
        exit.setOnAction(action -> {
            saveObjToFile(fs.path_tree);
            System.exit(0);
        });



    }

    void filtering(){

        if (selected_index == NOT_SELECTED){
            edit.setDisable(true);
            cd.setDisable(true);
            delete.setDisable(true);
            info.setDisable(true);

            return;
        }

        delete.setDisable(false);
        info.setDisable(false);
        if (files.get(selected_index).type.equals(DIR)){
            edit.setDisable(true);
            cd.setDisable(false);
        } else if (files.get(selected_index).type.equals(FILE)){
            cd.setDisable(true);
            edit.setDisable(false);
        }

    }

    public void saveObjToFile(Map<String, FCB> path_tree){
        try {
            //写对象流的对象
            ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream("data.dmg"));

            oos.writeObject(path_tree);                 //将Person对象p写入到oos中

            oos.close();                        //关闭文件流
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Map<String, FCB> getObjFromFile(){
        try {
            ObjectInputStream ois=new ObjectInputStream(new FileInputStream("data.dmg"));

            Map<String, FCB> test = (Map<String, FCB>)ois.readObject();              //读出对象

            return test;                                       //返回对象
        } catch (FileNotFoundException e) {
            file_not_found_flag = true;
//            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    void display_info(){
        FCB temp = temp = files.get(selected_index);
        f_name.setText(temp.name);
        f_type.setText(temp.type);
        f_owner.setText(temp.owner);
        f_inode_index.setText(String.valueOf(temp.inode_index));
        f_address.setText(temp.address);
        f_created_at.setText(temp.created_at.toString());
        f_updated_at.setText(temp.updated_at.toString());
        f_actual_size.setText(String.valueOf(temp.actual_size) + " Byte");
        f_occupied_size.setText(String.valueOf(temp.occupied_size) + " Byte");
        f_blocks.setText(String.valueOf(temp.occupied_size / SIZE_PER_BLOCK));
    }
}

