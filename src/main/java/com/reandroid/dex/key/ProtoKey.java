package com.reandroid.dex.key;

import com.reandroid.dex.id.ProtoId;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ArrayIterator;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.util.Iterator;
import java.util.Objects;

public class ProtoKey implements Key{

    private final String[] parameters;
    private final String returnType;

    private int mHash;

    public ProtoKey(String[] parameters, String returnType){
        this.parameters = parameters;
        this.returnType = returnType;
    }

    public int getRegister(int index) {
        int result = 0;
        int size = getParametersCount();
        if(index < size){
            size = index;
        }
        for(int i = 0; i < size; i++){
            TypeKey typeKey = getParameter(i);
            result ++;
            if(typeKey.equals(TypeKey.TYPE_D) || typeKey.equals(TypeKey.TYPE_J)){
                result ++;
            }
        }
        return result;
    }
    public ProtoKey removeParameter(int index){
        TypeListKey typeListKey = getParameterListKey();
        if(typeListKey != null){
            typeListKey = typeListKey.remove(index);
        }
        return create(typeListKey, getReturnTypeName());
    }
    public ProtoKey changeReturnType(TypeKey typeKey){
        return changeReturnType(typeKey.getTypeName());
    }
    public ProtoKey changeReturnType(String type){
        if(type.equals(getReturnTypeName())){
            return this;
        }
        return new ProtoKey(getParameterNames(), type);
    }

    public TypeListKey getParameterListKey() {
        return TypeListKey.create(getParameterNames());
    }
    public TypeKey getReturnType() {
        return new TypeKey(getReturnTypeName());
    }
    public String getReturnTypeName() {
        return returnType;
    }

    public int getParametersCount() {
        String[] parameters = getParameterNames();
        if(parameters != null){
            return parameters.length;
        }
        return 0;
    }
    public String[] getParameterNames() {
        return parameters;
    }
    public String getParameterName(int i){
        return getParameterNames()[i];
    }
    public TypeKey getParameter(int i){
        return TypeKey.create(getParameterName(i));
    }
    public Iterator<TypeKey> getParameters(){
        return ComputeIterator.of(ArrayIterator.of(getParameterNames()), TypeKey::create);
    }
    public String getShorty(){
        StringBuilder builder = new StringBuilder();
        builder.append(toShorty(getReturnTypeName()));
        String[] parameters = getParameterNames();
        if(parameters != null){
            for(String param : parameters){
                builder.append(toShorty(param));
            }
        }
        return builder.toString();
    }
    @Override
    public Iterator<Key> mentionedKeys() {
        return CombiningIterator.singleThree(
                ProtoKey.this,
                SingleIterator.of(StringKey.create(getShorty())),
                getParameters(),
                SingleIterator.of(getReturnType()));
    }
    @Override
    public Key replaceKey(Key search, Key replace) {
        ProtoKey result = this;
        if(search.equals(result)){
            return replace;
        }
        if(search.equals(result.getReturnType())){
            result = result.changeReturnType((TypeKey) replace);
        }
        String[] parameters = this.getParameterNames();
        if(parameters != null && search instanceof TypeKey){
            TypeKey searchType = (TypeKey) search;
            String replaceType = ((TypeKey) replace).getTypeName();
            int length = parameters.length;
            for(int i = 0; i < length; i++){
                if(searchType.equals(new TypeKey(parameters[i]))){
                    parameters[i] = replaceType;
                }
            }
        }
        return result;
    }

    @Override
    public int compareTo(Object obj) {
        if(obj == null){
            return -1;
        }
        ProtoKey key = (ProtoKey) obj;
        int i = CompareUtil.compare(getParameterNames(), key.getParameterNames());
        if(i != 0) {
            return i;
        }
        return CompareUtil.compare(getReturnTypeName(), key.getReturnTypeName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProtoKey)) {
            return false;
        }
        ProtoKey protoKey = (ProtoKey) obj;
        return Objects.equals(getReturnTypeName(), protoKey.getReturnTypeName()) &&
                CompareUtil.compare(getParameterNames(), protoKey.getParameterNames()) == 0;
    }

    @Override
    public int hashCode() {
        int hash = mHash;
        if(hash != 0){
            return hash;
        }
        hash = 1;
        String type = getReturnTypeName();
        if(type != null){
            hash += type.hashCode();
        }
        String[] parameters = getParameterNames();
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
        String[] parameters = getParameterNames();
        if(parameters != null){
            for (String parameter : parameters) {
                builder.append(parameter);
            }
        }
        builder.append(')');
        String type = getReturnTypeName();
        if(type != null){
            builder.append(type);
        }
        return builder.toString();
    }


    public static ProtoKey create(ProtoId protoId){
        TypeKey returnType = protoId.getReturnType();
        if(returnType == null){
            return null;
        }
        return new ProtoKey(protoId.getParameterNames(), returnType.getTypeName());
    }
    public static ProtoKey create(TypeListKey typeListKey, String returnType){
        if(returnType == null){
            return null;
        }
        String[] parameters = null;
        if(typeListKey != null){
            parameters = typeListKey.getParameterNames();
        }
        return new ProtoKey(parameters, returnType);
    }

    private static char toShorty(String typeName) {
        if(typeName.length() == 1){
            return typeName.charAt(0);
        }
        if(typeName.length() == 0){
            throw new RuntimeException();
        }
        return 'L';
    }
}
