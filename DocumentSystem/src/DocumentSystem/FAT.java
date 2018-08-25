package DocumentSystem;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.ArrayList;
import java.util.List;

public class FAT {

    public static final int MAX_INODE_NUMBER = 128;
    public static final int MAX_BLOCK_NUMBER = 128 * 12;

    public List<Inode> _inode_table = new ArrayList<Inode>();
    public List<Block> _block_table = new ArrayList<Block>();
    public boolean[] _inode_bit_map;
    public boolean[] _block_bit_map;

    private static final int SIZE_PER_BLOCK = 1024;

    FAT(){

        _inode_bit_map = new boolean[MAX_INODE_NUMBER];
        _block_bit_map = new boolean[MAX_BLOCK_NUMBER];

        for (int i = 0; i < MAX_INODE_NUMBER; i++) {
            _inode_bit_map[i] = true;
            _inode_table.add(new Inode(i));
        }

        for (int i = 0; i < MAX_BLOCK_NUMBER; i++) {
            _block_bit_map[i] = true;
            _block_table.add(new Block(i));
        }
    }

    int getFreeInode(int size){
        for (int i = 0; i < MAX_INODE_NUMBER; i++) {
            if (_inode_bit_map[i]){
//                getFreeBlock4Inode(i, size);
                _inode_bit_map[i] = false;
                return _inode_table.get(i).get_inode_index();
            }
        }

        return -1;
    }

    void getFreeBlock4Inode(int inode_index, int size){
        int blocks_needed = (int) Math.ceil(size / SIZE_PER_BLOCK);
        while (blocks_needed > 0){

            int i = 0;
            for (; i < MAX_INODE_NUMBER; i++) {
                if (_block_bit_map[i]){
                    _block_bit_map[i] = false;
                    _inode_table.get(inode_index)._direct_access_blocks.add(_block_table.get(i));
                }
            }
        }
    }

}
