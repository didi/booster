package com.didiglobal.booster.kotlinx;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Types {

    private static final Map<Class<?>, String> SIGNATURES = Stream.of(
            new AbstractMap.SimpleEntry<>(boolean.class, "Z"),
            new AbstractMap.SimpleEntry<>(byte.class, "B"),
            new AbstractMap.SimpleEntry<>(char.class, "C"),
            new AbstractMap.SimpleEntry<>(short.class, "S"),
            new AbstractMap.SimpleEntry<>(int.class, "I"),
            new AbstractMap.SimpleEntry<>(float.class, "F"),
            new AbstractMap.SimpleEntry<>(double.class, "D"),
            new AbstractMap.SimpleEntry<>(long.class, "J"),
            new AbstractMap.SimpleEntry<>(void.class, "V")
    ).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

    static String getDescriptor(final Class<?> clazz) {
        final String signature = SIGNATURES.get(clazz);
        if (null != signature) {
            return signature;
        }

        if (clazz.isArray()) {
            return "[" + getDescriptor(clazz.getComponentType());
        }

        return "L" + clazz.getName().replace('.', '/') + ";";
    }

    private Types() {
    }

}
