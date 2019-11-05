package cn.sexycode.util.core.collection;

import cn.sexycode.util.core.object.ObjectUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Various help for handling collections.
 *
 * @author qinzaizhen
 */
public final class CollectionUtils {
    public static final int MINIMUM_INITIAL_CAPACITY = 16;

    public static final float LOAD_FACTOR = 0.75f;

    /**
     * @deprecated use  {@link Collections#EMPTY_LIST} or {@link Collections#emptyList()}  instead
     */
    @Deprecated
    public static final List EMPTY_LIST = Collections.EMPTY_LIST;

    /**
     * @deprecated use {@link Collections#EMPTY_LIST} or {@link Collections#emptyList()}  instead
     */
    @Deprecated
    public static final Collection EMPTY_COLLECTION = Collections.EMPTY_LIST;

    /**
     * @deprecated use {@link Collections#EMPTY_MAP} or {@link Collections#emptyMap()}  instead
     */
    @Deprecated
    public static final Map EMPTY_MAP = Collections.EMPTY_MAP;

    private CollectionUtils() {
    }

    /**
     * Build a properly sized map, especially handling load size and load factor to prevent immediate resizing.
     * <p/>
     * Especially helpful for copy map contents.
     *
     * @param size The size to make the map.
     * @return The sized map.
     */
    public static <K, V> Map<K, V> mapOfSize(int size) {
        return new HashMap<K, V>(determineProperSizing(size), LOAD_FACTOR);
    }

    /**
     * Given a map, determine the proper initial size for a new Map to hold the same number of values.
     * Specifically we want to account for load size and load factor to prevent immediate resizing.
     *
     * @param original The original map
     * @return The proper size.
     */
    public static int determineProperSizing(Map original) {
        return determineProperSizing(original.size());
    }

    /**
     * Given a set, determine the proper initial size for a new set to hold the same number of values.
     * Specifically we want to account for load size and load factor to prevent immediate resizing.
     *
     * @param original The original set
     * @return The proper size.
     */
    public static int determineProperSizing(Set original) {
        return determineProperSizing(original.size());
    }

    /**
     * Determine the proper initial size for a new collection in order for it to hold the given a number of elements.
     * Specifically we want to account for load size and load factor to prevent immediate resizing.
     *
     * @param numberOfElements The number of elements to be stored.
     * @return The proper size.
     */
    public static int determineProperSizing(int numberOfElements) {
        int actual = ((int) (numberOfElements / LOAD_FACTOR)) + 1;
        return Math.max(actual, MINIMUM_INITIAL_CAPACITY);
    }

    /**
     * Create a properly sized {@link ConcurrentHashMap} based on the given expected number of elements.
     *
     * @param expectedNumberOfElements The expected number of elements for the created map
     * @param <K>                      The map key type
     * @param <V>                      The map value type
     * @return The created map.
     */
    public static <K, V> ConcurrentHashMap<K, V> concurrentMap(int expectedNumberOfElements) {
        return concurrentMap(expectedNumberOfElements, LOAD_FACTOR);
    }

    /**
     * Create a properly sized {@link ConcurrentHashMap} based on the given expected number of elements and an
     * explicit load factor
     *
     * @param expectedNumberOfElements The expected number of elements for the created map
     * @param loadFactor               The collection load factor
     * @param <K>                      The map key type
     * @param <V>                      The map value type
     * @return The created map.
     */
    public static <K, V> ConcurrentHashMap<K, V> concurrentMap(int expectedNumberOfElements, float loadFactor) {
        final int size = expectedNumberOfElements + 1 + (int) (expectedNumberOfElements * loadFactor);
        return new ConcurrentHashMap<K, V>(size, loadFactor);
    }

    public static <T> ArrayList<T> arrayList(int anticipatedSize) {
        return new ArrayList<T>(anticipatedSize);
    }

    public static <T> Set<T> makeCopy(Set<T> source) {
        if (source == null) {
            return null;
        }

        final int size = source.size();
        final Set<T> copy = new HashSet<T>(size + 1);
        copy.addAll(source);
        return copy;
    }

    public static boolean isNotEmpty(Collection collection) {
        return !isEmpty(collection);
    }

    public static boolean isNotEmpty(Map map) {
        return !isEmpty(map);
    }

    public static boolean isEmpty(Object[] objects) {
        return objects == null || objects.length == 0;
    }

    public static <X, Y> Map<X, Y> makeCopy(Map<X, Y> map) {
        final Map<X, Y> copy = mapOfSize(map.size() + 1);
        copy.putAll(map);
        return copy;
    }

