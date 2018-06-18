package sk.palo.liska;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import org.awaitility.Awaitility;
import org.hibernate.exception.LockAcquisitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.FileSystemUtils;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 * @author pavol.liska
 * @date 6/8/2018
 */
public class ServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(ServiceImplTest.class);

    private static final String DERBY_CONNECTION_URL = "jdbc:derby:derbyDB";
    private static Connection connection;
    public static final int TEST_TIMEOUT = 60;
    private final int nThreads = 16;

    private ApplicationContext context;
    private TransactionTemplate txTemplate;
    private HibernateSessionProvider sessionProvider;
    boolean isInterrupted = false;

    private IService testable;
    private boolean failed;

    /**
     * test concurrent inserts, updates, select count
     * all inside @Transactional
     * PASS
     */
    @Test
    public void testSaveTableByTimeAllTransactional() throws Exception {
        failed = false;
        logger.info("loop test 1");

        ExecutorService executor = Executors.newFixedThreadPool(nThreads * 2);
        List<Callable<Object>> tasks1 = insertTasks();

        List<Callable<Object>> tasks2 = updateTasksPassing();

        Collection<Callable<Object>> tasks = new ArrayList<>();
        tasks.addAll(tasks1);
        tasks.addAll(tasks2);

        runTasks(executor, tasks);

        shutdownExecutor(executor);

        logger.info("I have {} Tables saved", getCountTablePassing());

        Assert.assertFalse(isFailed());
    }

    /**
     * test concurrent inserts, updates, select count
     * insert, update inside @Transactional
     * select count in TransactionTempalate in test class method
     * FAILING derby with tooMuchContentionException
     */
    @Test
    public void testSaveTableByTimeWithTxTemplateInTest() throws Exception {
        failed = false;
        logger.info("loop test 2");

        ExecutorService executor = Executors.newFixedThreadPool(nThreads * 2);
        List<Callable<Object>> tasks1 = insertTasks();

        List<Callable<Object>> tasks2 = updateTasksFailing();

        Collection<Callable<Object>> tasks = new ArrayList<>();
        tasks.addAll(tasks1);
        tasks.addAll(tasks2);

        runTasks(executor, tasks);

        shutdownExecutor(executor);
        logger.info("I have {} Tables saved", getCountTableFailing());

        Assert.assertFalse(isFailed());
    }

    /**
     * test concurrent inserts, updates, select count
     * insert, update inside @Transactional
     * select count in TransactionTempalate in Service method
     */
    @Test
    public void testSaveTableByTimeWithTxTemplateInService() throws Exception {
        failed = false;
        logger.info("loop test 3");

        ExecutorService executor = Executors.newFixedThreadPool(nThreads * 2);
        List<Callable<Object>> tasks1 = insertTasks();

        List<Callable<Object>> tasks2 = updateTasksFailing2();

        Collection<Callable<Object>> tasks = new ArrayList<>();
        tasks.addAll(tasks1);
        tasks.addAll(tasks2);

        runTasks(executor, tasks);

        shutdownExecutor(executor);
        logger.info("I have {} Tables saved", getCountTableServiceFailing());

        Assert.assertFalse(isFailed());
    }

    private void shutdownExecutor(ExecutorService executor) throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        executor.shutdownNow();
    }

    private void runTasks(ExecutorService executor, Collection<Callable<Object>> tasks) throws InterruptedException {
        try {
            Awaitility.await().atMost(TEST_TIMEOUT, TimeUnit.SECONDS).until(() -> {
                executor.invokeAll(tasks);
                return null;
            });
        } catch (Exception e) {
            if (e instanceof TimeoutException) return;
            logger.warn("{}", e);
        }

        isInterrupted = true;
        Thread.currentThread().sleep(1000);
    }

    private List<Callable<Object>> updateTasksFailing() {
        return Collections.nCopies(nThreads, () -> {
            try {
                while (true) {
                    MyTable request = testable.getTable(getCountTableFailing() - 1);
                    if (request == null) continue;
                    request.setRequestID("som pipiq");
                    testable.updateTable(request);
                    if (isInterrupted()) throw new InterruptedException();
                }
            } catch (InterruptedException e) {
                //logger.info("interrupt");
            } catch (Exception e) {
                if (e instanceof LockAcquisitionException) {
                    logger.error("{}", e);
                    setFailed();
                } else {
                    //logger.warn("{}", e);
                }
            }
            return null;
        });
    }

    private List<Callable<Object>> updateTasksFailing2() {
        return Collections.nCopies(nThreads, () -> {
            try {
                while (true) {
                    MyTable request = testable.getTable(getCountTableServiceFailing() - 1);
                    if (request == null) continue;
                    request.setRequestID("som pipiq");
                    testable.updateTable(request);
                    if (isInterrupted()) throw new InterruptedException();
                }
            } catch (InterruptedException e) {
                //logger.info("interrupt");
            } catch (Exception e) {
                if (e instanceof LockAcquisitionException) {
                    logger.error("{}", e);
                    setFailed();
                } else {
                    //logger.warn("{}", e);
                }
            }
            return null;
        });
    }

    private List<Callable<Object>> updateTasksPassing() {
        return Collections.nCopies(nThreads, () -> {
            try {
                while (true) {
                    MyTable request = testable.getTable(getCountTablePassing() - 1);
                    if (request == null) continue;
                    request.setRequestID("teda som");
                    testable.updateTable(request);
                    if (isInterrupted()) throw new InterruptedException();
                }
            } catch (InterruptedException e) {
                //logger.info("interrupt");
            } catch (Exception e) {
                if (e instanceof LockAcquisitionException) {
                    logger.error("{}", e);
                    setFailed();
                } else {
                    //logger.warn("{}", e);
                }
            }
            return null;
        });
    }

    private List<Callable<Object>> insertTasks() {
        return Collections.nCopies(nThreads, () -> {
            try {
                while (true) {
                    MyTable request = new MyTable();
                    request.setBatch(1L);
                    request.setReqType("som");
                    request.setWorkstationID("ws");
                    request.setRequestID("teda");
                    testable.saveTable(request);
                    if (isInterrupted()) throw new InterruptedException();
                }
            } catch (InterruptedException e) {
                //logger.info("interrupt");
            } catch (Exception e) {
                if (e instanceof LockAcquisitionException) {
                    logger.error("{}", e);
                    setFailed();
                } else {
                    //logger.warn("{}", e);
                }
            }
            return null;
        });
    }

    private void setFailed() {
        failed = true;
    }

    private boolean isFailed() {
        return failed;
    }

    private boolean isInterrupted() {
        return isInterrupted;
    }

    @BeforeMethod
    public void beforeMethod() {
        FileSystemUtils.deleteRecursively(new File("derbyDB"));
        try {
            startEpsDB();
        } catch (Exception e) {
            logger.error("{}", e);
        }
        springInit();
        fillDB();
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        dropTable();
        try {
            stopEpsDB();
        } catch (SQLException e) {
            logger.error("{}", e);
        }
        FileSystemUtils.deleteRecursively(new File("derbyDB"));
    }

    private Long getCountTablePassing() {
        return testable.getCount();
    }

    private Long getCountTableFailing() {
        return txTemplate.execute(status -> (Long) sessionProvider.getCurrentSession()
                .createQuery("select count(*) from MYTABLE")
                .uniqueResult());
    }

    private Long getCountTableServiceFailing() {
        return testable.getCountWithTxTemplate();
    }

    private void dropTable() {
        logger.debug("drop table");
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                sessionProvider.getCurrentSession().createSQLQuery("DROP TABLE MyTable").executeUpdate();
            }
        });
    }

    private void fillDB() {
        logger.debug("creating db");
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                sessionProvider.getCurrentSession().createSQLQuery("create table MYTABLE (\n" +
                        "  ID                    BIGINT GENERATED BY DEFAULT AS IDENTITY\n" +
                        "    primary key,\n" +
                        "  AMOUNT                NUMERIC(19, 3),\n" +
                        "  APPLICATIONSENDER     VARCHAR(40),\n" +
                        "  BATCH                 NUMERIC(19),\n" +
                        "  CLERKID               VARCHAR(20),\n" +
                        "  POPID                 VARCHAR(40),\n" +
                        "  REQTYPE               VARCHAR(255) not null,\n" +
                        "  REQUESTID             VARCHAR(40)  not null,\n" +
                        "  SHIFTNUMBER           VARCHAR(20),\n" +
                        "  TERMINALID            VARCHAR(255),\n" +
                        "  WORKSTATIONID         VARCHAR(40)  not null,\n" +
                        "  CASHBACKAMOUNT        NUMERIC(19, 3),\n" +
                        "  REFERENCENUMBER       VARCHAR(40),\n" +
                        "  PAYMENTMETHOD         VARCHAR(40),\n" +
                        "  ORIGINALINVOICENUMBER VARCHAR(255),\n" +
                        "  ORIGINALAMOUNT        NUMERIC(19, 3),\n" +
                        "  RESENDFLAG            BOOLEAN,\n" +
                        "  HOSTCONFIGID          BIGINT,\n" +
                        "  TRANSACTIONNUMBER     VARCHAR(40),\n" +
                        "  STAN                  INTEGER,\n" +
                        "  TRNSECUREDATAID       BIGINT,\n" +
                        "  WASPREPAYCARDINBASKET BOOLEAN\n" +
                        ")").executeUpdate();
            }
        });
    }

    private void springInit() {
        logger.debug("Initalize spring db context");
        context = new AnnotationConfigApplicationContext(SpringConf.class, ServiceImpl.class);
        txTemplate = (TransactionTemplate) context.getBean("txTemplate");
        sessionProvider = (HibernateSessionProvider) context.getBean("sessionProvider");
        testable = context.getBean(IService.class);
    }

    private static void startEpsDB() throws Exception {
        logger.info("Starting EPS DB");
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        connection = DriverManager.getConnection(DERBY_CONNECTION_URL + ";create=true;user=user;password=user;");
        logger.info("EPS DB Successfully started");
    }

    private static void stopEpsDB() throws SQLException {
        logger.info("Stopping EPS DB");
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
        }
        logger.info("EPS DB successfully stopped");
    }
}