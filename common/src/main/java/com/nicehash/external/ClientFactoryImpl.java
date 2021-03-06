package com.nicehash.external;

import java.lang.reflect.Constructor;

import com.nicehash.external.spi.ClientImplementation;
import com.nicehash.external.spi.Options;
import com.nicehash.utils.options.OptionMap;

/**
 * @author Ales Justin
 */
class ClientFactoryImpl implements ClientFactory {

    private final OptionMap options;

    public ClientFactoryImpl(OptionMap options) {
        this.options = options;
    }

    @Override
    public <T> T getClient(Class<T> clientClass) {
        return getClient(clientClass, options);
    }

    @Override
    public <T> T getClient(Class<T> clientClass, String token) {
        OptionMap.Builder builder = OptionMap.builder();
        builder.addAll(options);
        builder.set(Options.TOKEN, token);
        OptionMap options = builder.getMap();
        return getClient(clientClass, options);
    }

    @Override
    public <T> T getClient(Class<T> clientClass, String key, String secret) {
        OptionMap.Builder builder = OptionMap.builder();
        builder.addAll(options);
        builder.set(Options.KEY, key);
        builder.set(Options.SECRET, secret);
        OptionMap options = builder.getMap();
        return getClient(clientClass, options);
    }

    @Override
    public <T> T getClient(Class<T> clientClass, OptionMap original) {
        ClientImplementation ci = clientClass.getAnnotation(ClientImplementation.class);
        if (ci == null) {
            throw new IllegalArgumentException(String.format("Client class %s is missing @ClientImplementation annotation.", clientClass.getName()));
        }
        Class<?> implClass = ci.value();
        if (clientClass.isAssignableFrom(implClass) == false) {
            throw new IllegalArgumentException(String.format("%S is not assignable from client class %s", implClass, clientClass));
        }

        OptionMap.Builder builder = OptionMap.builder();
        builder.addAll(options);
        builder.addAll(original);
        OptionMap current = builder.getMap();

        try {
            @SuppressWarnings("unchecked")
            Constructor<T> ctor = (Constructor<T>) implClass.getConstructor(OptionMap.class);
            return ctor.newInstance(current);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
