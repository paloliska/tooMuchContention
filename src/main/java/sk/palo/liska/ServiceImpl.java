package sk.palo.liska;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * POS servis specificky pre SHELL US. Spusta proces, ktory v definovanych intervaloch vola update POS config pre vsetky POS-ky.
 *
 * @author boris.brinza
 * @author denis.kudelas
 */
@Service
public class ServiceImpl {

    @Autowired
    private HibernateSessionProvider sessionProvider;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_UNCOMMITTED)
    public Long saveTable(MyTable request) {
        return (Long) sessionProvider.getCurrentSession().save(request);
    }

    @Transactional
    public void updateTable(MyTable request) {
        sessionProvider.getCurrentSession().saveOrUpdate(request);
    }

    @Transactional
    public MyTable getTable(Long id) {
        return (MyTable) sessionProvider.getCurrentSession().get(MyTable.class, id);
    }

    @Transactional
    public Long getCount() {
        return ((Integer) sessionProvider.getCurrentSession().createSQLQuery("select count(*) from MyTable").uniqueResult()).longValue();
    }
}