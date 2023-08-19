package com.reandroid.dex.instruction;

import com.reandroid.dex.item.DexContainerItem;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class Instruction extends DexContainerItem implements SmaliFormat {
    private final Opcode<?> opcode;
    Instruction(int childesCount, Opcode<?> opcode) {
        super(childesCount);
        this.opcode = opcode;
    }
    Instruction(Opcode<?> opcode) {
        this(1, opcode);
    }
    public Opcode<?> getOpcode() {
        return opcode;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {

    }
    @Override
    public String toString() {
        return String.valueOf(getOpcode());
    }
}
