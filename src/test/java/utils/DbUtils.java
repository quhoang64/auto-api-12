package utils;

import model.dao.user.AddressDao;
import model.dao.user.UserDao;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class DbUtils {
    private static SessionFactory sessionFactory;
    public static SessionFactory getSessionFactory(){
        final StandardServiceRegistry registry =
                new StandardServiceRegistryBuilder()
                        .build();
        SessionFactory sessionFactory = null;
        try {
            if(sessionFactory == null){
                sessionFactory =
                        new MetadataSources(registry)
                                .addAnnotatedClass(UserDao.class)
                                .addAnnotatedClass(AddressDao.class)
                                .buildMetadata()
                                .buildSessionFactory();
            }

        }
        catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we
            // had trouble building the SessionFactory so destroy it manually.
            StandardServiceRegistryBuilder.destroy(registry);
        }
        return sessionFactory;
    }

    public static UserDao getUserFromDb(String userId){
        SessionFactory sessionFactory = DbUtils.getSessionFactory();

        AtomicReference<UserDao> userDaoAtomic = new AtomicReference<>();
        sessionFactory.inTransaction(session -> {
            userDaoAtomic.set(session.createSelectionQuery("from UserDao c JOIN FETCH c.addresses WHERE c.id = :id", UserDao.class)
                    .setParameter("id", UUID.fromString(userId))
                    .getSingleResult());
        });
        return userDaoAtomic.get();
    }
}
