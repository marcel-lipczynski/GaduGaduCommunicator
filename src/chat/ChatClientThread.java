package chat;

import chat.ChatClient;
import javafx.application.Platform;
import javafx.scene.control.SelectionMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ChatClientThread implements Runnable {

    private ChatClient chatClient;
    private BufferedReader reader;
    private String receivedMessage = null;

    //zmienna ktora posluzy do pobrania listy uzytkownikow z serwera
    private boolean newlyConnected = true;

    public ChatClientThread(ChatClient chatClient) {
        this.chatClient = chatClient;
        try {
            reader = new BufferedReader(new InputStreamReader(chatClient.getSocket().getInputStream()));
        } catch (IOException e) {
            System.out.println("Unable to read from the socket: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        while(true){
            read();
        }
    }

    private void read() {
        try {
            if(newlyConnected){
                while(!(receivedMessage = reader.readLine()).equals("#")){
                    ChatPageController.getInstance().getUsers().add(receivedMessage);
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ChatPageController.getInstance().getUserList().getItems().setAll(ChatPageController.getInstance().getUsers());
                    }
                });
                newlyConnected = false;
            }


            if ((receivedMessage = reader.readLine()) != null) {
                if(receivedMessage.equals("#")){
                    //If server sends "#" character, it means that next message to send is new Clients nickname;
                    ChatPageController.getInstance().getUsers().add(reader.readLine());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            ChatPageController.getInstance().getUserList().getItems().setAll(ChatPageController.getInstance().getUsers());
                        }
                    });

                } else{
                    System.out.println(receivedMessage);
                    ChatPageController.getInstance().getMessages().add(receivedMessage);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            ChatPageController.getInstance().getChatPane().getItems().setAll(ChatPageController.getInstance().getMessages());
                            ChatPageController.getInstance().getChatPane().getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

                        }
                    });
                }

            }

        } catch (IOException e) {
            System.out.println("Reading data from server went wrong: " + e.getMessage());
        }
    }
}

