package bot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BotMethod {
	private FileWriter fw;
	private static BotMethod botMethodInstance = new BotMethod();
	
	private BotMethod() {}
	
	public static BotMethod getInstance() {
		return botMethodInstance;
	}
	
    public String sysInfo() {
    	String info = "";
    	String memory = "";
    	String cpu = "";
    	String ram = "";
    	long memorySize = (new File("/").getTotalSpace());
		memory = "Free memory on Disk: " + memorySize/1024/1024 + " MB\n";
		try {
			BufferedReader reader = new BufferedReader(new FileReader("/proc/loadavg"));
			String text = reader.readLine();
			reader.close();
			String[] textParts = text.split(" ");
			cpu = "CPU load Now - " + (Float.parseFloat(textParts[0]))*100 + "%\nAverage CPU load 5 min - "
					 + (Float.parseFloat(textParts[1]))*100 + "%\nAverage CPU load 15 min - "
					 + (Float.parseFloat(textParts[2]))*100 + "%\n";
			BufferedReader readerMem = new BufferedReader(new FileReader("/proc/meminfo"));
			String memTotal = readerMem.readLine();
			String memFree = readerMem.readLine();
			ram = memTotal + "\n" + memFree;
			readerMem.close();
		} catch(Exception e){
			e.printStackTrace();
		}
		info = memory + cpu + ram;
		return info;
    }
	
    public String getIp() {
    	String ip = "ping error";
		try{
			URL whatismyip = new URL("http://checkip.amazonaws.com"); //re
			BufferedReader in = new BufferedReader(new InputStreamReader( whatismyip.openStream())); 
			ip = in.readLine();
		} catch(Exception e){
			e.printStackTrace();
		}
		return ip;
    }
	
    public String executeCom(String command) throws IOException, InterruptedException{
    	StringBuilder sb = new StringBuilder();
        String[] commands = new String[]{"/bin/sh","-c", command};
        try {
            Process proc = new ProcessBuilder(commands).start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }

            while ((s = stdError.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }
            Thread.sleep(200);
            proc.destroy();
        } catch (IOException e) {
        	e.printStackTrace();
        	return "ERROR!";
        }
        return sb.toString();
    }
	
    public void log(String first_name, String user_username, String user_id, String txt) {
    	try {
			fw = new FileWriter("log.txt",true);
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			
			fw.write(dateFormat.format(date));
			fw.write(" Message from " + first_name + " " + user_username + ". (id = " + user_id + ") " + txt+ "\r\n");
			fw.close();
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public boolean isAdmin(String user) {
		String text;
		int k = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader("manifest.txt"));
			text = reader.readLine();
			reader.close();
			String[] textParts = text.split("#");
			
			for(int i = 0; i < textParts.length; i++) {
				if(user.equals(textParts[i])) {
					k++;
				}
			}
		} catch (IOException e){
			e.printStackTrace();
		}
		
		if(k > 0) {
			return true;
		} else{
			return false;
		}	
	}
}
