package crm;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Service {
    @XmlAttribute(name = "name")
    private String name = "crm";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
