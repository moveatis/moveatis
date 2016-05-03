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
package com.moveatis.restful;

import com.moveatis.category.CategoryEntity;
import com.moveatis.interfaces.Category;
import com.moveatis.interfaces.Label;
import com.moveatis.interfaces.Observation;
import com.moveatis.interfaces.Record;
import com.moveatis.interfaces.Session;
import com.moveatis.label.LabelEntity;
import com.moveatis.managedbeans.CategorySelectionManagedBean;
import com.moveatis.observation.ObservationEntity;
import com.moveatis.records.RecordEntity;
import com.moveatis.timezone.TimeZoneInformation;
import java.io.Serializable;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TimeZone;
import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sami Kallio <phinaliumz at outlook.com>
 */
@Path("/records")
@Named(value = "recordBean")
@Stateful
public class RecordListenerBean implements Serializable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordListenerBean.class);
    private static final long serialVersionUID = 1L;
    
    private JsonReader jsonReader;
    
    @Context
    private HttpServletRequest httpRequest;
    
    @Inject
    private Session sessionBean;
    
    private ResourceBundle messages;
    
    private ObservationEntity observationEntity;
    
    @Inject
    private Observation observationEJB;
    @Inject
    private Record recordEJB;
    @Inject
    private Category categoryEJB;
    @Inject
    private Label labelEJB;

    public RecordListenerBean() {
        
    }
    
    /*
    * Do we actually need this at all?
    */
    @POST
    @Path("startobservation")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String startObservation(String data) {
        LOGGER.debug(data);
        return "success";
    }
    
    /*
    * TODO: Needs work - what to do when keep-alive request is commenced?
    */
    // NOTE: Used by observer view.
    // TODO: What about other views? What happens if session expires? Redirect to front page?
    @POST
    @Path("keepalive")
    @Produces(MediaType.TEXT_PLAIN)
    public String keepAlive() {
        LOGGER.debug("keep-alive received");
        return "keep-alive";
    }
    
    @POST
    @Path("addobservationdata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addObservationData(String data) {
        Locale locale = httpRequest.getLocale();
        messages = ResourceBundle.getBundle("com.moveatis.messages.Messages", locale);
        StringReader stringReader = new StringReader(data);
        
        jsonReader = Json.createReader(stringReader);
        
        JsonObject jObject = jsonReader.readObject();
        JsonNumber duration = jObject.getJsonNumber("duration");
        JsonNumber timeZoneOffset = jObject.getJsonNumber("timeZoneOffsetInMs");
        JsonNumber DSTOffset = jObject.getJsonNumber("daylightSavingInMs");
        JsonArray array = jObject.getJsonArray("data");
        
        jsonReader.close();

        Date createdTime = Calendar.getInstance().getTime();
        
        TimeZone timeZone = TimeZoneInformation.getTimeZoneFromOffset(
                timeZoneOffset.intValue(), DSTOffset.intValue());
        
        sessionBean.setSessionTimeZone(timeZone);

        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT, locale);
        dateFormat.setTimeZone(timeZone);
        

        observationEntity = new ObservationEntity();
        observationEntity.setName(messages.getString("obs_title")
                + " - " + dateFormat.format(createdTime));
        observationEntity.setDuration(duration.longValue());
        
        observationEntity.setCreated(createdTime);
        
        
        /*
            Add categories to database if they are not already in database,
            and add the categories to a map by id for later use.
        */
        Map<Long, CategoryEntity> categoriesById = new HashMap<>();
        
        for (CategorySelectionManagedBean.CategorySet categorySet : sessionBean.getCategorySetsInUse()) {
            for (CategorySelectionManagedBean.Category category : categorySet.getCategories()) {
                
                CategoryEntity categoryEntity;
                Long categoryId = category.getId();
                
                if (category.isInDatabase()) {
                    categoryEntity = categoryEJB.find(categoryId);
                } else {
                    LOGGER.debug("Adding new category to database: " + category.getName());
                    
                    categoryEntity = new CategoryEntity();
                    LabelEntity labelEntity = new LabelEntity();
                    labelEntity.setLabel(category.getName());
                    categoryEntity.setLabel(labelEntity);

                    labelEJB.create(labelEntity);
                    categoryEJB.create(categoryEntity);
                    
                    // TODO: If user comes back to observer with browser's back button,
                    //       the observer page won't be rebuilt => category buttons have wrong ids
                    //       for previously added new categories! How to fix?
                    // NOTE: Works well with "reset observation" button.
                    category.setId(categoryEntity.getId());
                    category.setInDatabase(true);
                }
                
                categoriesById.put(categoryId, categoryEntity);
            }
        }

        try {
            for (int i = 0; i < array.size(); i++) {
                JsonObject object = array.getJsonObject(i);
                RecordEntity record = new RecordEntity();
                
                Long id = object.getJsonNumber("id").longValue();
                CategoryEntity categoryEntity = categoriesById.get(id);
                
                if (categoryEntity == null) {
                    LOGGER.debug("Received a record with unknown id! Skipping...");
                    continue;
                }
                
                record.setCategory(categoryEntity);
                
                record.setStartTime(object.getJsonNumber("startTime").longValue());
                record.setEndTime(object.getJsonNumber("endTime").longValue());

                recordEJB.create(record);
                observationEntity.addRecord(record);
            }
        } catch(Exception e) {
            LOGGER.debug(e.toString());
            return "failed";
        }
        
        observationEJB.create(observationEntity);
        
        SortedSet<Long> observations = sessionBean.getSessionObservationsIds();
        observations.add(observationEntity.getId());
        sessionBean.setSessionObservations(observations);
        
        return "success";
    }
}
