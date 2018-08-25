package DocumentSystem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Inode {

    private int _inode_index;
    private Date _created_at;
    private Date _updated_at;
    private int _size;

    private static final int MAX_DIRECT_INDEX = 12;
    private static final int MAX_INDIRECT_INDEX = 3;

    public List<Block> _direct_access_blocks = new ArrayList<Block>();
    public List<Block> _indirect_access_blocks = new ArrayList<Block>();

    Inode(int index){
        _inode_index = index;
        _created_at = new Date();
        _updated_at = new Date();
        _size = 0;
    }

    public int get_size() {
        return _size;
    }

    public void set_size(int _size) {
        this._size = _size;
    }

    public int get_inode_index() {
        return _inode_index;
    }

    public void set_inode_index(int _inode_index) {
        this._inode_index = _inode_index;
    }

    public Date get_created_at() {
        return _created_at;
    }

    public void set_created_at(Date _created_at) {
        this._created_at = _created_at;
    }

    public Date get_updated_at() {
        return _updated_at;
    }

    public void set_updated_at(Date _updated_at) {
        this._updated_at = _updated_at;
    }

    public List<Block> get_direct_access_blocks() {
        return _direct_access_blocks;
    }

    public List<Block> get_indirect_access_blocks() {
        return _indirect_access_blocks;
    }
}
