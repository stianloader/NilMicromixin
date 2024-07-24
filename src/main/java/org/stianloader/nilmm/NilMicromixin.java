package org.stianloader.nilmm;

import java.io.IOException;
import java.io.InputStream;

import org.jetbrains.annotations.NotNull;
import org.stianloader.micromixin.transform.api.BytecodeProvider;
import org.stianloader.micromixin.transform.api.MixinConfig;
import org.stianloader.micromixin.transform.api.MixinConfig.InvalidMixinConfigException;
import org.stianloader.micromixin.transform.api.MixinTransformer;
import org.stianloader.micromixin.transform.api.supertypes.ClassWrapperPool;

import nilloader.NilAgent;
import nilloader.api.lib.asm.ClassReader;
import nilloader.api.lib.asm.tree.ClassNode;

public class NilMicromixin implements Runnable {
    @NotNull
    private static final BytecodeProvider<ClassLoader> BYTECODE_PROVIDER = new BytecodeProvider<ClassLoader>() {
        @Override
        @NotNull
        public org.objectweb.asm.tree.ClassNode getClassNode(ClassLoader modularityAttachment, @NotNull String internalName) throws ClassNotFoundException {
            try {
                InputStream in = modularityAttachment.getResourceAsStream(internalName.replace('.', '/') + ".class");
                if (in == null) {
                    throw new ClassNotFoundException("Resource not found in specified classloader (" + modularityAttachment + ")");
                }
                ClassReader reader = new ClassReader(in);
                ClassNode node = new ClassNode();
                reader.accept(node, 0);
                return (org.objectweb.asm.tree.ClassNode) (Object) node; // This cast should be safe (and resolve to a NOP - should the jar be relocated correctly.)
            } catch (IOException e) {
                throw new ClassNotFoundException("Unable to read reasource from the specified classloader (" + modularityAttachment + ")", e);
            }
        }
    };

    @SuppressWarnings("null")
    @NotNull
    private static final ClassLoader LOADER = ClassWrapperPool.class.getClassLoader();

    @NotNull
    public static final BytecodeProviderClassWrapperProvider CLASS_WRAPPER_PROVIDER = new BytecodeProviderClassWrapperProvider(NilMicromixin.BYTECODE_PROVIDER).addClassloader(NilMicromixin.LOADER);

    @NotNull
    public static final ClassWrapperPool CLASS_WRAPPER_POOL = new ClassWrapperPool().addProvider(NilMicromixin.CLASS_WRAPPER_PROVIDER);

    public static final MixinTransformer<ClassLoader> TRANSFORMER = new MixinTransformer<ClassLoader>(NilMicromixin.BYTECODE_PROVIDER, NilMicromixin.CLASS_WRAPPER_POOL);

    @Override
    public void run() {
        NilAgent.registerTransformer(new MixinASMTransformer(NilMicromixin.TRANSFORMER));
    }

    public static void addMixin(@NotNull ClassLoader loader, @NotNull String mixinContents) throws InvalidMixinConfigException {
        NilMicromixin.TRANSFORMER.addMixin(loader, MixinConfig.fromString(mixinContents));
    }

    static {
        if (!Boolean.getBoolean("org.stianloader.nilmm.stdio")) {
            NilMicromixin.TRANSFORMER.setLogger(new NilMixinLoggingFacade());
        }
    }
}