    /**
     * Return {@code true} if the supplied Collection is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param collection the Collection to check
     * @return whether the given Collection is empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    /**
     * Return {@code true} if the supplied Map is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param map the Map to check
     * @return whether the given Map is empty
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    /**
     * Convert the supplied array into a List. A primitive array gets converted
     * into a List of the appropriate wrapper type.
     * <p><b>NOTE:</b> Generally prefer the standard {@link Arrays#asList} method.
     * This {@code arrayToList} method is just meant to deal with an incoming Object
     * value that might be an {@code Object[]} or a primitive array at runtime.
     * <p>A {@code null} source value will be converted to an empty List.
     *
     * @param source the (potentially primitive) array
     * @return the converted List result
     * @see ObjectUtils#toObjectArray(Object)
     * @see Arrays#asList(Object[])
     */
    @SuppressWarnings("rawtypes")
    public static List arrayToList(Object source) {
        return Arrays.asList(ObjectUtils.toObjectArray(source));
    }

    /**
     * Merge the given array into the given Collection.
     *
     * @param array      the array to merge (may be {@code null})
     * @param collection the target Collection to merge the array into
     */
    @SuppressWarnings("unchecked")
    public static <E> void mergeArrayIntoCollection(Object array, Collection<E> collection) {
        Object[] arr = ObjectUtils.toObjectArray(array);
        for (Object elem : arr) {
            collection.add((E) elem);
        }
    }

    /**
     * Merge the given Properties instance into the given Map,
     * copying all properties (key-value pairs) over.
     * <p>Uses {@code Properties.propertyNames()} to even catch
     * default properties linked into the original Properties instance.
     *
     * @param props the Properties instance to merge (may be {@code null})
     * @param map   the target Map to merge the properties into
     */
    @SuppressWarnings("unchecked")
    public static <K, V> void mergePropertiesIntoMap(Properties props, Map<K, V> map) {
        if (props != null) {
            for (Enumeration<?> en = props.propertyNames(); en.hasMoreElements(); ) {
                String key = (String) en.nextElement();
                Object value = props.get(key);
                if (value == null) {
                    // Allow for defaults fallback or potentially overridden accessor...
                    value = props.getProperty(key);
                }
                map.put((K) key, (V) value);
            }
        }
    }

