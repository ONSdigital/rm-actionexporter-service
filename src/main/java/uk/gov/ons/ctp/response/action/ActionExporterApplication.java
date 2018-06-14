package uk.gov.ons.ctp.response.action;

import net.sourceforge.cobertura.CoverageIgnore;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import uk.gov.ons.ctp.common.distributed.DistributedInstanceManager;
import uk.gov.ons.ctp.common.distributed.DistributedInstanceManagerRedissonImpl;
import uk.gov.ons.ctp.common.distributed.DistributedLatchManager;
import uk.gov.ons.ctp.common.distributed.DistributedLatchManagerRedissonImpl;
import uk.gov.ons.ctp.common.distributed.DistributedLockManager;
import uk.gov.ons.ctp.common.distributed.DistributedLockManagerRedissonImpl;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.repository.impl.BaseRepositoryImpl;

/**
 * The main entry point into the Action Service SpringBoot Application.
 */
@CoverageIgnore
@SpringBootApplication
@EnableTransactionManagement
@IntegrationComponentScan
@ComponentScan(basePackages = {"uk.gov.ons.ctp.response"})
@EnableJpaRepositories(basePackages = {"uk.gov.ons.ctp.response"}, repositoryBaseClass = BaseRepositoryImpl.class)
@EntityScan("uk.gov.ons.ctp.response")
@EnableAsync
@EnableCaching
@EnableScheduling
@ImportResource("springintegration/main.xml")
public class ActionExporterApplication {

  public static final String ACTION_EXECUTION_LOCK = "actionexport.request.execution";

  @Autowired
  private AppConfig appConfig;

  /**
   * Bean used to access Distributed Lock Manager
   *
   * @param redissonClient Redisson Client
   * @return the Distributed Lock Manager
   */
  @Bean
  public DistributedInstanceManager actionExportInstanceManager(RedissonClient redissonClient) {
    return new DistributedInstanceManagerRedissonImpl(ACTION_EXECUTION_LOCK, redissonClient);
  }

  /**
   * Bean used to access Distributed Latch Manager
   *
   * @param redissonClient Redisson Client
   * @return the Distributed Lock Manager
   */
  @Bean
  public DistributedLatchManager actionExportLatchManager(RedissonClient redissonClient) {
    return new DistributedLatchManagerRedissonImpl(ACTION_EXECUTION_LOCK, redissonClient,
        appConfig.getDataGrid().getLockTimeToLiveSeconds());
  }

  /**
   * Bean used to access Distributed Execution Lock Manager
   *
   * @param redissonClient Redisson Client
   * @return the Distributed Lock Manager
   */
  @Bean
  public DistributedLockManager actionExportExecutionLockManager(RedissonClient redissonClient) {
    return new DistributedLockManagerRedissonImpl(ACTION_EXECUTION_LOCK, redissonClient,
        appConfig.getDataGrid().getLockTimeToLiveSeconds());
  }

  /**
   * Rest Exception Handler
   *
   * @return a Rest Exception Handler
   */
  @Bean
  public RestExceptionHandler restExceptionHandler() {
    return new RestExceptionHandler();
  }

  /**
   * Custom Object Mapper
   *
   * @return a customer object mapper
   */
  @Bean @Primary
  public CustomObjectMapper customObjectMapper() {
    return new CustomObjectMapper();
  }

  /**
   * Bean used to create and configure Redisson Client
   *
   * @return the Redisson client
   */
  @Bean
  public RedissonClient redissonClient() {
    Config config = new Config();
    config.useSingleServer()
        .setAddress(appConfig.getDataGrid().getAddress())
        .setPassword(appConfig.getDataGrid().getPassword());
    return Redisson.create(config);
  }

  /**
   * This method is the entry point to the Spring Boot application.
   *
   * @param args These are the optional command line arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(ActionExporterApplication.class, args);
  }
}
