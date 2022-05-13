package dev.vality.auto.approve.service;

import dev.vality.damsel.domain.Category;

public interface DominantService {
    Category getCategory(Integer categoryId);
}
