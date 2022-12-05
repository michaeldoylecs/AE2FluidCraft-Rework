package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class DualityInterfaceTransformer extends FCClassTransformer.ClassMapper {

    public static final DualityInterfaceTransformer INSTANCE = new DualityInterfaceTransformer();

    private DualityInterfaceTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformDualityInterface(Opcodes.ASM5, downstream);
    }

    private static class TransformDualityInterface extends ClassVisitor {

        TransformDualityInterface(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            switch (name) {
                case "pushItemsOut":
                case "pushPattern":
                case "isBusy":
                    return new TransformInvAdaptorCalls(
                            api, super.visitMethod(access, name, desc, signature, exceptions), name);
                default:
                    return super.visitMethod(access, name, desc, signature, exceptions);
            }
        }
    }

    private static class TransformInvAdaptorCalls extends MethodVisitor {
        private final String name;
        private int const_1 = 0;

        private TransformInvAdaptorCalls(int api, MethodVisitor mv, String name) {
            super(api, mv);
            this.name = name;
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            super.visitFieldInsn(opcode, owner, name, desc);
            if (this.name.equals("pushItemsOut") && opcode == Opcodes.PUTFIELD && const_1 == 0) {
                //                update fluid tag
                super.visitVarInsn(Opcodes.ALOAD, 5);
                super.visitVarInsn(Opcodes.ALOAD, 10);
                super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/hooker/CoreModHooks",
                        "updateFluidTag",
                        "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)V",
                        false);
                const_1++;
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == Opcodes.INVOKESTATIC
                    && owner.equals("appeng/util/InventoryAdaptor")
                    && name.equals("getAdaptor")) {
                super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/hooker/CoreModHooks",
                        "wrapInventory",
                        "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraftforge/common/util/ForgeDirection;)Lappeng/util/InventoryAdaptor;",
                        false);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }
    }
}
