package com.glodblock.github.coremod;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

import com.glodblock.github.coremod.transform.*;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class FCClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] code) {
        Transform tform;
        switch (transformedName) {
            case "appeng.crafting.CraftingTreeNode":
                tform = CraftingTreeNodeTransformer.INSTANCE;
                break;
            case "appeng.me.cluster.implementations.CraftingCPUCluster":
                tform = CraftingCpuTransformer.INSTANCE;
                break;
            case "appeng.helpers.DualityInterface":
                tform = DualityInterfaceTransformer.INSTANCE;
                break;
            case "appeng.container.implementations.ContainerInterfaceTerminal":
                tform = ContainerInterfaceTerminalTransformer.INSTANCE;
                break;
            case "appeng.client.gui.implementations.GuiCraftingCPU":
            case "appeng.client.gui.implementations.GuiCraftConfirm":
            case "net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftConfirm":
                tform = GuiCraftingTransformer.INSTANCE;
                break;
            case "appeng.integration.modules.NEI":
                tform = NEITransfermer.INSTANCE;
                break;
            default:
                return code;
        }
        System.out.println("[FCAE2] Transforming class: " + transformedName);
        return tform.transformClass(code);
    }

    public interface Transform {

        byte[] transformClass(byte[] code);
    }

    public abstract static class ClassMapper implements Transform {

        @Override
        public byte[] transformClass(byte[] code) {
            ClassNode classNode = new ClassNode();
            ClassReader reader = new ClassReader(code);
            reader.accept(classNode, 0);
            ClassWriter writer = new ClassWriter(reader, getWriteFlags());
            classNode.accept(getClassMapper(writer, classNode));
            return writer.toByteArray();
        }

        protected int getWriteFlags() {
            return COMPUTE_FRAMES | COMPUTE_MAXS;
        }

        protected abstract ClassVisitor getClassMapper(ClassVisitor downstream, ClassNode classNode);
    }
}
