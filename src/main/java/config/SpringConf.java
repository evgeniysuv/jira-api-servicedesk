package config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by esuv on 4/29/18
 */
@Configuration
@ComponentScan({"converter", "model", "printer", "executable"})
public class SpringConf {

}
