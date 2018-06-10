package sk.palo.liska;

import org.hibernate.Session;

/**
 * @author pavol.liska
 * @date 6/10/2018
 */
public interface HibernateSessionProvider {
    /**
     * Method provides Hibernate session - http://docs.jboss.org/hibernate/orm/4.2/javadocs/index.html?org/hibernate/Session.html
     */
    Session getCurrentSession();

    /**
     * Opening and storing the session to the Spring managed session context - thread local variable
     */
    void openSession();

    /**
     * Closing current session stored in Spring managed SpringSessionContext object
     */
    void closeSession();
}
