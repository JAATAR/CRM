package crm;

import jakarta.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Business {
    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "VAT")
    private String vat;

    @XmlElement(name = "email")
    private String email;

    @XmlElement(name = "access_code")
    private String accessCode;

    @XmlElement(name = "address")
    private String address;

    @XmlElement(name = "service")
    private String service;

    @XmlElement(name = "method")
    private String method;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @XmlElement(name = "uuid")
    private String uuid;

    public Business() {
        this.service = "frontend";
        this.method = "create";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVat() {
        return vat;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
