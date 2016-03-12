package com.moveatis.lotas.restful;

import com.moveatis.lotas.enums.UserType;
import com.moveatis.lotas.interfaces.Session;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sami Kallio <phinaliumz at outlook.com>
 */
@Path("/records")
@Named(value="recordBean")
@Stateful
public class RecordListenerBean implements Serializable {
    
    private final Logger logger = LoggerFactory.getLogger(RecordListenerBean.class);
    private static final long serialVersionUID = 1L;
    
    @EJB(beanName="DebugSessionBean")
    private Session sessionBean;
    
    public RecordListenerBean() {
        logger.debug("Constructor");
    }
    
    @POST
    @Path("addrecord")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public void addRecord(String firstName, String lastName) {
        logger.debug("RECORDBEAN POST");
        logger.debug("Data -> " + firstName + ", " + lastName);
        System.out.println("Data -> " + firstName + ", " + lastName);
        System.out.println("RECORDBEAN POST");
        //return "ok";
    }
    
    @POST
    @Path("updaterecord")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String updateRecord() {
        logger.debug("RECORDBEAN PUT");
        return "ok";
    }
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN) 
    public void testi() {
        logger.debug("Testi");
    }
    
    //For debug purposes
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String helloWorld() {
        
        logger.debug("RECORDBEAN GET");
        
        if(sessionBean.getUserType() == UserType.IDENTIFIED_USER) {
            return "You are identified, output will be saved to database";
        } else {
            return "You are not identified, output will not be saved to database";
        }
    }
}
