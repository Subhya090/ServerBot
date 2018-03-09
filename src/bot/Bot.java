package bot;

import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Bot extends TelegramLongPollingBot {
	private String token;
	private String botName;
	private FileWriter fw;
	

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String user_first_name = update.getMessage().getChat().getFirstName();
			String user_username = update.getMessage().getChat().getUserName();
			long user_id = update.getMessage().getChat().getId();
			String message_text = update.getMessage().getText();
			long chat_id = update.getMessage().getChatId();
			String patch = "";
			String comand = "";
			String help = "Comand:\n/getFile <full patch> - get file to patch\n/getIP - get server IP adress\n"
					+ "/getLog - log menu\n/execute <comand> - execute comand\n/help - comand menu";
        
			if(isAdmin(user_username)){

				if(message_text.startsWith("/execute <")) {
					comand = message_text.substring(10,message_text.length() - 1); 
					message_text = "/executel";
				}
				if(message_text.startsWith("/getFile <")) {
					patch = message_text.substring(10,message_text.length() - 1);
					message_text = "/getFilel";
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
				case "/getLog":
					String logmenu = "Logs:\n1)/syslog (/var/log/syslog) - System log\n2)/authlog (/var/log/auth.log) - authorization log"
							+ "\n3)/faillog (/var/log/faillog) - fail log\n4) /botlog - Log Bot";
					sendMessage(logmenu, chat_id);
					log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/getIP":
					try{
						URL whatismyip = new URL("http://checkip.amazonaws.com"); 
						BufferedReader in = new BufferedReader(new InputStreamReader( whatismyip.openStream())); 
						String ip = in.readLine();
						sendMessage(ip,chat_id);
						log(user_first_name, user_username, Long.toString(user_id), message_text);
						break;
					} catch(Exception e){
            			e.printStackTrace();
					}  
				case "/executel":
					try{
						Process proc = Runtime.getRuntime().exec(comand);
						Thread.sleep(100);
						proc.destroy();
						sendMessage("execute!",chat_id);
						log(user_first_name, user_username, Long.toString(user_id), "/execute" + " " + comand);
						break;
					} catch(Exception e){
						e.printStackTrace();
					}
				case "/getFilel":
					sendFile(patch, chat_id, "file to patch <" + patch + ">");
					log(user_first_name, user_username, Long.toString(user_id), "/getFile" + " " + patch);
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
					sendMessage("unknown comand",chat_id);
				}
        } else{
        	sendMessage("no admin rules",chat_id);
        }
      } else if (update.hasCallbackQuery()) {
          String call_data = update.getCallbackQuery().getData();
          long chat_id = update.getCallbackQuery().getMessage().getChatId();
          if(call_data.equals("reboot")) {
        	  try {
        		  sendMessage("#reboot", chat_id);
        		  Process proc = Runtime.getRuntime().exec("reboot");
      			  Thread.sleep(100);
      			  proc.destroy();
      			  log("CalbackQuery comand chat ", "call_data = " + call_data, Long.toString(chat_id), "");
      		  } catch(Exception e){
      			  e.printStackTrace();
      		  }
          }
          if(call_data.equals("off")){
        	  try {
        		  sendMessage("#off", chat_id);
        		  Process proc = Runtime.getRuntime().exec("shutdown -h now");
        		  Thread.sleep(100);
        		  proc.destroy();
        		  log("CalbackQuery comand chat ", "call_data = " + call_data, Long.toString(chat_id), "");
      		  } catch(Exception e){
      			e.printStackTrace();
      		  }
          }
       }
    }

    public void setBotUsername(String name){
    	this.botName = name;
    }
    
    public void setBotToken(String token){
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
    
    private void log(String first_name, String user_username, String user_id, String txt) {
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
    
    public void sendMessage(String message, long chat_id){
    	SendMessage sm = new SendMessage() 
                .setChatId(chat_id)
                .setText(message);
    	try {
            execute(sm); 
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    public void sendFile(String patch, long chat_id, String caption){
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
}

		
	

