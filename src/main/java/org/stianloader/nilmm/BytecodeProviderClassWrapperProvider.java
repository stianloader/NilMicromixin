package org.stianloader.nilmm;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.stianloader.micromixin.transform.api.BytecodeProvider;
import org.stianloader.micromixin.transform.api.supertypes.ASMClassWrapperProvider;

public class BytecodeProviderClassWrapperProvider extends ASMClassWrapperProvider {

    @NotNull
    private final BytecodeProvider<ClassLoader> provider;
    @NotNull
    private final List<ClassLoader> loaders = new ArrayList<ClassLoader>();

    public BytecodeProviderClassWrapperProvider(@NotNull BytecodeProvider<ClassLoader> provider) {
        this.provider = provider;
    }

    @Override
    @Nullable
    public ClassNode getNode(@NotNull String name) {
        for (ClassLoader loader : this.loaders) {
            try {
                return this.provider.getClassNode(loader, name);
            } catch (ClassNotFoundException e) {
            }
        }
        return null;
    }

    @NotNull
    @Contract(mutates = "this", pure = false, value = "_ -> this")
    public BytecodeProviderClassWrapperProvider addClassloader(@NotNull ClassLoader loader) {
        this.loaders.add(loader);
        return this;
    }
}
