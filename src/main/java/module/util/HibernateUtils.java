package module.util;

import module.dao.UserDao;
import module.dao.UserDaoQuery;
import module.domain.persistentEntities.*;
import module.domain.UserCash;
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



    private static final StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder().applySettings(
            Map.of(
                    "hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect",
                    "hibernate.metadata_builder_contributor","module.util.SqlFunctionsMetadataBuilderContributor",
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
                    .addAnnotatedClass(Like.class)
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (Exception ex) {
            throw new PersistenceConfigurationException(ex);
        }
    }

}
