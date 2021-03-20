package tk.iovr.mall_learning001.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 */
@Configuration
@MapperScan("tk.iovr.mall_learning001.mybatisgenerator.mapper")
public class MyBatisConfig {
}
