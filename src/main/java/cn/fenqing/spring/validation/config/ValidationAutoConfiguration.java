package cn.fenqing.spring.validation.config;

import cn.fenqing.spring.validation.code.ValidationProxyProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author fenqing
 * @version 0.0.1
 */
@Configuration
@ComponentScan("cn.fenqing.spring.validation.code.handlers")
public class ValidationAutoConfiguration {

    @Bean
    public ValidationProxyProcessor validationProxyProcessor(){
        return new ValidationProxyProcessor();
    }

}
