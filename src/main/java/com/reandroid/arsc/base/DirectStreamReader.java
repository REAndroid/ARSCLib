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

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.item.BlockItem;

import java.io.IOException;
import java.io.InputStream;

public interface DirectStreamReader {
    int readBytes(InputStream inputStream) throws IOException;

    class Util {
        public static int readBlock(Block block, InputStream inputStream) throws IOException {
            if (block == null) {
                return 0;
            }
            int result = 0;
            if (block instanceof DirectStreamReader) {
                result = ((DirectStreamReader)block).readBytes(inputStream);
            } else if (block instanceof BlockItem) {
                result = ((BlockItem)block).readBytes(inputStream);
            } else if (block instanceof BlockList) {
                BlockList<?> list = (BlockList<?>) block;
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    result += readBlock(list.get(i), inputStream);
                }
            } else if (block instanceof SingleBlockContainer) {
                return readBlock(((SingleBlockContainer<?>) block).block(), inputStream);
            } else if (block instanceof BlockContainer) {
                Object[] blocks = ((BlockContainer) block).blocks();
                for (Object element : blocks) {
                    result += readBlock((Block) element, inputStream);
                }
            } else {
                throw new IOException("Can not read block type: " + block.getClass());
            }
            return result;
        }
    }
}
