package crm;

import jakarta.xml.bind.annotation.*;

import javax.xml.bind.annotation.XmlElement;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Participant {
    @XmlAttribute
    private int id;

    @XmlElement
    private String method;

    @XmlElement
    private String firstname;

    @XmlElement
    private String lastname;

    @XmlElement
    private String email;

    @XmlElement
    private String phone;

    @XmlElement
    private String business;
    @XmlElement(name = "date_of_birth")
    private Date dateOfBirth;
    @XmlAttribute( name = "uuid")
    private String uuid;

    @XmlAttribute(name = "fromBusiness")
    private String fromBusiness;

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFromBusiness() {
        return fromBusiness;
    }

    public void setFromBusiness(String fromBusiness) {
        this.fromBusiness = fromBusiness;
    }

    public Participant() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }


}
