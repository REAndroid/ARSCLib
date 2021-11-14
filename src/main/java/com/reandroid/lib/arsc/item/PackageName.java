package com.reandroid.lib.arsc.item;

import com.reandroid.lib.arsc.io.BlockReader;

import java.nio.charset.StandardCharsets;

public class PackageName extends StringItem {
    public PackageName() {
        super(true);
        setBytesLength(BYTES_LENGTH);
    }
    @Override
    byte[] encodeString(String str){
        if(str==null){
            return new byte[BYTES_LENGTH];
        }
        byte[] bts=getUtf16Bytes(str);
        byte[] results=new byte[BYTES_LENGTH];
        int len=bts.length;
        if(len>BYTES_LENGTH){
            len=BYTES_LENGTH;
        }
        System.arraycopy(bts, 0, results, 0, len);
        return results;
    }
    @Override
    String decodeString(){
        return decodeUtf16Bytes(getBytesInternal());
    }
    @Override
    StyleItem getStyle(){
        return null;
    }
    @Override
    int calculateReadLength(BlockReader reader){
        return BYTES_LENGTH;
    }
    private static String decodeUtf16Bytes(byte[] bts){
        if(isNullBytes(bts)){
            return null;
        }
        int len=getEndNullPosition(bts);
        return new String(bts,0, len, StandardCharsets.UTF_16LE);
    }
    private static int getEndNullPosition(byte[] bts){
        int max=bts.length;
        int result=0;
        boolean found=false;
        for(int i=1; i<max;i++){
            byte b0=bts[i-1];
            byte b1=bts[i];
            if(b0==0 && b1==0){
                if(!found){
                    result=i;
                    found=true;
                }else if(result<i-1){
                    return result;
                }
            }else {
                found=false;
            }
        }
        if(!found){
            return max;
        }
        return result;
    }
    private static final int BYTES_LENGTH=256;

}
