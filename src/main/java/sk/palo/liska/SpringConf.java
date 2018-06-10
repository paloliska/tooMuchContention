package sk.palo.liska;

import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author pavol.liska
 * @date 6/10/2018
 */
@Configuration
@EnableTransactionManagement
@PropertySource("classpath:test.properties")
public class SpringConf {
    @Autowired
    private Environment env;

    @Bean(name = "sessionProvider")
    public SessionProvider getSessionProvider() {
        return new SessionProvider();
    }

    @Bean(name = "sessionFactorySpring")
    public LocalSessionFactoryBean getSessionFactory() {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(getDataSource());
        factory.setPackagesToScan(env.getProperty("hibernatePackageToScan"));
        factory.setHibernateProperties(getHibernateProperties());

        return factory;
    }

    @Bean(name = "txManager")
    public HibernateTransactionManager getTransactionManager() {
        return new HibernateTransactionManager(getSessionFactory().getObject());
    }

    @Bean(name = "txTemplate")
    public TransactionTemplate getTransactionTemplate() {
        return new TransactionTemplate(getTransactionManager());
    }

    @Bean(name = "dataSource")
    public DataSource getDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(env.getProperty("driverClassName"));

        dataSource.setUrl(env.getProperty("databaseUrl"));
        dataSource.setUsername(env.getProperty("dbUserName"));
        dataSource.setPassword(env.getProperty("dbPassword"));
        dataSource.setInitialSize(Integer.valueOf(env.getProperty("initialSize")));
        dataSource.setMaxTotal(Integer.valueOf(env.getProperty("maxActive")));
        dataSource.setMaxIdle(Integer.valueOf(env.getProperty("maxIdle")));

        return dataSource;
    }

    protected Properties getHibernateProperties() {
        Properties props = new Properties();

        props.setProperty("hibernate.dialect", getProperty("hibernateDialect"));
        props.setProperty("hibernate.show_sql", getProperty("hibernateShowSql"));
        props.setProperty("hibernate.format_sql", getProperty("hibernateFormatSql"));
        props.setProperty("hibernate.use_sql_comments", getProperty("hibernateUseSqlComments"));
        props.setProperty("hibernate.cache.use_second_level_cache", getProperty("hibernateUseSecondCache"));
        props.setProperty("jadira.usertype.autoRegisterUserTypes", getProperty("jadiraAutoRegisterUserTypes"));
        props.setProperty("jadira.usertype.databaseZone", getProperty("jadiraDatabaseZone"));
        props.setProperty("jadira.usertype.javaZone", getProperty("jadiraJavaZone"));

        return props;
    }

    protected String getProperty(String propertyKey) {
        return env.getProperty(propertyKey);
    }
}
