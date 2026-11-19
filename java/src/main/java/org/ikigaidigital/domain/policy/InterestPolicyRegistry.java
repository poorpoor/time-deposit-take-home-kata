package org.ikigaidigital.domain.policy;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InterestPolicyRegistry {

    private final Map<String, InterestPolicy> policiesByPlanType;

    public InterestPolicyRegistry(List<InterestPolicy> policies) {
        Objects.requireNonNull(policies, "policies must not be null");
        Map<String, InterestPolicy> map = new HashMap<>();
        for (InterestPolicy policy : policies) {
            Objects.requireNonNull(policy, "policy must not be null");
            String planType = Objects.requireNonNull(policy.planType(), "policy.planType() must not be null");
            InterestPolicy previous = map.putIfAbsent(planType, policy);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate InterestPolicy registered for planType: " + planType);
            }
        }
        this.policiesByPlanType = Collections.unmodifiableMap(map);
    }

    public static InterestPolicyRegistry defaultRegistry() {
        return new InterestPolicyRegistry(List.of(
            new BasicInterestPolicy(),
            new StudentInterestPolicy(),
            new PremiumInterestPolicy()
        ));
    }

    public Optional<InterestPolicy> findByPlanType(String planType) {
        if (planType == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(policiesByPlanType.get(planType));
    }
}
