package org.itmo.lab3.config;

import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;


@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {
    "org.itmo.lab3.service",
    "org.itmo.lab3.repository",
    "org.itmo.lab3.validation",
    "org.itmo.lab3.aspect"
})
public class SpringConfig {
    
    @Bean
    public Validator validator() {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        return factoryBean;
    }
}

