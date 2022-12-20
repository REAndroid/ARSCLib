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
package com.reandroid.lib.common;

import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.group.EntryGroup;
import com.reandroid.lib.arsc.item.TableString;

import java.util.Collection;

public interface EntryStore {
    EntryGroup searchEntryGroup(String packageName, String type, String name);
    Collection<EntryGroup> getEntryGroups(int resourceId);

    EntryGroup getEntryGroup(int resourceId);

    Collection<PackageBlock> getPackageBlocks(byte packageId);

    Collection<TableString> getTableStrings(byte packageId, int stringReference);
}
