package sk.palo.liska;

import org.hibernate.FlushMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author pavol.liska
 * @date 6/10/2018
 */
public class SessionProvider implements HibernateSessionProvider {
    private static final Logger logger = LoggerFactory.getLogger(SessionProvider.class);
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void openSession() {
        Session session = sessionFactory.openSession();
        logger.trace("Opening Hibernate Session {} in SessionProvider", session);
        SessionHolder sessionHolder = new SessionHolder(session);
        TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);
    }

    public void closeSession() {
        SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
        logger.trace("Closing Hibernate Session {} in SessionProvider", sessionHolder.getSession());
        SessionFactoryUtils.closeSession(sessionHolder.getSession());
    }

    @Override
    public Session getCurrentSession() {
        Session session = sessionFactory.getCurrentSession();
        session.setHibernateFlushMode(FlushMode.COMMIT);
        logger.trace("Current transaction: {}", session.getTransaction());

        return session;
    }
}
