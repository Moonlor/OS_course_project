package DocumentSystem;

import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;

import java.io.Serializable;
import java.util.*;

public class FCB implements Serializable{

    private static final int SIZE_PER_BLOCK = 1024;

    public String name;
    public String type;
    public String owner;
    public String content;

    public int inode_index;
    public int actual_size;
    public int occupied_size;

    public String address;

    public Date created_at;
    public Date updated_at;

    public Map<String, FCB> path_tree;

    FCB(String name, String type, String owner, List<String> pwd){
        this.name = name;
        this.type = type;
        this.owner = owner;

        this.created_at = new Date();
        this.updated_at = new Date();

        g_path(pwd);
    }

    @Override
    public String toString() {
        return this.name+" "+this.type+" "+this.owner+" "+this.content+" "+this.path_tree.toString()+" "
                +Integer.toString(this.inode_index)+" "+this.updated_at.toString()+" "+address
                +" "+this.created_at.toString()+" "+Integer.toString(this.actual_size)+" "+Integer.toString(this.occupied_size);
    }

    public void g_path(List<String> pwd){
        String temp = "/";
        for (String each : pwd){
            temp = temp + each + "/";
        }
        address = temp;
    }

    public void update_size(){
        long size = ObjectSizeCalculator.getObjectSize(name) + ObjectSizeCalculator.getObjectSize(content);
        actual_size = (int) (size / 8);
        occupied_size = (int) (Math.ceil( (double)size / 8 / SIZE_PER_BLOCK) * SIZE_PER_BLOCK);
    }

    public void setInode_index(int inode_index) {
        this.inode_index = inode_index;
    }

    protected void finalize(){
        System.out.println("[!]" + this.getName() + "deleted");
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setActual_size(int actual_size) {
        this.actual_size = actual_size;
    }

    public void setOccupied_size(int occupied_size) {
        this.occupied_size = occupied_size;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }
}
