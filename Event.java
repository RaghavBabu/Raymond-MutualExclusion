import java.io.Serializable;


public class Event implements Serializable{

	private static final long serialVersionUID = -2429413363275956413L;
	EventType eventType;
	int processId;
	boolean returnToken;
	
	//to ping,request token.
	public Event(EventType eventType, int processId, boolean returnToken) {
		this.eventType = eventType;
		this.processId = processId;
		this.returnToken = returnToken;
	}

	@Override
	public String toString() {
		return "Event [eventType=" + eventType + ", processId=" + processId
				 + "]";
	}

}
