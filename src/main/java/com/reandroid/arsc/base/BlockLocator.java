/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.arsc.base;

public class BlockLocator extends BlockCounter{
    private final int bytePosition;
    private Block current;
    public BlockLocator(int bytePosition) {
        super(null);
        this.bytePosition = bytePosition;
    }
    @Override
    public void setCurrent(Block current){
        if(FOUND){
            return;
        }
        this.current = current;
        if(getCount() >= bytePosition){
            FOUND = true;
        }
    }
    public int getBytePosition() {
        return bytePosition;
    }
    public Block getResult(){
        if(FOUND){
            return current;
        }
        return null;
    }
    @Override
    public String toString(){
        if(!FOUND){
            return getCount() + "/" + bytePosition;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Found at: ").append(getCount());
        Block current = this.current;
        builder.append(", block = [").append(current).append(']');
        if(current == null){
            return builder.toString();
        }
        Block parent = current.getParent();
        if(parent != null){
            builder.append(", parent = [").append(parent).append(']');
        }
        return builder.toString();
    }
}
