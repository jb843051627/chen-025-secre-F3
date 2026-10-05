package com.fc.v2.service.countersign;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.fc.v2.service.impl.TSecreCarrierServiceImpl;
import com.fc.v2.service.impl.TSecreCountersignServiceImpl;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Date;

/**
 * 六则测试的库架：H2 内存库 + 真 MyBatis-Plus + 真事务，不起整个 SpringBoot（绕开 shiro/druid）。
 */
@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = "com.fc.v2.mapper.auto")
public class CountersignTestConfig {

    @Bean(destroyMethod = "shutdown")
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("countersign;MODE=MySQL;DB_CLOSE_DELAY=-1")
                .build();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        MybatisConfiguration configuration = new MybatisConfiguration();
        factory.setConfiguration(configuration);
        GlobalConfig globalConfig = new GlobalConfig().setMetaObjectHandler(new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createBy", String.class, "test");
                this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateBy", String.class, "test");
                this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
            }
        });
        factory.setGlobalConfig(globalConfig);
        return factory.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public TSecreCountersignServiceImpl countersignService() {
        return new TSecreCountersignServiceImpl();
    }

    @Bean
    public TSecreCarrierServiceImpl carrierService() {
        return new TSecreCarrierServiceImpl();
    }
}
