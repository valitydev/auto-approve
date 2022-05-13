package dev.vality.auto.approve.handler;

import dev.vality.auto.approve.service.ClaimManagementService;
import dev.vality.auto.approve.service.DominantService;
import dev.vality.auto.approve.utils.ClaimUtils;
import dev.vality.damsel.claim_management.ClaimCreated;
import dev.vality.damsel.claim_management.Event;
import dev.vality.damsel.claim_management.ShopParams;
import dev.vality.damsel.domain.Category;
import dev.vality.damsel.domain.CategoryType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimCreatedHandler implements dev.vality.auto.approve.handler.EventHandler<Event> {

    private final ClaimManagementService claimManagementServiceImpl;
    private final DominantService dominantServiceImpl;

    @Override
    public boolean isAccept(Event event) {
        return event.getChange().isSetCreated();
    }

    @Override
    @SneakyThrows
    public void handle(Event event) {
        ClaimCreated claimCreated = event.getChange().getCreated();
        String partyId = claimCreated.getPartyId();
        long claimId = claimCreated.getId();

        List<ShopParams> shopParamsList = ClaimUtils.extractShopParamsList(claimCreated);
        shopParamsList.forEach(shopParams -> {
            int categoryId = shopParams.getCategory().getId();
            Category category = dominantServiceImpl.getCategory(categoryId);
            if (CategoryType.test.equals(category.getType())) {
                claimManagementServiceImpl.accept(event.getUserInfo(), partyId, claimId, claimCreated.getRevision());
            }
        });
    }
}
