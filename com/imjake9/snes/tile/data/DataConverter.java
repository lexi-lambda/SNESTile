package com.imjake9.snes.tile.data;


public class DataConverter {
    
    /**
     * Converts data from the SNES 4BPP format to a packed array of palette values.
     * 
     * @param data data to convert
     * @return converted array
     */
    public static byte[] fromSNES4BPP(byte[] data) {
        byte[] tileData = new byte[data.length*2];
        
        for (int i = 0; i < tileData.length; i++) {
            int tile = i / 64;
            int row = (i % 64) / 8;
            int col = i % 8;
            
            byte value = 0;
            value |= getBit(data[0 +  tile*32 + row*2], (byte) 7 - col);
            value |= getBit(data[1 +  tile*32 + row*2], (byte) 7 - col) << 1;
            value |= getBit(data[16 + tile*32 + row*2], (byte) 7 - col) << 2;
            value |= getBit(data[17 + tile*32 + row*2], (byte) 7 - col) << 3;
            
            tileData[i] = value;
        }
        
        return tileData;
    }
    
    public static byte[] toSNES4BPP(byte[] tileData) {
        byte[] data = new byte[tileData.length/2];
        
        for (int i = 0; i < tileData.length; i++) {
            int tile = i / 64;
            int row = (i % 64) / 8;
            int col = i % 8;
            
            data[0 +  tile*32 + row*2] |= getBit(tileData[i], 0) << 7 - col;
            data[1 +  tile*32 + row*2] |= getBit(tileData[i], 1) << 7 - col;
            data[16 + tile*32 + row*2] |= getBit(tileData[i], 2) << 7 - col;
            data[17 + tile*32 + row*2] |= getBit(tileData[i], 3) << 7 - col;
        }
        
        return data;
    }
    
    /**
     * Gets the value of a bit in a byte.
     * 
     * @param value byte value to check
     * @param bit which bit to check
     * @return either 0 or 1
     */
    public static byte getBit(byte value, int bit) {
        return (byte) (value >>> bit & 0x1);
    }
    
    /**
     * Converts a hex string into a byte array.
     * A convenience method for use in debugging.
     * 
     * @param data hex string
     * @return byte array
     */
    public static byte[] toByteArray(String data) {
        byte[] array = new byte[data.length()/2];
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) Integer.parseInt(data.substring(i*2, i*2 + 2), 16);
        }
        return array;
    }
    
}
