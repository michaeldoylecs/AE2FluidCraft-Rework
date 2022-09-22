package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.ACC_SUPER;


public class NEITransfermer extends FCClassTransformer.ClassMapper{

    public static final NEITransfermer INSTANCE = new NEITransfermer();

    private NEITransfermer(){}

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new NEITransfermer.TransformNEI(Opcodes.ASM5, downstream);
    }
    private static class TransformNEI extends ClassVisitor {

        TransformNEI(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if ("getStackUnderMouse".equals(name)) {
                return new NEITransfermer.TransformUnderMouse(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class TransformUnderMouse extends MethodVisitor {

        TransformUnderMouse(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == Opcodes.INVOKEINTERFACE) {
//                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
//                    "com/glodblock/github/coremod/hooker/CoreModHooks",
//                    "getStackUnderMouse",
//                    "(Lnet/minecraft/client/gui/inventory/GuiContainer;II)Lnet/minecraft/item/ItemStack;",
//                    false);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
            else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }

    }
}
