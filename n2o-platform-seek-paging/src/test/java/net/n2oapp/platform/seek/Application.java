package net.n2oapp.platform.seek;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "net.n2oapp.platform.seek")
@EnableJpaRepositories(repositoryFactoryBeanClass = SeekableJpaRepositoryFactoryBean.class)
public class Application {
}
