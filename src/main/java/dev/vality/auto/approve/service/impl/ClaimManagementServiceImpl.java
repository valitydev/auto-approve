package dev.vality.auto.approve.service.impl;

import dev.vality.auto.approve.configuration.meta.*;
import dev.vality.auto.approve.exception.NotFoundException;
import dev.vality.auto.approve.service.ClaimManagementService;
import dev.vality.damsel.claim_management.*;
import dev.vality.woody.api.flow.WFlow;
import dev.vality.woody.api.trace.ContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimManagementServiceImpl implements ClaimManagementService {

    private final ClaimManagementSrv.Iface claimManagementClient;

    @Override
    public void accept(UserInfo userInfo, String partyId, Long claimId, Integer claimRevision) {
        log.info("Accept a claim for partyId={}, claimId={}", partyId, claimId);
        new WFlow().createServiceFork(() -> {
            ContextUtils.setCustomMetadataValue(UserIdentityIdExtensionKit.KEY, userInfo.getId());
            ContextUtils.setCustomMetadataValue(UserIdentityEmailExtensionKit.KEY, userInfo.getEmail());
            ContextUtils.setCustomMetadataValue(UserIdentityUsernameExtensionKit.KEY, userInfo.getUsername());
            ContextUtils.setCustomMetadataValue(UserIdentityRealmExtensionKit.KEY, getType(userInfo));
            acceptClaim(partyId, claimId, claimRevision);
        }).run();
        log.info("Claim has been accepted for partyId={}, claimId={}", partyId, claimId);
    }

    private void acceptClaim(String partyId, Long claimId, Integer claimRevision) {
        try {
            claimManagementClient.acceptClaim(partyId, claimId, claimRevision);
        } catch (ClaimNotFound e) {
            throw new NotFoundException(
                    String.format("Claim not found for partyId=%s, claimId=%d, claimRevision=%d",
                            partyId, claimId, claimRevision),
                    e);
        } catch (InvalidClaimRevision | InvalidClaimStatus e) {
            throw new RuntimeException(
                    String.format("Invalid claim state for partyId=%s, claimId=%d, claimRevision=%d",
                            partyId, claimId, claimRevision),
                    e);
        } catch (TException e) {
            throw new RuntimeException("Thrift exception while processed event", e);
        }
    }

    private UserTypeEnum getType(UserInfo userInfo) {
        if (userInfo.getType().isSetInternalUser()) {
            return UserTypeEnum.internal;
        } else if (userInfo.getType().isSetExternalUser()) {
            return UserTypeEnum.external;
        } else {
            throw new IllegalArgumentException("Unknown userInfo type: " + userInfo.getType());
        }
    }
}
