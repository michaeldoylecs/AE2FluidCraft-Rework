package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TitleIOPortTransformer extends FCClassTransformer.ClassMapper {
    public static final TitleIOPortTransformer INSTANCE = new TitleIOPortTransformer();

    private TitleIOPortTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TitleIOPortTransformer.TransformTitleIOPort(Opcodes.ASM5, downstream);
    }

    private static class TransformTitleIOPort extends ClassVisitor {

        TransformTitleIOPort(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("transferContents")) {
                return new TitleIOPortTransformer.TransformTransferContents(
                        api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    private static class TransformTransferContents extends MethodVisitor {

        TransformTransferContents(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == Opcodes.INVOKESTATIC && owner.equals("java/lang/Math") && name.equals("min")) {
                super.visitVarInsn(Opcodes.ALOAD, 6);
                super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/hooker/CoreModHooks",
                        "IOPortMinSpeed",
                        "(JJLappeng/api/storage/StorageChannel;)J",
                        false);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }
    }
}
