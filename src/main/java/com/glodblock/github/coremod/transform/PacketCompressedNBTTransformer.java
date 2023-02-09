package com.glodblock.github.coremod.transform;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.glodblock.github.coremod.FCClassTransformer;

public class PacketCompressedNBTTransformer extends FCClassTransformer.ClassMapper {

    public static final PacketCompressedNBTTransformer INSTANCE = new PacketCompressedNBTTransformer();

    private PacketCompressedNBTTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformPacketCompressedNBT(Opcodes.ASM5, downstream);
    }

    private static class TransformPacketCompressedNBT extends ClassVisitor {

        TransformPacketCompressedNBT(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("clientPacketData")) {
                return new TransformClientPacketData(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);

        }
    }

    private static class TransformClientPacketData extends MethodVisitor {

        TransformClientPacketData(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            super.visitVarInsn(Opcodes.ALOAD, 0);
            super.visitFieldInsn(
                    Opcodes.GETFIELD,
                    "appeng/core/sync/packets/PacketCompressedNBT",
                    "in",
                    "Lnet/minecraft/nbt/NBTTagCompound;");
            super.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "com/glodblock/github/coremod/hooker/CoreModHooks",
                    "clientPacketData",
                    "(Lnet/minecraft/nbt/NBTTagCompound;)V",
                    false);
            super.visitInsn(opcode);
        }
    }
}
