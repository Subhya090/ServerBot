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
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot {
	public int uploadFlag = 0;
	public String token;
	private String botName;
	public String upPath = "";
	String path = "";
	String command = "";
	String help = "Command:\n/getFile <full path> - get file to path\n/getIP - get server IP adress\n"
			+ "/getlog - log menu\n/execute <command> - execute command\n/info - get server info\n"
			+ "/uploadFile <full path> - upload file to server to path, path example: /home/user/ \n/help - command menu";
	BotMethod bm = BotMethod.getInstance();
	@Override
	public void onUpdateReceived(Update update) {
		
		if (update.hasMessage() && update.getMessage().hasText()){
			String user_first_name = update.getMessage().getChat().getFirstName();
			String user_username = update.getMessage().getChat().getUserName();
			long user_id = update.getMessage().getChat().getId();
			String message_text = update.getMessage().getText().trim();
			long chat_id = update.getMessage().getChatId();
			
        
			if(bm.isAdmin(user_username)) {

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
					bm.log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/help":	
					sendInline(help,chat_id);
					bm.log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/info":	
					sendInfoMessage(chat_id);
					bm.log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/getlog":
					String logmenu = "logs:\n1)/syslog (/var/log/syslog) - System log\n2)/authlog (/var/log/auth.log) - authorization log"
							+ "\n3)/faillog (/var/log/faillog) - fail log\n4) /botlog - log Bot";
					sendMessage(logmenu, chat_id);
					bm.log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/getIP":
					sendMessage(bm.getIp(),chat_id);
					bm.log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/executel":
					try {
						String result = bm.executeCom(command);
						sendMessage("execute!\nOutput>>\n"+ result,chat_id);
						bm.log(user_first_name, user_username, Long.toString(user_id), "/execute" + " " + command);
					} catch (IOException | InterruptedException e) {
						sendMessage("Error!",chat_id);
						e.printStackTrace();
					}
					break;
				
				case "/getFilel":
					sendFile(path, chat_id, "file to path <" + path + ">");
					bm.log(user_first_name, user_username, Long.toString(user_id), "/getFile" + " " + path);
					break;
				case "/uploadFilel":
					sendMessage("Send file",chat_id);
					uploadFlag = 1;
					bm.log(user_first_name, user_username, Long.toString(user_id), "/uploadFile" + " " + upPath);
					break;
				case "/syslog":
					sendFile("/var/log/syslog",chat_id,"#Syslog");
					bm.log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/authlog":
					sendFile("/var/log/auth.log",chat_id,"#Authlog");
					bm.log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/faillog":
					sendFile("/var/log/faillog",chat_id,"#Faillog");
					bm.log(user_first_name, user_username, Long.toString(user_id), message_text);
					break;
				case "/botlog":
					sendFile("log.txt",chat_id,"#Botlog");
					bm.log(user_first_name, user_username, Long.toString(user_id), message_text);
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
        	  editInfoMessage(chat_id, mes_id);
        	  bm.log("CalbackQuery command chat ", "call_data = " + call_data, Long.toString(chat_id), "");
          }
          if(call_data.equals("reboot")) {
        	  try {
        		  sendMessage("#reboot", chat_id);
        		  bm.executeCom("reboot");
      			  bm.log("CalbackQuery command chat ", "call_data = " + call_data, Long.toString(chat_id), "");
      		  } catch(Exception e){
      			  e.printStackTrace();
      		  }
          }
          if(call_data.equals("off")){
        	  try {
        		  sendMessage("#off", chat_id);
        		  bm.executeCom("shutdown -h now");
        		  bm.log("CalbackQuery command chat ", "call_data = " + call_data, Long.toString(chat_id), "");
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
	        bm.log(user_username, " upload file ", file_name, " to " + upPath);
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
    
    public void sendInfoMessage(long chat_id){
    	
		List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
	    List<InlineKeyboardButton> buttons1 = new ArrayList<>();
	    buttons1.add(new InlineKeyboardButton().setText("update").setCallbackData("update"));
	    buttons.add(buttons1);
	    InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
	    markupKeyboard.setKeyboard(buttons);
	    SendMessage sm = new SendMessage() 
	            .setChatId(chat_id)
	            .setText(bm.sysInfo())
	            .setReplyMarkup(markupKeyboard);
	    try {
	        execute(sm); 
	    } catch (TelegramApiException e) {
	        e.printStackTrace();
	    }
    }
    
    public void editInfoMessage(long chat_id, int mes_id){
    	
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
               .setText(bm.sysInfo());
        try {
			   execute(new_message);
		} catch (TelegramApiException e) {
			   e.printStackTrace();
	    }
    }
}

		
	

