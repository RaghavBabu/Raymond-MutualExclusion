import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Class ProcessClient
 * Each Process sends event or state to another process running in different machine or same machine based 
 * on configurations in XML file on request from Process Thread.
 * @author Raghav Babu
 * Date : 04/15/2016
 */
public class ProcessClient{

	Event event;
	int toProcessId;
	String toIPAddress = null;
	int  toPort;

	public ProcessClient(Event event, int toProcessId) {
		this.event = event;
		this.toProcessId = toProcessId;
		this.toIPAddress = ProcessIPPortXmlParser.processIDToIpMap.get(toProcessId);
		this.toPort = ProcessIPPortXmlParser.processIDToPortMap.get(toProcessId);
	}


	public boolean send() {

		try {

			Socket socket = null;

			try {

				try {
					socket = new Socket(toIPAddress, toPort);
				} catch (Exception e) {
					return false;
				}

				//write msg object
				OutputStream os = socket.getOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(os);

				if(event.eventType == EventType.REQUEST_TOKEN){
					System.out.println("------------------------------------------");
					System.out.println("Process : " +Process.processId+" requesting token from current holder : "+Process.currentHolder);
					oos.writeObject(event);
					System.out.println(event);
					System.out.println("------------------------------------------");
				}
				else if(event.eventType == EventType.TRANSFER_TOKEN){
					System.out.println("------------------------------------------");
					System.out.println("Sending token to process : "+toProcessId);
					oos.writeObject(event);
					System.out.println(event);
					System.out.println("New holder of the token : "+toProcessId);
					Process.currentHolder = toProcessId;
					System.out.println("------------------------------------------");
				}
				else if(event.eventType == EventType.PING){
					oos.writeObject(event);
				}


			}catch (Exception e){
				System.out.println("Exception while passing event object to  "+toIPAddress);
				e.printStackTrace();
			}
			socket.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return true;
	}
}
