package com.reandroid.dex;

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexFile;
import com.reandroid.dex.model.DexInstruction;
import com.reandroid.dex.model.DexMethod;

public class SampleDexFileCreator {

    public static DexFile createApplicationClass(String appSourceName, String activitySourceName, int contentViewResourceId) {
        DexFile dexFile = DexFile.createDefault();
        createApplicationClass(dexFile, appSourceName);
        createActivityClass(dexFile, activitySourceName, contentViewResourceId);
        dexFile.clearUnused();
        dexFile.clearEmptySections();
        dexFile.refreshFull();
        return dexFile;
    }
    private static void createApplicationClass(DexFile dexFile, String classSourceName){
        if(classSourceName == null){
            return;
        }
        String binClassName = DexUtils.toBinaryName(classSourceName);

        DexClass dexClass = dexFile.getOrCreateClass(binClassName);
        dexClass.addAccessFlag(AccessFlag.PUBLIC);
        dexClass.setSuperClass(TypeKey.create("Landroid/app/Application;"));

        createConstructor(dexClass);
    }
    private static void createActivityClass(DexFile dexFile, String activitySourceName, int contentViewResourceId){
        String binClassName = DexUtils.toBinaryName(activitySourceName);

        DexClass dexClass = dexFile.getOrCreateClass(binClassName);
        dexClass.addAccessFlag(AccessFlag.PUBLIC);
        dexClass.setSuperClass(TypeKey.create("Landroid/app/Activity;"));

        createConstructor(dexClass);
        create_onCreate(dexClass, contentViewResourceId);
    }

    private static void createConstructor(DexClass dexClass){
        MethodKey methodKey = MethodKey.CONSTRUCTOR.changeDeclaring(dexClass.getKey());
        DexMethod constructor = dexClass.getOrCreateDirectMethod(methodKey);

        constructor.addAccessFlag(AccessFlag.PUBLIC);
        constructor.addAccessFlag(AccessFlag.CONSTRUCTOR);

        constructor.setParameterRegistersCount(1);
        constructor.setLocalRegistersCount(0);

        DexInstruction invokeSuper = constructor.addInstruction(Opcode.INVOKE_DIRECT);

        TypeKey declaring = dexClass.getSuperClassKey();

        MethodKey superMethodKey = methodKey.changeDeclaring(declaring);
        invokeSuper.setKey(superMethodKey);

        invokeSuper.setRegistersCount(1);
        invokeSuper.setRegister(0); // p0 (this)

        constructor.addInstruction(Opcode.RETURN_VOID);
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
