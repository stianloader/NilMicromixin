package org.stianloader.nilmm;

import org.stianloader.micromixin.api.MixinLoggingFacade;

import nilloader.api.NilLogger;

public class NilMixinLoggingFacade implements MixinLoggingFacade {

    private static final NilLogger LOGGER = NilLogger.get(NilMixinLoggingFacade.class.getName());

    @Override
    public void error(Class<?> clazz, String message, Object... args) {
        Object[] o = new Object[args.length + 1];
        System.arraycopy(args, 0, o, 1, args.length);
        o[0] = clazz;
        LOGGER.error("{}: " + message, o);
    }

    @Override
    public void info(Class<?> clazz, String message, Object... args) {
        Object[] o = new Object[args.length + 1];
        System.arraycopy(args, 0, o, 1, args.length);
        o[0] = clazz;
        LOGGER.info("{}: " + message, o);
    }

    @Override
    public void warn(Class<?> clazz, String message, Object... args) {
        Object[] o = new Object[args.length + 1];
        System.arraycopy(args, 0, o, 1, args.length);
        o[0] = clazz;
        LOGGER.warn("{}: " + message, o);
    }
}
