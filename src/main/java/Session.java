import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;
@XmlRootElement
public class Session {
    @XmlElementWrapper(name = "speakers")
    @XmlElement(name = "participant")
    private List<Participant> speakers;
    @XmlElementWrapper(name = "participants")
    @XmlElement(name = "participant")
    private List<Participant> participants;

    public List<Participant> getSpeakers() {
        return speakers;
    }

    public void setSpeakers(List<Participant> speakers) {
        this.speakers = speakers;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }
}
