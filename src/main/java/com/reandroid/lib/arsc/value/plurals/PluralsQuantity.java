package com.reandroid.lib.arsc.value.plurals;


public enum PluralsQuantity {
    OTHER((short) 0x0004),
    ZERO((short) 0x0005),
    ONE((short) 0x0006),
    TWO((short) 0x0007),
    FEW((short) 0x0008),
    MANY((short) 0x0009);

    private final short mId;
    PluralsQuantity(short id) {
        this.mId=id;
    }
    public short getId() {
        return mId;
    }
    @Override
    public String toString(){
        return name().toLowerCase();
    }
    public static PluralsQuantity valueOf(short id){
        PluralsQuantity[] all=values();
        for(PluralsQuantity pq:all){
            if(id==pq.mId){
                return pq;
            }
        }
        return null;
    }
    public static PluralsQuantity value(String name){
        if(name==null){
            return null;
        }
        name=name.toUpperCase();
        PluralsQuantity[] all=values();
        for(PluralsQuantity pq:all){
            if(name.equals(pq.name())){
                return pq;
            }
        }
        return null;
    }
}
