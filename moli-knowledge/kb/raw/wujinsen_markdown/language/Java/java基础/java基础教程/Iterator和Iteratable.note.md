Iterable接⼜ ： public interface Iterable<T> { Iterator<T> iterator(); //JDK1.8提供 default void forEach(Consumer<? super T> action) {

Objects.requireNonNull(action); for (Tt : this) {

action.accept(t); }

} //JDK1.8提供 default Spliterator<T> spliterator() {

return Spliterators.spliteratorUnknownSize(iterator(), 0); }

} Iterator接⼜： public interface Iterator<E> {

boolean hasNext(); Enext(); default void remove() {

throw new UnsupportedOperationException("remove");

} //JDK1.8提供 default void forEachRemaining(Consumer<? super E> action) {

Objects.requireNonNull(action); while (hasNext())

action.accept(next()); }

}

