package sk.palo.liska;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * POS servis specificky pre SHELL US. Spusta proces, ktory v definovanych intervaloch vola update POS config pre vsetky POS-ky.
 *
 * @author boris.brinza
 * @author denis.kudelas
 */
@Service
public class ServiceImpl implements IService {

    private HibernateSessionProvider sessionProvider;
    private TransactionTemplate txTemplate;

    @Autowired
    public ServiceImpl(TransactionTemplate txTemplate, HibernateSessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
        this.txTemplate = txTemplate;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_UNCOMMITTED)
    public Long saveTable(MyTable request) {
        return (Long) sessionProvider.getCurrentSession().save(request);
    }

    @Override
    @Transactional
    public void updateTable(MyTable request) {
        sessionProvider.getCurrentSession().saveOrUpdate(request);
    }

    @Override
    @Transactional
    public MyTable getTable(Long id) {
        return (MyTable) sessionProvider.getCurrentSession().get(MyTable.class, id);
    }

    @Override
    @Transactional
    public Long getCount() {
        return ((Integer) sessionProvider.getCurrentSession().createNativeQuery("select count(*) from MyTable").uniqueResult()).longValue();
    }

    @Override
    public Long getCountWithTxTemplate() {
        return txTemplate.execute(status -> ((Integer) sessionProvider.getCurrentSession().createNativeQuery("select count(*) from MyTable").uniqueResult()).longValue());
    }
}