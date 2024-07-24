package org.stianloader.nilmm;

import org.stianloader.micromixin.transform.api.MixinTransformer;

import nilloader.api.ClassTransformer;
import nilloader.api.lib.asm.ClassReader;
import nilloader.api.lib.asm.ClassWriter;
import nilloader.api.lib.asm.tree.ClassNode;

public class MixinASMTransformer implements ClassTransformer {
    private final MixinTransformer<ClassLoader> transformer;

    public MixinASMTransformer(MixinTransformer<ClassLoader> transformer) {
        this.transformer = transformer;
    }

    /*
    @SuppressWarnings("null")
    @Override
    public boolean transform(ClassLoader loader, ClassNode node) {
        this.transformer.transform((org.objectweb.asm.tree.ClassNode) (Object) node);
        return true;
    }

    @Override
    public boolean canTransform(ClassLoader loader, String className) {
        assert className != null;
        return this.transformer.isMixinTarget(className);
    }*/

    @Override
    public byte[] transform(String className, byte[] originalData) {
        assert className != null;
        // Nilloader's ASM ClassNode writing is broken beyond repair - so we have to do it ourselves.
        if (!this.transformer.isMixinTarget(className)) {
            return originalData;
        }
        try {
            ClassReader reader = new ClassReader(originalData);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);
            this.transformer.transform((org.objectweb.asm.tree.ClassNode) (Object) node);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES) {
                @SuppressWarnings("null")
                @Override
                protected String getCommonSuperClass(String type1, String type2) {
                    return MixinASMTransformer.this.transformer.getPool().getCommonSuperClass(MixinASMTransformer.this.transformer.getPool().get(type1), MixinASMTransformer.this.transformer.getPool().get(type2)).getName();
                }
            };
            node.accept(writer);
            return writer.toByteArray();
        } catch (Throwable t) {
            if (!Boolean.getBoolean("org.stianloader.nilmm.stdio")) {
                this.transformer.getLogger().error(MixinASMTransformer.class, "Unable to transform class", t);
            } else {
                t.printStackTrace();
                t.printStackTrace(System.out);
            }
            if (t instanceof Error) {
                throw (Error) t;
            }
            throw new RuntimeException("Unable to transform class", t);
        }
    }
}
