package org.jdriven;

public class Mappers {

    /**
     * Utility method to dynamically instantiate an implementation of a mapper interface.
     *
     * <p>This method is needed because annotation processors generate implementation
     * classes at compile-time (e.g., {@code CarMapperImpl} for {@code CarMapper}),
     * but those implementations are not directly available in the source code.
     * Instead of manually instantiating the implementation (which requires knowing
     * the generated class name), this method allows usage through the interface itself.
     *
     * <p>It constructs the expected implementation class name dynamically, loads
     * it via reflection, and instantiates it. If the implementation class cannot
     * be found or instantiated, it throws a {@link RuntimeException}.
     *
     * @param mapperInterface The mapper interface class (e.g., {@code CarMapper.class}).
     * @param <T> The type of the mapper interface.
     * @return An instance of the generated implementation.
     * @throws RuntimeException If the implementation class cannot be instantiated.
     */
    public static <T> T getMapper(Class<T> mapperInterface) {
        try {
            String implClassName = mapperInterface.getPackageName() + "." + mapperInterface.getSimpleName() + "Impl";
            Class<?> implClass = Class.forName(implClassName);
            return mapperInterface.cast(implClass.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate mapper for: " + mapperInterface.getName(), e);
        }
    }
}

