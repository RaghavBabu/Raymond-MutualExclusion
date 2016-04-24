import java.util.Map.Entry;

/**
 * Class Utils.
 * Utility methods for the application.
 * @author Raghav Babu
 * Date : 04/14/2016
 */

public class Utils {

	static int arr[] = new int[Process.totalProcess];

	/*
	 * create an array using the number of processes.
	 * The array represnts a K-ary tree.
	 */
	public static void constructNaryTree() {

		int i = 0;
		for(Entry<Integer, String> e : ProcessIPPortXmlParser.processIDToIpMap.entrySet() ){
			arr[i] = e.getKey();
			i++;
		}
	}

	/*
	 * Given a processId and childPos(k) as input, get its k-th childprocess Id.
	 * returns parent.
	 */
	public static int getChildProcessId(int processId, int childPos){

		int processIdPos  = -1;

		for(int i = 0; i < arr.length; i++){

			if(arr[i] == processId)
				processIdPos = i;
		}

		return arr[(Process.childsInTree * processIdPos) + childPos + 1];
	}

	/*
	 * Given a processId as input, get its parent Node Id.
	 * returns parent.
	 */
	public static int getParentProcessId(int processId){

		int processIdPos = 0;

		for(int i = 0; i < arr.length; i++){

			if(arr[i] == processId){
				processIdPos = i;
			}
		}
		
		int val = (int)(Math.floor( (processIdPos - 1)/Process.childsInTree) );
		return arr[val];
	}
	
	/*
	 * return root process id fron K-ary tree.
	 */
	public static int getRootProcessId(){
		return arr[0];
	}




}
