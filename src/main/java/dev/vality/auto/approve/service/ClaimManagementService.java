package dev.vality.auto.approve.service;

import dev.vality.damsel.claim_management.UserInfo;

public interface ClaimManagementService {
    void accept(UserInfo userInfo, String partyId, Long claimId, Integer claimRevision);
}
