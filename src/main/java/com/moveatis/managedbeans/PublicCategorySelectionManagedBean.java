/*
 * Copyright (c) 2016, Jarmo Juujärvi, Sami Kallio, Kai Korhonen, Juha Moisio, Ilari Paananen 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     3. Neither the name of the copyright holder nor the names of its 
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.moveatis.managedbeans;

import com.moveatis.category.CategoryEntity;
import com.moveatis.category.CategorySetEntity;
import com.moveatis.helpers.CategorySetHelper;
import com.moveatis.interfaces.CategorySet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sami Kallio <phinaliumz at outlook.com>
 */
@Named(value="publicCategorySelectionManagedBean")
@ViewScoped
public class PublicCategorySelectionManagedBean implements Serializable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PublicCategorySelectionManagedBean.class);
    private static final long serialVersionUID = 1L;
    
    private Long selectedPublicEventCategorySetId;
    
    private Map<String, Long> publicCategorySetsMap;
    
    private Set<CategoryEntity> selectedPublicCategories;
    
    private Set<CategorySetEntity> publicCategorySetsInSelection;
    
    private Map<Long, List<Long>> selectedCategoriesMap;


    @Inject
    private CategorySet categorySetEJB;

    /** Creates a new instance of PublicCategorySelectionManagedBean */
    public PublicCategorySelectionManagedBean() {
        
    }

    public Long getSelectedPublicEventCategorySetId() {
        return selectedPublicEventCategorySetId;
    }

    public void setSelectedPublicEventCategorySetId(Long selectedPublicEventCategorySetId) {
        this.selectedPublicEventCategorySetId = selectedPublicEventCategorySetId;
        LOGGER.debug("selectedPublicEventCategorySetId -> " + this.selectedPublicEventCategorySetId);
    }

    public Map<String, Long> getPublicCategorySetsMap() {
        if(publicCategorySetsMap == null) {
            Set<CategorySetEntity> publicCategorySets = categorySetEJB.findPublicCatagorySets();
            publicCategorySetsMap = CategorySetHelper.fillCategorySetMap(publicCategorySets);
        }
        return publicCategorySetsMap;
    }

    public void setPublicCategorySetsMap(Map<String, Long> publicCategorySetsMap) {
        this.publicCategorySetsMap = publicCategorySetsMap;
    }
    
    public Map<Long, List<Long>> getSelectedCategoriesMap() {
        return this.selectedCategoriesMap;
    }

    public void setSelectedCategoriesMap(Map<Long, List<Long>> selectedCategoriesMap) {
        this.selectedCategoriesMap = selectedCategoriesMap;
    }

    public Set<CategoryEntity> getSelectedPublicCategories() {
        return selectedPublicCategories;
    }

    public void setSelectedPublicCategories(Set<CategoryEntity> selectedPublicCategories) {
        this.selectedPublicCategories = selectedPublicCategories;
    }

    public Set<CategorySetEntity> getPublicCategorySetsInSelection() {
        if(this.publicCategorySetsInSelection != null) {
            LOGGER.debug("publicCategorySetsInSelection -> size -> " + this.publicCategorySetsInSelection.size());
        }
        return publicCategorySetsInSelection;
    }

    public void setPublicCategorySetsInSelection(Set<CategorySetEntity> publicCategorySetsInSelection) {
        this.publicCategorySetsInSelection = publicCategorySetsInSelection;
        LOGGER.debug("publicCategorySetsInSelection -> size -> " + this.publicCategorySetsInSelection.size());
    }
    
    public void addPublicCategorySet() {
        
        if(publicCategorySetsInSelection == null) {
            publicCategorySetsInSelection = new HashSet<>();
        }
        CategorySetEntity categorySetEntity = categorySetEJB.find(selectedPublicEventCategorySetId);
        
        LOGGER.debug("categorySetEntity id -> " + categorySetEntity.getId());
        
        publicCategorySetsInSelection.add(categorySetEntity);
        for(CategoryEntity categoryEntity : categorySetEntity.getCategoryEntitys().values()) {
            selectedPublicCategories.add(categoryEntity);
        }
    }
    
    public void removeCategorySet(CategorySetEntity categorySetEntity) {
        publicCategorySetsInSelection.remove(categorySetEntity);
    }
    
    public void removeCategory(CategoryEntity categoryEntity) {
        selectedPublicCategories.remove(categoryEntity);
    }

}