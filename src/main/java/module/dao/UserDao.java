package module.dao;

import module.domain.persistentEntities.User;
import module.domain.persistentEntities.UserFilter;
import module.domain.persistentEntities.UserLocation;
import module.util.exeptions.UserDaoException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.open.cdi.annotations.DIBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static module.util.HibernateUtils.execute;
import static module.util.HibernateUtils.getSessionFactory;

@DIBean
public class UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    public void save(User user) throws UserDaoException {
        user.setUserFilter(UserFilter.buildDefaultFilter(user));
        execute(
                session -> session.persist(user)
        );
    }


    public boolean isRegistered(Long id) {
        try (Session session = getSessionFactory().openSession()) {
            Query query = session.createQuery("select 1 from User u where u.telegram_id = :key");
            query.setParameter("key", id );
            return (query.uniqueResult() != null);
        }
    }

    public User getByTelegramId(Long userId) {
        try (Session session = getSessionFactory().openSession()) {
            Query query = session.createQuery("from User u where u.telegram_id = :key");
            query.setParameter("key", userId);
            User user =  (User) query.getSingleResult();
            System.out.println(user.getLocation());
            return user;
        } catch (HibernateException e) {
            logger.info(e.toString());
            throw new UserDaoException(e);
        }
    }


    public void update(User user) {
        execute(session -> session.update(user));
    }


    public void saveUserLocationById(UserLocation location, User user) {
        execute(session -> {
            location.setUser(user);
            session.persist(location);
        });
    }

    public void saveUserFilter(UserFilter filter) {
        execute(session -> {
            User user = session.find(User.class, 54L);
            System.out.println(user);
            filter.setUser(user);
            session.persist(filter);
        });
    }




}
