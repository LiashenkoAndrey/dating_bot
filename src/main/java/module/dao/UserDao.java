package module.dao;

import module.domain.enums.FindBy;
import module.domain.persistentEntities.Like;
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


import java.util.List;
import java.util.Random;


import static module.util.HibernateUtils.getSessionFactory;

@DIBean
public class UserDao extends Dao {

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);


    public User searchRandom(User user) {
        User random = null;
        UserLocation location = user.getLocation();
        try (Session session = getSessionFactory().openSession()) {
            Query query = null;

            Long id = user.getId();

            switch (user.getUserFilter().getFind_by()) {
                case CITY -> query = session.createQuery("from User u where u.location.city = :city and u.id != :id")
                        .setParameter("city", location.getCity());

                case DISTINCT -> query = session.createQuery("from User u where u.location.district = :dist and u.id != :id")
                        .setParameter("dist", location.getDistrict());

                case TOWN -> query =  session.createQuery("from User u where u.location.town = :town and u.id != :id")
                        .setParameter("town", location.getTown());

                case VILLAGE -> query =  session.createQuery("from User u where u.location.village = :vill and u.id != :id")
                        .setParameter("vill", location.getVillage());

                case STATE -> query =  session.createQuery("from User u where u.location.state = :state and u.id != :id")
                        .setParameter("state", location.getState());

                // get user if distance is less or equal location.distance
                // function to get distance from two points √(x₂ - x₁)² + (y₂ - y₁)²
                case DISTANCE ->
                    query =  session.createQuery("from User u where get_distance_from_two_points(:lat, :lon, u.location.latitude, u.location.longitude)  <= :distance and u.id != :id and u.sex != :sex")
                            .setParameter("lat", location.getLatitude())
                            .setParameter("lon", location.getLongitude())
                            .setParameter("distance", user.getUserFilter().getDistance())
                            .setParameter("sex", user.getSex());

                case LOCALITY -> throw new UnsupportedOperationException("find by LOCALITY is not supported");
            }

            query.setParameter("id", id);
            List<User> users = (List<User>) query.list();

            if (!users.isEmpty()) {
                random = users.get(new Random().nextInt(0, users.size())); // get random form string
            }

        } catch (UserDaoException e) {
            logger.info(e.toString());
            throw new UserDaoException(e);
        }

        return random;
    }

    public boolean isRegistered(Long id) {
        try (Session session = getSessionFactory().openSession()) {
            Query query = session.createQuery("select 1 from User u where u.telegram_id = :key");
            query.setParameter("key", id );
            return (query.uniqueResult() != null);
        }
    }

    public User getById(Long id) {
        try (Session session = getSessionFactory().openSession()) {
            return (User) session.createQuery("from User u where u.id = :key")
                    .setParameter("key", id)
                    .getSingleResult();
        } catch (HibernateException e) {
            logger.info(e.toString());
            throw new UserDaoException(e);
        }
    }

    public User getByTelegramId(Long userId) {
        try (Session session = getSessionFactory().openSession()) {
            Query query = session.createQuery("from User u where u.telegram_id = :key");
            query.setParameter("key", userId);
            return (User) query.getSingleResult();
        } catch (HibernateException e) {
            logger.info(e.toString());
            throw new UserDaoException(e);
        }
    }
}
