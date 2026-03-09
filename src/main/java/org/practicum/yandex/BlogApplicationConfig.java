package org.practicum.yandex;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@Configuration
@PropertySource(Constants.PROPERTIES_PATH)
@ComponentScan(basePackages = Constants.ROOT_PACKAGE)
public class BlogApplicationConfig {
}
