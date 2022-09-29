package com.glodblock.github.coremod.transform;
import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

public class NEITransfermer extends FCClassTransformer.ClassMapper{

    public static final NEITransfermer INSTANCE = new NEITransfermer();

    private NEITransfermer(){}

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream, ClassNode classNode) {
        for (MethodNode method : classNode.methods){
            if(method.name.equals("getStackUnderMouse")){
                InsnList toInsert = new InsnList();
                toInsert.add(new VarInsnNode(ALOAD, 1));
                toInsert.add(new VarInsnNode(ILOAD, 2));
                toInsert.add(new VarInsnNode(ILOAD, 3));
                toInsert.add(new MethodInsnNode(INVOKESTATIC,  "com/glodblock/github/coremod/hooker/CoreModHooks", "getStackUnderMouse", "(Lnet/minecraft/client/gui/inventory/GuiContainer;II)Lnet/minecraft/item/ItemStack;", false));
                toInsert.add(new InsnNode(ARETURN));
                method.instructions = toInsert;
            }else if(method.name.equals("shouldShowTooltip")){
                InsnList toInsert = new InsnList();
                toInsert.add(new VarInsnNode(ALOAD, 1));
                toInsert.add(new MethodInsnNode(INVOKESTATIC,  "com/glodblock/github/coremod/hooker/CoreModHooks", "shouldShowTooltip", "(Lnet/minecraft/client/gui/inventory/GuiContainer;)Z", false));
                toInsert.add(new InsnNode(IRETURN));
                method.instructions = toInsert;
            }
        }
        return new NEITransfermer.TransformNEI(Opcodes.ASM5, downstream);
    }
    private static class TransformNEI extends ClassVisitor {
        TransformNEI(int api, ClassVisitor cv) {
            super(api, cv);
        }
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }
}
