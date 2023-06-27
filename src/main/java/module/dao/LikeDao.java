package module.dao;

import module.domain.persistentEntities.Like;
import module.util.exeptions.UserDaoException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.open.cdi.annotations.DIBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static module.util.HibernateUtils.getSessionFactory;

@DIBean
public class LikeDao extends Dao {

    private static final Logger logger = LoggerFactory.getLogger(LikeDao.class);

    public List<Like> getLikesListOfLikedUser(Long id) {
        System.out.println(id);
        logger.info("user is is   "+ id);
        List<Like> likes;
        try (Session session = getSessionFactory().openSession()) {
            session.beginTransaction();
            Query query = session.createQuery("from Like l where l.like_to_id = :user_id");
            query.setParameter("user_id", id );
            likes = (List<Like>) query.list();
            session.getTransaction().commit();
        } catch (UserDaoException e) {
            logger.info(e.toString());
            throw new UserDaoException(e);
        }
        return likes;
    }

}
