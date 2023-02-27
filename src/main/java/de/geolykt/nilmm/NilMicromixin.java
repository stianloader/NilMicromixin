package de.geolykt.nilmm;

import java.io.IOException;
import java.io.InputStream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.geolykt.micromixin.BytecodeProvider;
import de.geolykt.micromixin.MixinConfig;
import de.geolykt.micromixin.MixinConfig.InvalidMixinConfigException;
import de.geolykt.micromixin.MixinTransformer;
import de.geolykt.micromixin.internal.util.Objects;
import de.geolykt.micromixin.supertypes.ASMClassWrapperProvider;
import de.geolykt.micromixin.supertypes.ClassWrapperPool;

import nilloader.api.ClassTransformer;
import nilloader.api.NilLogger;
import nilloader.api.lib.asm.ClassReader;
import nilloader.api.lib.asm.ClassWriter;
import nilloader.api.lib.asm.tree.ClassNode;

public class NilMicromixin implements Runnable {
    @NotNull
    private static final BytecodeProvider<ClassLoader> BYTECODE_PROVIDER = new BytecodeProvider<ClassLoader>() {
        @Override
        public org.objectweb.asm.tree.@NotNull ClassNode getClassNode(ClassLoader modularityAttachment, @NotNull String internalName) throws ClassNotFoundException {
            try {
                InputStream in = modularityAttachment.getResourceAsStream(internalName.replace('.', '/') + ".class");
                if (in == null) {
                    throw new ClassNotFoundException("Resource not found in specified classloader (" + modularityAttachment + ")");
                }
                ClassReader reader = new ClassReader(in);
                ClassNode node = new ClassNode();
                reader.accept(node, ClassReader.SKIP_CODE);
                return (org.objectweb.asm.tree.ClassNode) (Object) node; // This cast should be safe (and resolve to a NOP - should the jar be relocated correctly.)
            } catch (IOException e) {
                throw new ClassNotFoundException("Unable to read reasource from the specified classloader (" + modularityAttachment + ")", e);
            }
        }
    };
    private static final ClassLoader LOADER = ClassWrapperPool.class.getClassLoader();
    private static final NilLogger LOGGER = NilLogger.get("NilMicromixin");
    @NotNull
    public static final ClassWrapperPool CLASS_WRAPPER_POOL = new ClassWrapperPool().addProvider(new ASMClassWrapperProvider() {
        @Override
        @Nullable
        public org.objectweb.asm.tree.@Nullable ClassNode getNode(@NotNull String name) {
            try {
                return BYTECODE_PROVIDER.getClassNode(LOADER, name);
            } catch (ClassNotFoundException e) {
                LOGGER.info("Unable to find classnode for class " + name);
                return null;
            }
        }
    });
    public static final MixinTransformer<ClassLoader> TRANSFORMER = new MixinTransformer<>(BYTECODE_PROVIDER, CLASS_WRAPPER_POOL);

    @Override
    public void run() {
        ClassTransformer.register(new ClassTransformer() {
            @Override
            public byte[] transform(String className, byte[] originalData) {
                // Nilloader's ASM ClassNode writing is broken beyond repair - so we have to do it ourselves.
                if (!NilMicromixin.TRANSFORMER.isMixinTarget(Objects.requireNonNull(className))) {
                    return originalData;
                }
                ClassReader reader = new ClassReader(originalData);
                ClassNode node = new ClassNode();
                reader.accept(node, 0);
                NilMicromixin.TRANSFORMER.transform((org.objectweb.asm.tree.ClassNode) (Object) node);
                ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES) {
                    @SuppressWarnings("null")
                    @Override
                    protected String getCommonSuperClass(String type1, String type2) {
                        return CLASS_WRAPPER_POOL.getCommonSuperClass(CLASS_WRAPPER_POOL.get(type1), CLASS_WRAPPER_POOL.get(type2)).getName();
                    }
                };
                node.accept(writer);
                return writer.toByteArray();
            }
        });
    }

    public static void addMixin(@NotNull ClassLoader loader, @NotNull String mixinContents) throws InvalidMixinConfigException {
        TRANSFORMER.addMixin(loader, MixinConfig.fromString(mixinContents));
    }
}
