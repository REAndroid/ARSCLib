package com.reandroid.dex.key;

import com.reandroid.dex.index.ProtoId;
import com.reandroid.utils.CompareUtil;

import java.util.Objects;

public class ProtoKey implements Key{

    private final String[] parameters;
    private final String returnType;

    public ProtoKey(String[] parameters, String returnType){
        this.parameters = parameters;
        this.returnType = returnType;
    }

    public TypeKey getReturnTypeKey() {
        return new TypeKey(getReturnType());
    }
    public TypeListKey getParametersKey() {
        return new TypeListKey(getParameters());
    }

    public String[] getParameters() {
        return parameters;
    }
    public String getReturnType() {
        return returnType;
    }
    public String getShorty(){
        StringBuilder builder = new StringBuilder();
        builder.append(toShorty(getReturnType()));
        String[] parameters = getParameters();
        if(parameters != null){
            for(String param : parameters){
                builder.append(toShorty(param));
            }
        }
        return builder.toString();
    }

    @Override
    public int compareTo(Object obj) {
        if(obj == null){
            return -1;
        }
        ProtoKey key = (ProtoKey) obj;
        int i = CompareUtil.compare(getParameters(), key.getParameters());
        if(i != 0) {
            return i;
        }
        return CompareUtil.compare(getReturnType(), key.getReturnType());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProtoKey)) {
            return false;
        }
        ProtoKey methodKey = (ProtoKey) obj;
        return Objects.equals(getReturnType(), methodKey.getReturnType()) &&
                CompareUtil.compare(getParameters(), methodKey.getParameters()) == 0;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        String type = getReturnType();
        if(type != null){
            hash += type.hashCode();
        }
        String[] parameters = getParameters();
        if(parameters != null){
            for(String param : parameters){
                hash = hash * 31 + param.hashCode();
            }
        }
        return hash;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        String[] parameters = getParameters();
        if(parameters != null){
            for (String parameter : parameters) {
                builder.append(parameter);
            }
        }
        builder.append(')');
        String type = getReturnType();
        if(type != null){
            builder.append(type);
        }
        return builder.toString();
    }


    public static ProtoKey create(ProtoId protoId){
        String returnType = protoId.getReturnType();
        if(returnType == null){
            return null;
        }
        return new ProtoKey(protoId.getParameterNames(), returnType);
    }

    private static char toShorty(String typeName) {
        if(typeName.length() == 1){
            return typeName.charAt(0);
        }
        return 'L';
    }
}
