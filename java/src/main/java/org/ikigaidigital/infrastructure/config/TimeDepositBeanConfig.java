package org.ikigaidigital.infrastructure.config;

import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.in.UpdateTimeDepositBalancesUseCase;
import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.ikigaidigital.application.service.GetAllTimeDepositsService;
import org.ikigaidigital.application.service.UpdateTimeDepositBalancesService;
import org.ikigaidigital.domain.policy.InterestPolicyRegistry;
import org.ikigaidigital.infrastructure.adapter.in.web.TimeDepositWebMapper;
import org.ikigaidigital.infrastructure.adapter.out.persistence.TimeDepositPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeDepositBeanConfig {

    @Bean
    public InterestPolicyRegistry interestPolicyRegistry() {
        return InterestPolicyRegistry.defaultRegistry();
    }

    @Bean
    public TimeDepositCalculator timeDepositCalculator(InterestPolicyRegistry policyRegistry) {
        return new TimeDepositCalculator(policyRegistry);
    }

    @Bean
    public GetAllTimeDepositsUseCase getAllTimeDepositsUseCase(TimeDepositRepository timeDepositRepository) {
        return new GetAllTimeDepositsService(timeDepositRepository);
    }

    @Bean
    public UpdateTimeDepositBalancesUseCase updateTimeDepositBalancesUseCase(
        TimeDepositRepository timeDepositRepository,
        TimeDepositCalculator timeDepositCalculator
    ) {
        return new UpdateTimeDepositBalancesService(timeDepositRepository, timeDepositCalculator);
    }

    @Bean
    public TimeDepositPersistenceMapper timeDepositPersistenceMapper() {
        return new TimeDepositPersistenceMapper();
    }

    @Bean
    public TimeDepositWebMapper timeDepositWebMapper() {
        return new TimeDepositWebMapper();
    }
}
