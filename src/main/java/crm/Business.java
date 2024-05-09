package crm;

import jakarta.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Business {
    @XmlAttribute
    private int id;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "vat")
    private String vat;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVat() {
        return vat;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


