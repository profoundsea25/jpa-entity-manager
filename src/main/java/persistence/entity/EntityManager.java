package persistence.entity;

public interface EntityManager {

    <T, K> T find(Class<T> clazz, K id);

    void persist(Object entity);

    void remove(Object entity);

}