    /**
     * Check whether the given Iterator contains the given element.
     *
     * @param iterator the Iterator to check
     * @param element  the element to look for
     * @return {@code true} if found, {@code false} otherwise
     */
    public static boolean contains(Iterator<?> iterator, Object element) {
        if (iterator != null) {
            while (iterator.hasNext()) {
                Object candidate = iterator.next();
                if (ObjectUtils.nullSafeEquals(candidate, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether the given Enumeration contains the given element.
     *
     * @param enumeration the Enumeration to check
     * @param element     the element to look for
     * @return {@code true} if found, {@code false} otherwise
     */
    public static boolean contains(Enumeration<?> enumeration, Object element) {
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                Object candidate = enumeration.nextElement();
                if (ObjectUtils.nullSafeEquals(candidate, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether the given Collection contains the given element instance.
     * <p>Enforces the given instance to be present, rather than returning
     * {@code true} for an equal element as well.
     *
     * @param collection the Collection to check
     * @param element    the element to look for
     * @return {@code true} if found, {@code false} otherwise
     */
    public static boolean containsInstance(Collection<?> collection, Object element) {
        if (collection != null) {
            for (Object candidate : collection) {
                if (candidate == element) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return {@code true} if any element in '{@code candidates}' is
     * contained in '{@code source}'; otherwise returns {@code false}.
     *
     * @param source     the source Collection
     * @param candidates the candidates to search for
     * @return whether any of the candidates has been found
     */
    public static boolean containsAny(Collection<?> source, Collection<?> candidates) {
        if (isEmpty(source) || isEmpty(candidates)) {
            return false;
        }
        for (Object candidate : candidates) {
            if (source.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the first element in '{@code candidates}' that is contained in
     * '{@code source}'. If no element in '{@code candidates}' is present in
     * '{@code source}' returns {@code null}. Iteration order is
     * {@link Collection} implementation specific.
     *
     * @param source     the source Collection
     * @param candidates the candidates to search for
     * @return the first present object, or {@code null} if not found
     */
    @SuppressWarnings("unchecked")

    public static <E> E findFirstMatch(Collection<?> source, Collection<E> candidates) {
        if (isEmpty(source) || isEmpty(candidates)) {
            return null;
        }
        for (Object candidate : candidates) {
            if (source.contains(candidate)) {
                return (E) candidate;
            }
        }
        return null;
    }

    /**
     * Find a single value of the given type in the given Collection.
     *
     * @param collection the Collection to search
     * @param type       the type to look for
     * @return a value of the given type found if there is a clear match,
     * or {@code null} if none or more than one such value found
     */
    @SuppressWarnings("unchecked")

    public static <T> T findValueOfType(Collection<?> collection, Class<T> type) {
        if (isEmpty(collection)) {
            return null;
        }
        T value = null;
        for (Object element : collection) {
            if (type == null || type.isInstance(element)) {
                if (value != null) {
                    // More than one value found... no clear single value.
                    return null;
                }
                value = (T) element;
            }
        }
        return value;
    }

    /**
     * Find a single value of one of the given types in the given Collection:
     * searching the Collection for a value of the first type, then
     * searching for a value of the second type, etc.
     *
     * @param collection the collection to search
     * @param types      the types to look for, in prioritized order
     * @return a value of one of the given types found if there is a clear match,
     * or {@code null} if none or more than one such value found
     */

    public static Object findValueOfType(Collection<?> collection, Class<?>[] types) {
        if (isEmpty(collection) || ObjectUtils.isEmpty(types)) {
            return null;
        }
        for (Class<?> type : types) {
            Object value = findValueOfType(collection, type);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Determine whether the given Collection only contains a single unique object.
     *
     * @param collection the Collection to check
     * @return {@code true} if the collection contains a single reference or
     * multiple references to the same instance, {@code false} otherwise
     */
    public static boolean hasUniqueObject(Collection<?> collection) {
        if (isEmpty(collection)) {
            return false;
        }
        boolean hasCandidate = false;
        Object candidate = null;
        for (Object elem : collection) {
            if (!hasCandidate) {
                hasCandidate = true;
                candidate = elem;
            } else if (candidate != elem) {
                return false;
            }
        }
        return true;
    }

    /**
     * Find the common element type of the given Collection, if any.
     *
     * @param collection the Collection to check
     * @return the common element type, or {@code null} if no clear
     * common type has been found (or the collection was empty)
     */

    public static Class<?> findCommonElementType(Collection<?> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        Class<?> candidate = null;
        for (Object val : collection) {
            if (val != null) {
                if (candidate == null) {
                    candidate = val.getClass();
                } else if (candidate != val.getClass()) {
                    return null;
                }
            }
        }
        return candidate;
    }

    /**
     * Retrieve the last element of the given Set, using {@link SortedSet#last()}
     * or otherwise iterating over all elements (assuming a linked set).
     *
     * @param set the Set to check (may be {@code null} or empty)
     * @return the last element, or {@code null} if none
     * @see SortedSet
     * @see LinkedHashMap#keySet()
     * @see java.util.LinkedHashSet
     * @since 5.0.3
     */

    public static <T> T lastElement(Set<T> set) {
        if (isEmpty(set)) {
            return null;
        }
        if (set instanceof SortedSet) {
            return ((SortedSet<T>) set).last();
        }

        // Full iteration necessary...
        Iterator<T> it = set.iterator();
        T last = null;
        while (it.hasNext()) {
            last = it.next();
        }
        return last;
    }

    /**
     * Retrieve the last element of the given List, accessing the highest index.
     *
     * @param list the List to check (may be {@code null} or empty)
     * @return the last element, or {@code null} if none
     * @since 5.0.3
     */

    public static <T> T lastElement(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    /**
     * Marshal the elements from the given enumeration into an array of the given type.
     * Enumeration elements must be assignable to the type of the given array. The array
     * returned will be a different instance than the array given.
     */
    public static <A, E extends A> A[] toArray(Enumeration<E> enumeration, A[] array) {
        ArrayList<A> elements = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            elements.add(enumeration.nextElement());
        }
        return elements.toArray(array);
    }

    /**
     * Adapt an enumeration to an iterator.
     *
     * @param enumeration the enumeration
     * @return the iterator
     */
    public static <E> Iterator<E> toIterator(Enumeration<E> enumeration) {
        return new EnumerationIterator<>(enumeration);
    }

    /**
     * Iterator wrapping an Enumeration.
     */
    private static class EnumerationIterator<E> implements Iterator<E> {

        private final Enumeration<E> enumeration;

        public EnumerationIterator(Enumeration<E> enumeration) {
            this.enumeration = enumeration;
        }

        @Override
        public boolean hasNext() {
            return this.enumeration.hasMoreElements();
        }

        @Override
        public E next() {
            return this.enumeration.nextElement();
        }

        @Override
        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported");
        }
    }

}
