package com.moveatis.lotas.session;

import com.moveatis.lotas.enums.UserType;
import com.moveatis.lotas.timers.AnonymityTimerSessionBean;
import com.moveatis.lotas.user.UserEntity;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sami Kallio <phinaliumz at outlook.com>
 */
@ManagedBean(name="sessionBean")
@SessionScoped
public class SessionBean implements Serializable  {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionBean.class);

    private UserType userType;
    private String tag;
    private UserEntity userEntity;
    
    private Boolean loggedIn = false;
    
    @Inject
    private AnonymityTimerSessionBean anonymityTimer;
    
    public SessionBean() {
        
    }

    
    public void setIdentifiedUser() {
        userType = UserType.IDENTIFIED_USER;
    }
    
    public void setAnonymityUser() {
        userType = UserType.ANONYMITY_USER;
        
    }
    
    public void setTagUser(String tag) {
        userType = UserType.TAG_USER;
        anonymityTimer.setTimer();
        this.loggedIn = true;
        this.tag = tag;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserEntity(UserEntity user) {
        this.userEntity = user;
    }

    public Boolean isLoggedIn() {
        return loggedIn;
    }

    public void setIsLoggedIn(Boolean isLoggedIn) {
        this.loggedIn = isLoggedIn;
    }
        
}
