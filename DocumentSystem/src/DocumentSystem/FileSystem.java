package DocumentSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSystem {

    public static final String DIR = "文件夹";
    public static final String FILE = "文本文件";

    public Map<String, FCB> path_tree = new HashMap<String, FCB>();
    public List<String> pwd = new ArrayList<String>();

    FAT fat;

    FileSystem(){
        fat = new FAT();
        return;
    }

    FileSystem(Map<String, FCB> tree){
        fat = new FAT();
        this.path_tree = tree;
    }

    Map<String, FCB> ls(){
        Map<String, FCB> current_dir = path_tree;
        for (String path : pwd) {
            current_dir =  current_dir.get(path).path_tree;
        }

        return current_dir;
    }

    void mkdir(FCB dir){
        Map<String, FCB> current_dir = ls();
        dir.path_tree = new HashMap<String, FCB>();
        dir.update_size();
        dir.setInode_index(fat.getFreeInode(dir.actual_size));

        current_dir.put(dir.name, dir);
    }

    void mkfile(FCB file){
        Map<String, FCB> current_dir = ls();
        file.update_size();
        file.setInode_index(fat.getFreeInode(file.actual_size));
        current_dir.put(file.name, file);
    }

    void cd(String name){
        Map<String, FCB> current_dir = ls();
        if (current_dir.containsKey(name)){
            pwd.add(name);
        }
    }

    boolean cd_back(){
        if (pwd.size() >= 1){
            pwd.remove(pwd.size() - 1);
            return true;
        } else  {
            return false;
        }
    }

    void format(){
        List<String> name_list = new ArrayList<String>();
        for (Map.Entry<String, FCB> item : path_tree.entrySet()){
            name_list.add(item.getKey());
        }
        for (String e: name_list) {
            path_tree.remove(e);
        }
        pwd.removeAll(pwd);
    }

    void delete(String name){
        Map<String, FCB> current_dir = ls();
        current_dir.remove(name);
    }


}
