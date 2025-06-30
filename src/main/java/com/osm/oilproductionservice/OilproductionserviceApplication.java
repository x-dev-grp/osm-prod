package com.osm.oilproductionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.xdev","com.xdev.communicator", "com.xdev.xdevsecurity", "com.osm.oilproductionservice"})
@ComponentScan(basePackages = {"com.xdev", "com.xdev.xdevbase", "com.osm.oilproductionservice"})
@EnableJpaRepositories(basePackages = {"com.xdev", "com.xdev.xdevbase", "com.osm.oilproductionservice"}, repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
public class OilproductionserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OilproductionserviceApplication.class, args);
    }

}
