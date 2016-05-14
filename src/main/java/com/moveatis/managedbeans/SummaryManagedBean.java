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

import com.moveatis.export.CSVFileBuilder;
import com.moveatis.interfaces.Mailer;
import com.moveatis.interfaces.MessageBundle;
import com.moveatis.interfaces.Observation;
import com.moveatis.interfaces.Session;
import com.moveatis.observation.ObservationCategory;
import com.moveatis.observation.ObservationCategorySet;
import com.moveatis.observation.ObservationEntity;
import com.moveatis.records.RecordEntity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.extensions.model.timeline.TimelineEvent;
import org.primefaces.extensions.model.timeline.TimelineModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.faces.view.ViewScoped;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.extensions.model.timeline.TimelineGroup;

/**
 * Class for Summary page managed bean. Responsive for creating summary page
 * timeline model and get required variables from observation.
 *
 * @author Juha Moisio <juha.pa.moisio at student.jyu.fi>
 */
@Named(value = "summaryBean")
@ViewScoped
public class SummaryManagedBean implements Serializable {

    private TimelineModel timeline;
    private final Date min;
    private final Date start;
    private final long zoomMin;
    private final long zoomMax;
    private Date max;
    private Date duration;
    private String recipientEmail;

    private ObservationEntity observation;

    private List<String> selectedSaveOptions;

    private static final String MAIL_OPTION = "mail";
    private static final String SAVE_OPTION = "save";
    private static final String DOWNLOAD_OPTION = "download";

    private boolean observationSaved = false;

    @Inject
    private Observation observationEJB;

    @Inject
    private Session sessionBean;
    @Inject
    private ObservationManagedBean observationManagedBean;
    @Inject
    private Mailer mailerEJB;

    @Inject
    private ValidationManagedBean validationBean;

    @Inject
    @MessageBundle
    private transient ResourceBundle messages;

    private static final Logger LOGGER = LoggerFactory.getLogger(SummaryManagedBean.class);

    /**
     * Default constructor to initialize timeline options.
     */
    public SummaryManagedBean() {
        this.start = new Date(0);
        this.min = new Date(0);
        this.max = new Date(0);
        this.zoomMin = 10 * 1000;
        this.zoomMax = 24 * 60 * 60 * 1000;
        this.selectedSaveOptions = new ArrayList<>();
    }

    /**
     * Post constructor to create timeline on request.
     */
    @PostConstruct
    protected void initialize() {
        createTimeline();
    }

    /**
     * Create timeline model. Add category groups as timeline event groups and
     * records as timeline events.
     */
    private void createTimeline() {
        timeline = new TimelineModel();

        observation = observationManagedBean.getObservationEntity();
        List<RecordEntity> records = observation.getRecords();

        duration = new Date(observation.getDuration());
        max = new Date(Math.round(this.observation.getDuration() * 1.1)); // timeline max 110% of obs. duration

        // Add categories to timeline as timeline groups
        int categoryNumber = 1;
        for (ObservationCategorySet categorySet : observationManagedBean.getCategorySetsInUse()) {
            for (ObservationCategory category : categorySet.getCategories()) {
                // Add category name inside element with class name
                // use css style to hide them in timeline
                // class name is intentionally without quotes, timeline expectional case
                String numberedLabel
                        = "<span class=categoryNumber>" + categoryNumber + ". </span>"
                        + "<span class=categoryLabel>" + StringEscapeUtils.escapeHtml4(category.getName()) + "</span>"
                        + "<span class=categorySet>" + StringEscapeUtils.escapeHtml4(categorySet.getName()) + "</span>";
                String groupID = Long.toString(category.getTag());
                TimelineGroup timelineGroup = new TimelineGroup(groupID, numberedLabel);
                timeline.addGroup(timelineGroup);
                // Add dummy records to show empty categories in timeline
                TimelineEvent timelineEvent = new TimelineEvent("", new Date(0), false, groupID, "dummyRecord");
                timeline.add(timelineEvent);
                categoryNumber++;
            }
        }

        // Add records to timeline as timeline-events
        for (RecordEntity record : records) {
            ObservationCategory category = record.getCategory();
            long startTime = record.getStartTime();
            long endTime = record.getEndTime();
            TimelineEvent timelineEvent = new TimelineEvent("", new Date(startTime),
                    new Date(endTime), false, Long.toString(category.getTag()));
            timeline.add(timelineEvent);
        }
    }

