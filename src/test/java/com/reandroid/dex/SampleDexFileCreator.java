package com.reandroid.dex;

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.*;

import java.io.IOException;

public class SampleDexFileCreator {

    public static DexFile createApplicationClass(String appSourceName, String activitySourceName, int contentViewResourceId) throws IOException {
        return createApplicationClass(false, appSourceName, activitySourceName, contentViewResourceId);
    }
    public static DexFile createApplicationClass(boolean multiContainerDex, String appSourceName, String activitySourceName, int contentViewResourceId) throws IOException {
        DexFile dexFile = DexFile.createDefault();
        createApplicationClass(dexFile, appSourceName);
        int i = 0;
        if (multiContainerDex) {
            dexFile.setVersion(41);
            i = 1;
        }
        createActivityClass(dexFile.getOrCreateAt(i), activitySourceName, contentViewResourceId);
        dexFile.clearUnused();
        dexFile.clearEmptySections();
        dexFile.refreshFull();
        return dexFile;
    }
    private static void createApplicationClass(DexFile dexFile, String classSourceName) throws IOException {
        if(classSourceName == null){
            return;
        }
        TypeKey typeKey = TypeKey.parse(classSourceName);
        DexLayout dexLayout = dexFile.getOrCreateFirst();
        DexClass dexClass = dexLayout.getOrCreateClass(typeKey);
        dexClass.addAccessFlag(AccessFlag.PUBLIC);
        dexClass.setSuperClass(TypeKey.create("Landroid/app/Application;"));

        createConstructor(dexClass);
    }
    private static void createActivityClass(DexLayout dexLayout, String activitySourceName, int contentViewResourceId) throws IOException {

        DexClass dexClass = dexLayout.getOrCreateClass(TypeKey.parse(activitySourceName));
        dexClass.addAccessFlag(AccessFlag.PUBLIC);
        dexClass.setSuperClass(TypeKey.create("Landroid/app/Activity;"));

        createConstructor(dexClass);
        create_onCreate(dexClass, contentViewResourceId);
    }

    private static void createConstructor(DexClass dexClass) throws IOException {
        MethodKey methodKey = MethodKey.CONSTRUCTOR.changeDeclaring(dexClass.getKey());
        DexMethod constructor = dexClass.getOrCreateDirectMethod(methodKey);

        constructor.addAccessFlag(AccessFlag.PUBLIC);
        constructor.addAccessFlag(AccessFlag.CONSTRUCTOR);

        DexInstruction instruction = constructor.parseInstruction("invoke-direct {p0}, Landroid/app/Activity;-><init>()V");
        MethodKey key = instruction.getKeyAsMethod();
        key = key.changeDeclaring(dexClass.getSuperClassKey());
        instruction.setKey(key);
        instruction.createNextFromSmali("return-void");
    }

    private static void create_onCreate(DexClass dexClass, int contentViewResourceId){

        MethodKey superMethodKey = MethodKey.parse("Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V");

        MethodKey methodKey = superMethodKey.changeDeclaring(dexClass.getKey());

        DexMethod onCreate = dexClass.getOrCreateVirtualMethod(methodKey);

        onCreate.addAccessFlag(AccessFlag.PUBLIC);

        onCreate.setParameterRegistersCount(1 + superMethodKey.getParameterRegistersCount());
        onCreate.setLocalRegistersCount(1);

        DexInstruction invokeSuper = onCreate.addInstruction(Opcode.INVOKE_SUPER);
        invokeSuper.setKey(superMethodKey);

        invokeSuper.setRegistersCount(2);
        invokeSuper.setRegister(0, 1); // p0 (this)
        invokeSuper.setRegister(1, 2); // p1

        DexInstruction constInstruction = onCreate.addInstruction(Opcode.CONST);
        constInstruction.setRegister(0);
        constInstruction.setAsInteger(contentViewResourceId);

        MethodKey setContentViewKey = MethodKey.parse("Landroid/app/Activity;->setContentView(I)V");
        setContentViewKey = setContentViewKey.changeDeclaring(dexClass.getKey());

        DexInstruction invokeVirtual = onCreate.addInstruction(Opcode.INVOKE_VIRTUAL);
        invokeVirtual.setKey(setContentViewKey);

        invokeVirtual.setRegistersCount(2);
        invokeVirtual.setRegister(0, 1); // p0 (this)
        invokeVirtual.setRegister(1, constInstruction.getRegister()); // v0

        onCreate.addInstruction(Opcode.RETURN_VOID);
    }
}
