package bot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Main {
	
    public static void main(String[] args) throws IOException {
    	File fileman = new File("manifest.txt");
    	fileman.createNewFile();
		BufferedReader reader = new BufferedReader(new FileReader(fileman));
		String text = reader.readLine();
		reader.close();
		String[] textParts = text.split("#");
		
		// Start initialize bot
    	System.out.println("Started...");
    	// Initialize Api Context
        ApiContextInitializer.init();
        // Instantiate Telegram Bots API
        TelegramBotsApi botsApi = new TelegramBotsApi();

        // Register our bot
        try {
        	Bot bot = new Bot();
        	
        	bot.setBotToken(textParts[0]);
            bot.setBotUsername(textParts[1]);
        	botsApi.registerBot(bot);
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
        
        System.out.println("Bot successfully started!");
    }
}
