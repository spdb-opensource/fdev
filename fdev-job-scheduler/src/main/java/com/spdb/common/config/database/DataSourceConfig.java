package com.spdb.common.config.database;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * 默认数据源
 * Created by lettuce on 2017/07/27
 *
 * @author lizz
 */

@Configuration
public class DataSourceConfig {

    /*@Bean(name="schedulerDataSource")
    @Primary
    @ConfigurationProperties(prefix="scheduler.datasource")
    public DataSource dataSource() throws Exception{
        return DataSourceBuilder.create().*//*type(com.alibaba.druid.pool.DruidDataSource.class).*//*build();
	}*/

    @Bean(name = "schedulerDataSource")
    @Primary
    @ConfigurationProperties(prefix = "scheduler.datasource")
    public DruidDataSource dataSource() {
        List list = new ArrayList<Filter>();
        list.add(slf4jLogFilter());
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setProxyFilters(list);
        return druidDataSource;
    }

    @Bean
    public Slf4jLogFilter slf4jLogFilter() {
        Slf4jLogFilter slf4jLogFilter = new Slf4jLogFilter();
        return slf4jLogFilter;
    }

    /**
     * 创建SqlSessionFactory工厂类
     *
     * @param dataSource
     * @return
     * @throws Exception
     */
    @Bean(name = "schedulerSqlSessionFactory")
    @Primary
    public SqlSessionFactory schedulerSqlSessionFactory(@Qualifier("schedulerDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mybatis/mapper/*.xml"));
        return bean.getObject();
    }

    /**
     * 创建DataSourceTransactionManager实例
     *
     * @param dataSource
     * @return
     */
    @Bean(name = "schedulerDataSourceTransactionManager")
    @Primary
    public DataSourceTransactionManager schedulerDataSourceTransactionManager(@Qualifier("schedulerDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 创建SqlSessionTemplate实例
     *
     * @param sqlSessionFactory
     * @return
     */
    @Bean(name = "schedulerSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate schedulerSqlSessionTemplate(@Qualifier("schedulerSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
