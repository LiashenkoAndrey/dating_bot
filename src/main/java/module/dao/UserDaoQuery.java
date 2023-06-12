package module.dao;

import org.hibernate.Session;

@FunctionalInterface
public interface UserDaoQuery {

    void execute(Session session);
}
