package com.moveatis.lotas.observation;

import com.moveatis.lotas.category.CategoryEntity;
import com.moveatis.lotas.devel.ObservationFileOperations;
import com.moveatis.lotas.interfaces.Category;
import com.moveatis.lotas.timezone.TimeZoneInformation;
import com.moveatis.lotas.interfaces.AbstractBean;
import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import com.moveatis.lotas.interfaces.Observation;
import com.moveatis.lotas.interfaces.Scene;
import com.moveatis.lotas.interfaces.User;
import com.moveatis.lotas.records.RecordEntity;
import com.moveatis.lotas.user.UserEntity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sami Kallio <phinaliumz at outlook.com>
 */
@Stateful
public class ObservationBean extends AbstractBean<ObservationEntity> implements Observation, Serializable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationBean.class);
    
    private static final long serialVersionUID = 1L;
    
    /*
    * For development phase we use local file, not database
    */
    private ObservationFileOperations develFileOperations;

    @PersistenceContext(unitName = "LOTAS_PERSISTENCE")
    private EntityManager em;
    
    @EJB
    private Category categoryEJB;
    
    @EJB
    private Scene sceneEJB;
    
    @EJB
    private User userEJB;
    
    private GregorianCalendar calendar;
    
    private ObservationEntity observation;
    private UserEntity user;
    

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ObservationBean() {
        super(ObservationEntity.class);
    }
    
    @PostConstruct
    public void initialize() {
        calendar = (GregorianCalendar) Calendar.getInstance(TimeZoneInformation.getTimeZone());
        develFileOperations = new ObservationFileOperations();
        
        //For devel-phase there is just one user
        user = userEJB.find(1L);
        if(user == null) {
            user = new UserEntity();
            userEJB.create(user);
        }
    }

    @Override
    public void categorizedObservationActivated(String categoryLabel) {
        ObservationEntity observationEntity = new ObservationEntity();
        
        observationEntity.setCreated((Date) calendar.getTime());
        
        CategoryEntity category = categoryEJB.find(categoryLabel);
        if(category == null) {
            category = new CategoryEntity();
            category.setLabel(categoryLabel);
            category.setCreated((Date) calendar.getTime());
            category.setScene(null);
        }
    }

    @Override
    public void categorizedObservationDeactivated(String category) {
        
    }

    @Override
    public void addRecord(RecordEntity recordEntity) {
        if(this.observation == null) {
            observation = new ObservationEntity();
            observation.setCreated(Calendar.getInstance(TimeZoneInformation.getTimeZone()).getTime());
            List<RecordEntity> records = new ArrayList<>();
            observation.setRecords(records);
            observation.setUser(user);
        }
        this.observation.getRecords().add(recordEntity);
        develFileOperations.write(observation);
    }

    @Override
    public List<RecordEntity> getRecords(Long observationId) {
        return develFileOperations.read().getRecords();
    }
}
