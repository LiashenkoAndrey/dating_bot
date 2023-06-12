package module.util;

import module.dao.UserDao;
import module.dao.UserDaoQuery;
import module.domain.persistentEntities.User;
import module.domain.UserCash;
import module.domain.persistentEntities.UserFilter;
import module.domain.persistentEntities.UserLocation;
import module.domain.persistentEntities.UserPhoto;
import module.util.exeptions.PersistenceConfigurationException;
import module.util.exeptions.UserDaoException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HibernateUtils {

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    public static final Map<Long, User> unregisteredUserMap = new HashMap<>();

    public static final Map<Long, UserCash> userCashMap = new HashMap<>();

    public static void execute(UserDaoQuery query) {
        try {
            Session session = getSessionFactory().openSession();
            session.beginTransaction();

            query.execute(session);

            session.getTransaction().commit();
            session.close();
        } catch (HibernateException e) {
            logger.info(e.toString());
            throw new UserDaoException(e);
        }
    }

    private static final StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder().applySettings(
            Map.of(
                    "hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect",
                    "hibernate.connection.url", "jdbc:postgresql://localhost:5432/dating_bot",
                    "hibernate.connection.password", "admin",
                    "hibernate.connection.username", "postgres",
                    "hibernate.connection.driver_class", "org.postgresql.Driver",
                    "hibernate.format_sql", "true",
                    "hibernate.show_sql", "true",
                    "use_sql_comments", "true"
            ));



    public static SessionFactory getSessionFactory() {
        try {
            ServiceRegistry registry1 = registryBuilder.build();
            return new MetadataSources(registry1)
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(UserLocation.class)
                    .addAnnotatedClass(UserPhoto.class)
                    .addAnnotatedClass(UserFilter.class)
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (Exception ex) {
            throw new PersistenceConfigurationException(ex);
        }
    }

}
