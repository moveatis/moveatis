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
package com.moveatis.category;

import com.moveatis.event.EventGroupEntity;
import com.moveatis.abstracts.AbstractBean;
import com.moveatis.interfaces.AnonUser;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import com.moveatis.interfaces.CategorySet;
import com.moveatis.interfaces.EventGroup;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sami Kallio <phinaliumz at outlook.com>
 */
@Stateless
public class CategorySetBean extends AbstractBean<CategorySetEntity> implements CategorySet {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CategorySetBean.class);

    @PersistenceContext(unitName = "LOTAS_PERSISTENCE")
    private EntityManager em;
    
    @Inject
    private AnonUser anonUserEJB;
    
    @Inject
    private EventGroup eventGroupEJB;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CategorySetBean() {
        super(CategorySetEntity.class);
    }
    
    @Override
    public void remove(CategorySetEntity categorySetEntity) {
        super.remove(categorySetEntity);
        eventGroupEJB.removeCategorySetEntityFromEventGroups(categorySetEntity);
        categorySetEntity.setEventGroupEntity(null);
        super.edit(categorySetEntity);
    }

    @Override
    public Set<CategorySetEntity> findPublicCategorySets() {
        
        List<EventGroupEntity> publicEventGroups = eventGroupEJB.findAllForPublicUser();
        Set<CategorySetEntity> publicCategorySets = new HashSet<>();
        
        for(EventGroupEntity eventGroupEntity : publicEventGroups) {
            Set<CategorySetEntity> eventGroupCategorySets = eventGroupEntity.getCategorySets();
            publicCategorySets.addAll(eventGroupCategorySets);
        }
        
        return publicCategorySets;
    }

    @Override
    public void removeCategoryFromCategorySet(CategorySetEntity categorySet, CategoryEntity categoryEntity) {
        Map<Integer, CategoryEntity> categories = categorySet.getCategoryEntitys();
        Integer keyFound = -1;
        
        for(Integer key : categories.keySet()) {
            if(categories.get(key).getId().equals(categoryEntity.getId())) {
                keyFound = key;
            }
        }
        
        if(keyFound > -1) {
            categories.remove(keyFound);
        }
        
        categorySet.setCategoryEntitys(categories);
        super.edit(categorySet);
    }
}
