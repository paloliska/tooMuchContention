package sk.palo.liska;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

/**
 * @author pavol.liska
 * @date 6/23/2018
 */
public class Run {

	private static final Logger logger = LoggerFactory.getLogger(Run.class);

	private static final String DERBY_CONNECTION_URL = "jdbc:derby:derbyDB";

	private static Connection connection;
	public static final int TEST_TIMEOUT = 10;
	private final int nThreads = 16;
	private ApplicationContext context;

	private TransactionTemplate txTemplate;
	private HibernateSessionProvider sessionProvider;
	boolean isInterrupted = false;
	private IService service;
	private boolean failed;

	public static void main(String[] args) {
		Run r = new Run();

		r.beforeMethod();
		try {
			r.testSaveTableByTimeAllTransactional();
		} catch (Exception e) {
			logger.error("{}", e);
		}
		r.afterMethod();

		r.beforeMethod();
		try {
			r.testSaveTableByTimeWithTxTemplateInService();
		} catch (Exception e) {
			logger.error("{}", e);
		}
		r.afterMethod();

		r.beforeMethod();
		try {
			r.testSaveTableByTimeWithTxTemplateInTest();
		} catch (Exception e) {
			logger.error("{}", e);
		}
		r.afterMethod();
	}

	/**
	 * concurrent inserts, updates, select count
	 * all inside @Transactional
	 */
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
	}

	/**
	 * concurrent inserts, updates, select count
	 * insert, update inside @Transactional
	 * select count in TransactionTempalate in test class method
	 * derby with tooMuchContentionException
	 */
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
	}

	/**
	 * concurrent inserts, updates, select count
	 * insert, update inside @Transactional
	 * select count in TransactionTempalate in Service method
	 */
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
					MyTable request = service.getTable(getCountTableFailing() - 1);
					if (request == null) continue;
					request.setRequestID("som pipiq");
					service.updateTable(request);
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
					MyTable request = service.getTable(getCountTableServiceFailing() - 1);
					if (request == null) continue;
					request.setRequestID("som pipiq");
					service.updateTable(request);
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
					MyTable request = service.getTable(getCountTablePassing() - 1);
					if (request == null) continue;
					request.setRequestID("teda som");
					service.updateTable(request);
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
					service.saveTable(request);
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
		return service.getCount();
	}

	private Long getCountTableFailing() {
		return txTemplate.execute(status -> (Long) sessionProvider.getCurrentSession()
			.createQuery("select count(*) from MYTABLE")
			.uniqueResult());
	}

	private Long getCountTableServiceFailing() {
		return service.getCountWithTxTemplate();
	}

	private void dropTable() {
		logger.debug("drop table");
		txTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				sessionProvider.getCurrentSession().createNativeQuery("DROP TABLE MyTable").executeUpdate();
			}
		});
	}

	private void fillDB() {
		logger.debug("creating db");
		txTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				sessionProvider.getCurrentSession().createNativeQuery("create table MYTABLE (\n" +
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
					"  WORKSTATIONID         VARCHAR(40)  not null\n" +
					")").executeUpdate();
			}
		});
	}

	private void springInit() {
		logger.debug("Initalize spring db context");
		context = new AnnotationConfigApplicationContext(SpringConf.class, ServiceImpl.class);
		txTemplate = (TransactionTemplate) context.getBean("txTemplate");
		sessionProvider = (HibernateSessionProvider) context.getBean("sessionProvider");
		service = context.getBean(IService.class);
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