package chat;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ChatClientThread implements Runnable {
    private volatile boolean exit = false;
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
        while(!exit){
            read();
        }
    }

    private void read() {
        try {
            if(newlyConnected){
                while(!(receivedMessage = reader.readLine()).trim().equals("#")){

                    if(receivedMessage.equals("")){
                        System.out.println("LOGIN ZAJETY!");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                               Alert alert = new Alert(Alert.AlertType.INFORMATION);
                               alert.setTitle("Nickname already used");
                               alert.setHeaderText(null);
                               alert.setContentText("Please try choosing another nickname");

                               alert.showAndWait();
                               System.exit(0);
                            }
                        });
//                        ChatPageController.getInstance().getChatPane().getItems().set(0,"NICKNAME IN USE");
//                        ChatPageController.getInstance().getChatPane().getItems().set(0,"NICKNAME IN USE");

                    }


                    System.out.println("Received message: " + receivedMessage + " " +
                            "length: " + receivedMessage.trim().length());

                    ChatPageController.getInstance().getUsers().add(receivedMessage.trim());

                }
                System.out.println("I received stopReadingUsers character: " + receivedMessage );
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ChatPageController.getInstance().getUserList().getItems().setAll(ChatPageController.getInstance().getUsers());
                        ChatPageController.getInstance().getUserList().getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                        System.out.println("We did it");
                    }
                });
                newlyConnected = false;
            }



            if ((receivedMessage = reader.readLine()) != null) {
                if(receivedMessage.trim().equals("#")){
                    System.out.println("Odbieram login uzytkownika\n");
                    //If server sends "#" character, it means that next message to send is new Clients nickname;
                    ChatPageController.getInstance().getUsers().add(reader.readLine().trim());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            ChatPageController.getInstance().getUserList().getItems().setAll(ChatPageController.getInstance().getUsers());
//                            ChatPageController.getInstance().getUserList().getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                        }
                    });

                }else if(receivedMessage.trim().equals("#!#")){
                    //jesli otrzymales sekwencje mowiaca o tym ze uzytkownik sie wylogowuje to go usun z listy uzytkownikow
                    receivedMessage = reader.readLine().trim();
                    String[] splittedMessage;
                    splittedMessage = receivedMessage.split(":");
                    int index = ChatPageController.getInstance().getUsers().indexOf(splittedMessage[0]);
                    System.out.println(splittedMessage[0] + " is disconnecting. Deleting index " + index);
                    ChatPageController.getInstance().getUsers().remove(index);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("Updating the view!");
                            if(ChatPageController.getInstance().getWhoToSend().equals(splittedMessage[0])){
                                ChatPageController.getInstance().getChatPane().getItems().clear();
                            }
                            ChatPageController.getInstance().getUserList().getItems().clear();
                            ChatPageController.getInstance().getUserList().getItems().setAll(ChatPageController.getInstance().getUsers());
//                            ChatPageController.getInstance().getUserList().getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                        }
                    });





                }else{
                    System.out.println(receivedMessage.trim());
                    ChatPageController.getInstance().getMessages().add(receivedMessage.trim());

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            ChatPageController.getInstance().setAppropriateMessagesInWindow();
//                            ChatPageController.getInstance().getChatPane().getItems().setAll(ChatPageController.getInstance().getMessages());
//                            ChatPageController.getInstance().getChatPane().getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

                        }
                    });
                }

            }

        } catch (IOException e) {
            System.out.println("Reading data from server went wrong: " + e.getMessage());
        }
    }

    public void stop() throws IOException {
        reader.close();
        exit = true;
    }


}

