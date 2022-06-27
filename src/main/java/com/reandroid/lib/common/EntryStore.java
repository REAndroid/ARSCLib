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
