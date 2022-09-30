package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
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
            if(method.name.equals("getStackUnderMouse") || method.name.equals("shouldShowTooltip")){
                AbstractInsnNode targetNode = null;
                for (AbstractInsnNode instruction : method.instructions.toArray())
                {
                    if (instruction.getOpcode() == ALOAD)
                    {
                        targetNode = instruction;
                        break;
                    }
                }
                InsnList toInsert = new InsnList();
                toInsert.add(new VarInsnNode(ALOAD, 1));
                toInsert.add(new TypeInsnNode(INSTANCEOF, "com/glodblock/github/client/gui/GuiFluidCraftConfirm"));
                LabelNode label0 = new LabelNode();
                toInsert.add(new JumpInsnNode(IFEQ, label0));
                toInsert.add(new VarInsnNode(ALOAD, 1));
                toInsert.add(new TypeInsnNode(CHECKCAST, "com/glodblock/github/client/gui/GuiFluidCraftConfirm"));
                toInsert.add(new MethodInsnNode(INVOKEVIRTUAL,  "com/glodblock/github/client/gui/GuiFluidCraftConfirm", "getHoveredStack", "()Lnet/minecraft/item/ItemStack;", false));
                if(method.name.equals("getStackUnderMouse")){
                    toInsert.add(new InsnNode(ARETURN));
                }else{
                    LabelNode label1 = new LabelNode();
                    toInsert.add(new JumpInsnNode(IFNONNULL, label1));
                    toInsert.add(new InsnNode(ICONST_1));
                    LabelNode label2 = new LabelNode();
                    toInsert.add(new JumpInsnNode(GOTO, label2));
                    toInsert.add(label1);
                    toInsert.add(new InsnNode(ICONST_0));
                    toInsert.add(label2);
                    toInsert.add(new InsnNode(IRETURN));
                }
                toInsert.add(label0);
                method.instructions.insertBefore(targetNode,toInsert);
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
