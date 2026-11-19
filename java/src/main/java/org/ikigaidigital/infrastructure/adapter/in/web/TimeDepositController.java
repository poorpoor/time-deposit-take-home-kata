package org.ikigaidigital.infrastructure.adapter.in.web;

import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.in.UpdateTimeDepositBalancesUseCase;
import org.ikigaidigital.infrastructure.adapter.in.web.generated.api.TimeDepositsApi;
import org.ikigaidigital.infrastructure.adapter.in.web.generated.model.TimeDepositResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
public class TimeDepositController implements TimeDepositsApi {

    private final GetAllTimeDepositsUseCase getAllTimeDepositsUseCase;
    private final UpdateTimeDepositBalancesUseCase updateTimeDepositBalancesUseCase;
    private final TimeDepositWebMapper webMapper;

    public TimeDepositController(
        GetAllTimeDepositsUseCase getAllTimeDepositsUseCase,
        UpdateTimeDepositBalancesUseCase updateTimeDepositBalancesUseCase,
        TimeDepositWebMapper webMapper
    ) {
        this.getAllTimeDepositsUseCase = Objects.requireNonNull(
            getAllTimeDepositsUseCase,
            "getAllTimeDepositsUseCase must not be null"
        );
        this.updateTimeDepositBalancesUseCase = Objects.requireNonNull(
            updateTimeDepositBalancesUseCase,
            "updateTimeDepositBalancesUseCase must not be null"
        );
        this.webMapper = Objects.requireNonNull(webMapper, "webMapper must not be null");
    }

    @Override
    public ResponseEntity<List<TimeDepositResponse>> getAllTimeDeposits() {
        List<TimeDepositResponse> response = getAllTimeDepositsUseCase.getAllTimeDeposits().stream()
            .map(webMapper::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> updateAllTimeDepositBalances() {
        updateTimeDepositBalancesUseCase.updateAllBalances();
        return ResponseEntity.noContent().build();
    }
}
