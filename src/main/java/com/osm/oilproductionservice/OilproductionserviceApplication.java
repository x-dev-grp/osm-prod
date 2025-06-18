package com.osm.oilproductionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
//@EnableFeignClients
@ComponentScan(basePackages = {"com.xdev", "com.xdev.xdevbase", "com.osm.oilproductionservice"})
@EnableJpaRepositories(basePackages = {"com.xdev", "com.xdev.xdevbase", "com.osm.oilproductionservice"}, repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
public class OilproductionserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OilproductionserviceApplication.class, args);
    }

}
