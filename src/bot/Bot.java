package bot;

import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Document;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Bot extends TelegramLongPollingBot {
	private int uploadFlag = 0;
	private String token;
	private String botName;
	private FileWriter fw;
	private String upPath = "";

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()){
			String user_first_name = update.getMessage().getChat().getFirstName();
			String user_username = update.getMessage().getChat().getUserName();
			long user_id = update.getMessage().getChat().getId();
			String message_text = update.getMessage().getText().trim();
			long chat_id = update.getMessage().getChatId();
			String path = "";
			String command = "";
			String help = "Command:\n/getFile <full path> - get file to path\n/getIP - get server IP adress\n"
					+ "/getLog - log menu\n/execute <command> - execute command\n/info - get server info\n"
					+ "/uploadFile <full path> - upload file to server to path, path example: /home/user/ \n/help - command menu";
        
			if(isAdmin(user_username)) {

				if(message_text.startsWith("/execute <")) {
					command = message_text.substring(10,message_text.length() - 1); 
					message_text = "/executel";
				}
				if(message_text.startsWith("/getFile <")) {
					path = message_text.substring(10,message_text.length() - 1);
					message_text = "/getFilel";
				}
				if(message_text.startsWith("/uploadFile <")) {
					upPath = message_text.substring(13,message_text.length() - 1);
					message_text = "/uploadFilel";
				}
         
				switch(message_text) {
				case "/start":	
					String hello = "Hello " + user_username + "!\n" + help;
					sendInline(hello, chat_id);
					log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/help":	
					sendInline(help,chat_id);
					log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/info":	
					List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
				    List<InlineKeyboardButton> buttons1 = new ArrayList<>();
				    buttons1.add(new InlineKeyboardButton().setText("update").setCallbackData("update"));
				    buttons.add(buttons1);
				    InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
				    markupKeyboard.setKeyboard(buttons);
				    SendMessage sm = new SendMessage() 
				            .setChatId(chat_id)
				            .setText(sysInfo())
				            .setReplyMarkup(markupKeyboard);
				    try {
				        execute(sm); 
				    } catch (TelegramApiException e) {
				        e.printStackTrace();
				    }
					log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/getLog":
					String logmenu = "Logs:\n1)/syslog (/var/log/syslog) - System log\n2)/authlog (/var/log/auth.log) - authorization log"
							+ "\n3)/faillog (/var/log/faillog) - fail log\n4) /botlog - Log Bot";
					sendMessage(logmenu, chat_id);
					log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/getIP":
					sendMessage(getIp(),chat_id);
					log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/executel":
					try {
						String result = executeCom(command);
						sendMessage("execute!\nOutput>>\n"+ result,chat_id);
						log(user_first_name, user_username, Long.toString(user_id), "/execute" + " " + command);
					} catch (IOException | InterruptedException e) {
						sendMessage("Error!",chat_id);
						e.printStackTrace();
					}
					break;
				
				case "/getFilel":
					sendFile(path, chat_id, "file to path <" + path + ">");
					log(user_first_name, user_username, Long.toString(user_id), "/getFile" + " " + path);
					break;
				case "/uploadFilel":
					sendMessage("Send file",chat_id);
					uploadFlag = 1;
					log(user_first_name, user_username, Long.toString(user_id), "/uploadFile" + " " + upPath);
					break;
				case "/syslog":
					sendFile("/var/log/syslog",chat_id,"#SysLog");
					log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/authlog":
					sendFile("/var/log/auth.log",chat_id,"#AuthLog");
					log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/faillog":
					sendFile("/var/log/faillog",chat_id,"#FailLog");
					log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/botlog":
					sendFile("log.txt",chat_id,"#BotLog");
					log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				default:
					sendMessage("unknown command",chat_id);
				}
        } else{
        	sendMessage("no admin rules",chat_id);
        }
      } else if (update.hasCallbackQuery()) {
          String call_data = update.getCallbackQuery().getData();
          long chat_id = update.getCallbackQuery().getMessage().getChatId();
          int mes_id = update.getCallbackQuery().getMessage().getMessageId();
          if(call_data.equals("update")) {
        	  List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
		      List<InlineKeyboardButton> buttons1 = new ArrayList<>();
		      buttons1.add(new InlineKeyboardButton().setText("update").setCallbackData("update"));
		      buttons.add(buttons1);
		      InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
		      markupKeyboard.setKeyboard(buttons);
		      EditMessageText new_message = new EditMessageText()
                      .setChatId(chat_id)
                      .setMessageId(mes_id)
                      .setReplyMarkup(markupKeyboard)
                      .setText(sysInfo());
              try {
				  execute(new_message);
			  } catch (TelegramApiException e) {
				e.printStackTrace();
			  }
        	  log("CalbackQuery command chat ", "call_data = " + call_data, Long.toString(chat_id), "");
          }
          if(call_data.equals("reboot")) {
        	  try {
        		  sendMessage("#reboot", chat_id);
        		  executeCom("reboot");
      			  log("CalbackQuery command chat ", "call_data = " + call_data, Long.toString(chat_id), "");
      		  } catch(Exception e){
      			  e.printStackTrace();
      		  }
          }
          if(call_data.equals("off")){
        	  try {
        		  sendMessage("#off", chat_id);
        		  executeCom("shutdown -h now");
        		  log("CalbackQuery command chat ", "call_data = " + call_data, Long.toString(chat_id), "");
      		  } catch(Exception e){
      			e.printStackTrace();
      		  }
          }
       }else if(update.getMessage().hasDocument() && uploadFlag == 1 && !upPath.isEmpty()){
    	  Document doc = update.getMessage().getDocument();
    	  String file_name = doc.getFileName();
    	  String file_id = doc.getFileId();
    	  long chat_id = update.getMessage().getChatId();
    	  String user_username = update.getMessage().getChat().getUserName();
    	  try {
  			uploadFile(file_name, file_id);
	        sendMessage("#uploaded file " + file_name + " to " + upPath, chat_id);
	        log(user_username, " upload file ", file_name, " to " + upPath);
	      }catch(IOException e)
	      {
	         e.printStackTrace();
	      }
  		  
       }	

    }

    public void setBotUsername(String name) {
    	this.botName = name;
    }
    
    public void setBotToken(String token) {
    	this.token = token;
    }
   
    public void sendInline(String text, long chat_id) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        buttons1.add(new InlineKeyboardButton().setText("reboot").setCallbackData("reboot"));
        buttons1.add(new InlineKeyboardButton().setText("off").setCallbackData("off"));
        buttons.add(buttons1);
        

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        SendMessage sm = new SendMessage() 
                .setChatId(chat_id)
                .setText(text)
                .setReplyMarkup(markupKeyboard);
    	try {
            execute(sm); 
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return token;
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
    
    public void sendMessage(String message, long chat_id) {
    	SendMessage sm = new SendMessage() 
                .setChatId(chat_id)
                .setText(message);
    	try {
            execute(sm); 
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    public void sendFile(String patch, long chat_id, String caption) {
    	File file = new File(patch);
    	SendDocument sd = new SendDocument()
    		.setChatId(chat_id)
    		.setCaption(caption)
    		.setNewDocument(file);
    	try{
    		sendDocument(sd);
    	} catch(TelegramApiException e){
    		sendMessage("Error!",chat_id);
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
			cpu = "CPU load Now - " + (Double.parseDouble(textParts[0]))*100 + "%\nAverage CPU load 5 min - "
					 + (Double.parseDouble(textParts[1]))*100 + "%\nAverage CPU load 15 min - "
					 + (Double.parseDouble(textParts[2]))*100 + "%\n";
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
			URL whatismyip = new URL("http://checkip.amazonaws.com"); 
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
            BufferedReader stdInput = new BufferedReader(new 
                    InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new 
                    InputStreamReader(proc.getErrorStream()));

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
    
    public void uploadFile(String file_name, String file_id) throws IOException{
		URL url = new URL("https://api.telegram.org/bot"+token+"/getFile?file_id="+file_id);
		BufferedReader in = new BufferedReader(new InputStreamReader( url.openStream())); 
		String res = in.readLine();
		JSONObject jresult = new JSONObject(res);
		JSONObject path = jresult.getJSONObject("result");
		String file_path = path.getString("file_path");
		URL downoload = new URL("https://api.telegram.org/file/bot" + token + "/" + file_path);
		FileOutputStream fos = new FileOutputStream(upPath + file_name);
		System.out.println("Start upload");
        ReadableByteChannel rbc = Channels.newChannel(downoload.openStream());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
        uploadFlag = 0;
        System.out.println("Uploaded!");
    }
}

		
	

