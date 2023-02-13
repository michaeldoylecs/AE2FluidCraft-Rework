package com.glodblock.github.coremod.transform;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.glodblock.github.coremod.FCClassTransformer;

public class GuiRenamerTransformer extends FCClassTransformer.ClassMapper {

    public static final GuiRenamerTransformer INSTANCE = new GuiRenamerTransformer();

    private GuiRenamerTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new GuiRenamerTransformer.TransformGuiRenamer(Opcodes.ASM5, downstream);
    }

    private static class TransformGuiRenamer extends ClassVisitor {

        TransformGuiRenamer(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("keyTyped")) {
                return new GuiRenamerTransformer.TransformKeyTyped(
                        api,
                        super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    private static class TransformKeyTyped extends MethodVisitor {

        TransformKeyTyped(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (owner.equals("net/minecraft/client/entity/EntityClientPlayerMP") && name.equals("closeScreen")) {
                super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/hooker/CoreModHooksClient",
                        "reopenInterfaceTerminal",
                        "()V",
                        false);
            }
        }

    }
}
