package module.dao;

import module.util.exeptions.UserDaoException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static module.util.HibernateUtils.getSessionFactory;

public class Dao {
    private static final Logger logger = LoggerFactory.getLogger(Dao.class);

    protected  <T> void execute(UserDaoQuery query) {
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

    public  <T> void deleteById(T o) {
        execute(session -> session.delete(o));
    }


    public <T> void save(T type) throws UserDaoException {
        execute(session -> session.persist(type));
    }

    public <T> void update(T type) {
        execute(session -> session.update(type));
    }

}
