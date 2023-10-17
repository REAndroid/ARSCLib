package com.reandroid.dex.key;

import com.reandroid.dex.id.ProtoId;
import com.reandroid.utils.CompareUtil;

import java.util.Objects;

public class ProtoKey implements Key{

    private final String[] parameters;
    private final String returnType;

    private int mHash;

    public ProtoKey(String[] parameters, String returnType){
        this.parameters = parameters;
        this.returnType = returnType;
    }

    public ProtoKey removeParameter(int index){
        TypeListKey typeListKey = getParametersKey();
        if(typeListKey != null){
            typeListKey = typeListKey.removeParameter(index);
        }
        return create(typeListKey, getReturnType());
    }

    public TypeKey getReturnTypeKey() {
        return new TypeKey(getReturnType());
    }
    public TypeListKey getParametersKey() {
        return TypeListKey.create(getParameters());
    }

    public String[] getParameters() {
        return parameters;
    }
    public int getParametersCount() {
        String[] parameters = getParameters();
        if(parameters != null){
            return parameters.length;
        }
        return 0;
    }
    public String getParameter(int i){
        return getParameters()[i];
    }
    public TypeKey getParameterType(int i){
        return TypeKey.create(getParameter(i));
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
        int hash = mHash;
        if(hash != 0){
            return hash;
        }
        hash = 1;
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
        mHash = hash;
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
    public static ProtoKey create(TypeListKey typeListKey, String returnType){
        if(returnType == null){
            return null;
        }
        String[] parameters = null;
        if(typeListKey != null){
            parameters = typeListKey.getParameters();
        }
        return new ProtoKey(parameters, returnType);
    }

    private static char toShorty(String typeName) {
        if(typeName.length() == 1){
            return typeName.charAt(0);
        }
        return 'L';
    }
}
