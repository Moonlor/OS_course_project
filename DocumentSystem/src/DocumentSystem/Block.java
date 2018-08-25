package DocumentSystem;

import java.util.ArrayList;
import java.util.List;

public class Block {

    private static final int SIZE_PER_BLOCK = 1024;

    private int _block_size;

    private int _block_index;

    Block(int index){
        _block_size = SIZE_PER_BLOCK;
        _block_index = index;
    }

    public int get_block_size() {
        return _block_size;
    }

    public void set_block_size(int _block_size) {
        this._block_size = _block_size;
    }

}