    public void showObservationSavedMessage() {
        if (observationSaved) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("sum_observationSaved"), ""));
            observationSaved = false;
        }
    }

    public void saveCurrentObservation() {
        observationManagedBean.saveObservationToDatabase();
        observationSaved = true;
    }

    public void mailCurrentObservation() {
        CSVFileBuilder csv = new CSVFileBuilder();
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
        try {
            String fileName = this.observation.getName();
            // replace non-word ![a-ZA-Z_0-9] chars with underscope
            fileName = fileName.replaceAll("\\W", "_");
            File f = File.createTempFile(fileName, ".csv");
            
            StringBuilder msgBuilder = new StringBuilder();
            String description = StringUtils.defaultIfEmpty(observation.getDescription(), 
                    bundle.getString("sum_descriptionNotSet"));
            String target = StringUtils.defaultIfEmpty(observation.getTarget(), 
                    bundle.getString("sum_targetNotSet"));
            String descriptionPartOfMessage = MessageFormat.format(bundle.getString("sum_descriptionWas"), description);
            String targetPartOfMessage = MessageFormat.format(bundle.getString("sum_targetWas"), target);
            String messageWithSender = MessageFormat.format(bundle.getString("sum_message"), 
                    sessionBean.getLoggedIdentifiedUser().getGivenName());
            
            msgBuilder
                    .append(messageWithSender)
                    .append("\n\n")
                    .append(descriptionPartOfMessage)
                    .append("\n\n")
                    .append(targetPartOfMessage)
                    .append("\n\n")
                    .append(bundle.getString("emailSignature"));
            
            
            FileOutputStream fos = new FileOutputStream(f);
            csv.buildCSV(fos, observation, ",");
            fos.flush();
            String[] recipients = {recipientEmail};
            File[] files = {f};
            mailerEJB.sendEmailWithAttachment(recipients, bundle.getString("sum_subject"),
                    msgBuilder.toString(), files);
            //remove the temp file after sending it
            f.delete();
        } catch (IOException ex) {
            LOGGER.error("Väliaikaisen tiedoston luonti epäonnistui", ex);
        }
        observationSaved = true;
    }

    private static String convertToFilename(String s) {
        if (s == null || s.isEmpty()) {
            return "unnamed";
        }
        return s.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    public void downloadCurrentObservation() throws IOException {
        String fileName = convertToFilename(observation.getName()) + ".csv";

        FacesContext facesCtx = FacesContext.getCurrentInstance();
        ExternalContext externalCtx = facesCtx.getExternalContext();

        externalCtx.responseReset();
        externalCtx.setResponseContentType("text/plain");
        externalCtx.setResponseHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        OutputStream outputStream = externalCtx.getResponseOutputStream();

        CSVFileBuilder csv = new CSVFileBuilder();
        csv.buildCSV(outputStream, observation, ",");
        outputStream.flush();

        facesCtx.responseComplete();

        observationSaved = true;
    }

    public void doSelectedSaveOperation() {
        if (selectedSaveOptions.contains(DOWNLOAD_OPTION)) {
            try {
                downloadCurrentObservation();
            } catch (IOException e) {
                //TODO: show error message
            }
        }
        if (selectedSaveOptions.contains(MAIL_OPTION)) {
            mailCurrentObservation();
        }
        if (selectedSaveOptions.contains(SAVE_OPTION)) {
            saveCurrentObservation();
        }
    }

    /*
       Getters and setters
     */
    /**
     * Get timeline model.
     *
     * @return TimelineModel
     */
    public TimelineModel getTimeline() {
        return timeline;
    }

    /**
     * Get min date of timeline. User cannot move the timeline before that date.
     *
     * @return Date
     */
    public Date getMin() {
        return min;
    }

    /**
     * Get maximum date of timeline. User cannot move the timeline after that
     * date.
     *
     * @return Date
     */
    public Date getMax() {
        return max;
    }

    public Date getDuration() {
        return duration;
    }

    /**
     * Get timeline start date.
     *
     * @return Date
     */
    public Date getStart() {
        return start;
    }

    /**
     * Get minimum zoom interval for timeline in milliseconds.
     *
     * @return long
     */
    public long getZoomMin() {
        return zoomMin;
    }

    /**
     * Get maximum zoom interval for timeline in milliseconds.
     *
     * @return long
     */
    public long getZoomMax() {
        return zoomMax;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public ObservationEntity getObservation() {
        return observation;
    }

    public void setObservation(ObservationEntity observation) {
        this.observation = observation;
    }

    public List<String> getSelectedSaveOptions() {
        return selectedSaveOptions;
    }

    public void setSelectedSaveOptions(List<String> selectedSaveOptions) {
        this.selectedSaveOptions = selectedSaveOptions;
    }

    public boolean getMailOptionChecked() {
        return selectedSaveOptions.contains(MAIL_OPTION);
    }

    public boolean isObservationSaved() {
        return observationSaved;
    }

    public void setObservationSaved(boolean observationSaved) {
        this.observationSaved = observationSaved;
    }
}
