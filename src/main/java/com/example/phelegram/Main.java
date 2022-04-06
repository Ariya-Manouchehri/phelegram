package com.example.phelegram;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Scanner;

public class Main extends Application {

    static user current_user = null;
    static chatRoom current_chat = null;
    static String[] list_of_group;
    static Node[] Node;
    public Stage second_stage;
    static ListView<Node> group_list;

    @Override
    public void start(Stage stage) throws Exception {
        second_stage = new Stage();

        FXMLLoader sign_in = new FXMLLoader(Main.class.getResource("fxml/sign_in.fxml"));
        FXMLLoader sign_up = new FXMLLoader(Main.class.getResource("fxml/sign_up.fxml"));
        FXMLLoader chat_view = new FXMLLoader(Main.class.getResource("fxml/chat_view.fxml"));
        FXMLLoader search_group = new FXMLLoader(Main.class.getResource("fxml/search_group.fxml"));
        FXMLLoader create_group = new FXMLLoader(Main.class.getResource("fxml/create_group.fxml"));


        Parent sign_in_root = sign_in.load();
        Parent sign_up_root = sign_up.load();
        Parent chat_view_root = chat_view.load();
        Parent search_group_root = search_group.load();
        Parent create_group_root = create_group.load();

        Scene sign_in_scene = new Scene(sign_in_root, 600, 400);
        Scene sign_up_scene = new Scene(sign_up_root, 600, 400);
        Scene chat_view_scene = new Scene(chat_view_root, 600, 400);
        Scene search_group_scene = new Scene(search_group_root, 250, 400);
        Scene create_group_scene = new Scene(create_group_root, 350, 450);

        stage.setTitle("Phelegram");
        stage.setResizable(false);
        stage.setScene(sign_in_scene);
        stage.show();

        DataBas.loadUsers();
        DataBas.loadLastPortNumber();
        DataBas.reWriteUsers();

        Label sign_up_button_label = (Label) sign_in_root.lookup("#sign_up");
        sign_up_button_label.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                stage.setScene(sign_up_scene);
            }
        });
        TextField sign_in_username = (TextField) sign_in_root.lookup("#username");
        PasswordField sign_in_password = (PasswordField) sign_in_root.lookup("#pasword");
        Label sign_in_error_label = (Label) sign_in_root.lookup("#label_error");
        Button sign_in_button = (Button) sign_in_root.lookup("#sign_in");
        sign_in_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                current_user = DataBas.login(sign_in_username.getText().toString().trim(), sign_in_password.getText().toString().trim());
                if (current_user == null) {
                    sign_in_error_label.setVisible(true);
                    sign_in_error_label.setText("username or password is invalid try again");
                } else {
                    stage.setScene(chat_view_scene);
                    stage.setMaximized(true);
                    load_Group(chat_view_root);
                }
            }
        });

        Button sign_up_back_button = (Button) sign_up_root.lookup("#back_button");
        sign_up_back_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                stage.setScene(sign_in_scene);
            }
        });
        TextField sign_up_username = (TextField) sign_up_root.lookup("#username");
        PasswordField sign_up_password = (PasswordField) sign_up_root.lookup("#password");
        PasswordField sign_up_password_confirm = (PasswordField) sign_up_root.lookup("#confrim_password");

        Label sign_up_error_label_username = (Label) sign_up_root.lookup("#user_name_error");
        Label sign_up_error_label_password = (Label) sign_up_root.lookup("#password_error");
        Button sign_up_button = (Button) sign_up_root.lookup("#sign_up");
        sign_up_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                sign_up_error_label_username.setText("");
                sign_up_error_label_password.setText("");
                if (checkUsername(sign_up_username.getText().toString().trim(), sign_up_error_label_username)) {
                    if (checkPassword(sign_up_password.getText().toString().trim(), sign_up_password_confirm.getText().toString().trim(), sign_up_error_label_password)) {
                        current_user = new user(sign_up_username.getText().toString().trim(), sign_up_password.getText().toString().trim(), sign_up_username.getText().toString().trim() + sign_up_password.getText().toString().trim());
                        DataBas.signUp(current_user);

                        stage.setScene(chat_view_scene);
                        stage.setMaximized(true);
                        load_Group(chat_view_root);
                    }
                }
            }
        });
        Button search_button = (Button) chat_view_root.lookup("#search_group");
        search_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                second_stage.setScene(search_group_scene);
                second_stage.show();
            }
        });
        TextField serach_label = (TextField) search_group_root.lookup("#serach_label");
        Button serach_button_group_search = (Button) search_group_root.lookup("#serach_button");
        Label group_search_filed = (Label) search_group_root.lookup("#textfiled");
        final chatRoom[] c = new chatRoom[1];
        serach_button_group_search.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(!serach_label.getText().toString().trim().equals("")){
                    group_search_filed.setVisible(true);
                    group_search_filed.setText("");
                }else {
                    group_search_filed.setVisible(false);
                }
                c[0] = DataBas.loadChat(serach_label.getText().toString().trim());
                if (c[0] != null) {
                    group_search_filed.setText(c[0].getName());
                }
            }
        });
        group_search_filed.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (current_user.hasChat(c[0].getName())) {
                    System.out.println("you are already joined in this chat.");
                } else {
                    Alert Join_group = new Alert(Alert.AlertType.INFORMATION);
                    Join_group.setX(600);
                    Join_group.setY(400);
                    Join_group.setTitle("joined the group");
                    Join_group.setHeaderText("");
                    Join_group.setContentText("chat room '" + c[0].getName() + "' founded" + "\n" + "are you sure to join this group?");
                    Join_group.getButtonTypes().clear();
                    Join_group.getButtonTypes().add(ButtonType.YES);
                    Join_group.getButtonTypes().add(ButtonType.NO);

                    Join_group.showAndWait();
                    if (Join_group.getResult() == ButtonType.YES) {
                        current_user.addChatRoom(c[0]);
                        DataBas.reWriteUsers();
                        current_chat = c[0];
                        current_chat.addMessage(new message(current_user.getUserName() + " joined the chat!"));
                        DataBas.saveChat(current_chat, current_chat.getName());
                        server.hasWatingMessage = true;
                        server.watingMessage = current_user.getUserName() + " joined the chat!";
                        load_Group(chat_view_root);
                    }
                }
            }
        });
        Button create_group_chat_view_button = (Button) chat_view_root.lookup("#create_group");
        create_group_chat_view_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                second_stage.setScene(create_group_scene);
                second_stage.show();
            }
        });

        Button creat_group_button = (Button) create_group_root.lookup("#creat_group_button");
        Button cancel_creat_group_button = (Button) create_group_root.lookup("#cancel_creat_group_button");
        TextField create_group_name = (TextField) create_group_root.lookup("#create_group_name");
        Label label_create_group_error = (Label) create_group_root.lookup("#label_create_group_error");
        creat_group_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(!create_group_name.getText().toString().trim().equals("")){
                    label_create_group_error.setVisible(true);
                    label_create_group_error.setText("");
                }else {
                    label_create_group_error.setVisible(false);
                }
                chatRoom c = DataBas.loadChat(create_group_name.getText().toString().trim());
                if (c != null) {
                    label_create_group_error.setText("this chat is already exist try again!");
                }else {
                    chatRoom m = new chatRoom(create_group_name.getText().toString().trim(), DataBas.lastPortNumber);
                    DataBas.lastPortNumber++;
                    DataBas.saveLastPortNumber();
                    current_user.addChatRoom(m);
                    DataBas.reWriteUsers();
                    current_chat = m;
                    DataBas.saveChat(current_chat, create_group_name.getText().toString().trim());
                    load_Group(chat_view_root);
                }
            }
        });
        cancel_creat_group_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                create_group_name.setText("");
            }
        });

        TextField send_message = (TextField) chat_view_root.lookup("#send_message");
        Button send_button = (Button) chat_view_root.lookup("#send_button");
        send_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (current_chat.s == null) {
                    current_chat.startServer(send_message.getText().toString().trim(),chat_view_root);
                }
                send_message.setText("");
            }
        });
    }

    public static void load_Group(Parent chat_view_root) {
        list_of_group = current_user.list_Group();
        if (list_of_group != null) {
            Node = new Node[list_of_group.length];

            group_list = (ListView<Node>) chat_view_root.lookup("#group_list");
            group_list.getItems().clear();
            for (int i = 0; i < list_of_group.length; i++) {
                if(list_of_group[i] != null) {
                    try {
                        Node[i] = new FXMLLoader(Main.class.getResource("fxml/group_item.fxml")).load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final int finalI = i;
                    Node[i].setOnMouseEntered(event -> Node[finalI].setStyle("-fx-background-color: #d32222"));
                    Node[i].setOnMouseExited(event -> Node[finalI].setStyle("-fx-background-color: #1b2444"));
                    Node[i].setOnMousePressed(event -> Node[finalI].setStyle("-fx-background-color: #d32222"));
                    Label groupName = (Label) Node[i].lookup("#group_name");
                    HBox group_item = (HBox) Node[i].lookup("#group_item");
                    groupName.setText(list_of_group[i]);
                    int finalI1 = i;
                    group_item.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            current_chat = current_user.getChatRooms()[finalI1];
                            DataBas.reWriteUsers();
                            current_chat.printMessages(chat_view_root);
                        }
                    });
                    group_list.getItems().add(Node[i]);
                }
            }
        }
    }

    public static boolean checkUsername(String name, Label sign_up_error_label_username) {
        if (name.length() < 4) {
            sign_up_error_label_username.setVisible(true);
            sign_up_error_label_username.setText("Minimum length of username is 4.");
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == ' ' || name.charAt(i) == '@' || name.charAt(i) == '!' || name.charAt(i) == '$' || name.charAt(i) == '%' || name.charAt(i) == '^' || name.charAt(i) == '#' || name.charAt(i) == '&' || name.charAt(i) == '*' || name.charAt(i) == '(' || name.charAt(i) == ')' || name.charAt(i) == '-' || name.charAt(i) == '_' || name.charAt(i) == '+' || name.charAt(i) == '=') {
                sign_up_error_label_username.setVisible(true);
                sign_up_error_label_username.setText("username can`t contain this characters !@#$%^&*()_-+= and also empty space");
                return false;
            }
        }
        if (DataBas.hasUsername(name)) {
            sign_up_error_label_username.setVisible(true);
            sign_up_error_label_username.setText("username already exist try an other one!");
            return false;
        }
        return true;
    }

    public static boolean checkPassword(String pass, String conPass, Label sign_up_error_label_password) {
        if (pass.length() < 8) {
            sign_up_error_label_password.setVisible(true);
            sign_up_error_label_password.setText("minimum length od password is 8.");
            return false;
        }
        if (pass.contains(" ")) {
            sign_up_error_label_password.setVisible(true);
            sign_up_error_label_password.setText("password cant contain empty space!");
            return false;
        }
        if (!pass.equals(conPass)) {
            sign_up_error_label_password.setVisible(true);
            sign_up_error_label_password.setText("password is not pair with confirm password!");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        launch();
    }
}

class user {
    private String userName;
    private String password;
    private String alias;
    private chatRoom chatRooms[] = new chatRoom[1000];
    private int cChats = 0;

    public user(String userName, String password, String alias) {
        this.userName = userName;
        this.password = password;
        this.alias = alias;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public chatRoom[] getChatRooms() {
        return chatRooms;
    }

    public void setChatRooms(chatRoom[] chatRooms) {
        this.chatRooms = chatRooms;
    }

    public int getcChats() {
        return cChats;
    }

    public void setcChats(int cChats) {
        this.cChats = cChats;
    }

    public String getFileOutput() {
        String temp = this.getUserName() + " " + this.getPassword() + " " + this.getAlias() + " ";
        for (int i = 0; i < cChats; i++) {
            temp += chatRooms[i].getName();
            if (i != cChats - 1) {
                temp += " ";
            } else {
                temp += "\n";
            }
        }
        return temp;
    }

    public void addChatRoom(chatRoom c) {
        chatRooms[cChats] = c;
        cChats++;
        DataBas.reWriteUsers();
    }

    public String[] list_Group() {
        String[] list_of_group = new String[100];
        for (int i = 0; i < cChats; i++) {
            list_of_group[i] = chatRooms[i].getName();
        }
        return list_of_group;
    }

    public boolean hasChat(String name) {
        for (int i = 0; i < cChats; i++) {
            if (chatRooms[i].getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void removeChat(String name) {
        for (int i = 0; i < cChats; i++) {
            if (chatRooms[i].getName().equals(name)) {
                for (int j = i; j < cChats - 1; j++) {
                    chatRooms[j] = chatRooms[j + 1];
                }
                cChats--;
                break;
            }
        }
    }

}

class message {
    private String content;

    public message(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

class chatRoom {

    private int port;
    private String name;
    private message messages[] = new message[1000];
    private int cMessages = 0;
    server s;

    public chatRoom(String name, int port) {
        this.name = name;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public message[] getMessages() {
        return messages;
    }

    public void setMessages(message[] messages) {
        this.messages = messages;
    }

    public int getcMessages() {
        return cMessages;
    }

    public void setcMessages(int cMessages) {
        this.cMessages = cMessages;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void removeFirstMessage() {
        for (int i = 0; i < cMessages - 1; i++) {
            messages[i] = messages[i + 1];
        }
    }

    public void addMessage(message message) {
        if (cMessages == 1000) {
            removeFirstMessage();
            messages[cMessages - 1] = message;
        } else {
            messages[cMessages] = message;
            cMessages++;
        }
    }

    public void printMessages(Parent chat_view_root) {
        ListView<String> chat_list = (ListView<String>) chat_view_root.lookup("#chat_list");
        chat_list.getItems().clear();
        for (int i = 0; i < cMessages; i++) {
            System.out.println();
            chat_list.getItems().add(messages[i].getContent());
        }
    }

    public void startServer(String send_message, Parent chat_view_root) {
        try {
            server s = new server(this.port, Main.current_user.getAlias(),send_message,chat_view_root);
        } catch (Exception e) {

        }
    }

}

class DataBas {

    public static user users[] = new user[10000];
    public static int cUsers = 0;
    static String basePath = "E:\\Phelegram\\src\\Files\\";
    public static int lastPortNumber = 0;

    public static void loadUsers() {
        try {
            File myObj = new File(basePath + "Users\\users.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String temp[] = data.split(" ");
                user n = new user(temp[0], temp[1], temp[2]);
                if (temp.length > 3) {
                    for (int i = 3; i < temp.length; i++) {
                        n.addChatRoom(loadChat(temp[i]));
                    }
                }
                users[cUsers] = n;
                cUsers++;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void loadLastPortNumber() {
        try {
            File myObj = new File(basePath + "lastPortNumber.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                DataBas.lastPortNumber = Integer.parseInt(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void saveLastPortNumber() {
        try {
            File myObj = new File(basePath + "lastPortNumber.txt");
            PrintWriter writer = new PrintWriter(myObj);
            writer.write(Integer.toString(DataBas.lastPortNumber));
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static chatRoom loadChat(String name) {
        chatRoom c = new chatRoom(name, 0);
        try {
            File myObj = new File(basePath + "chatRooms\\" + name + ".txt");
            if (!myObj.exists()) {
                return null;
            }
            Scanner myReader = new Scanner(myObj);

            String p = myReader.nextLine();
            String m[] = p.split(" ");
            c.setPort(Integer.parseInt(m[1]));
            while (myReader.hasNextLine()) {
                c.addMessage(new message(myReader.nextLine()));
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return c;
    }

    public static void reWriteUsers() {
        try {
            File myObj = new File(basePath + "Users\\users.txt");
            PrintWriter writer = new PrintWriter(myObj);

            for (int i = 0; i < cUsers; i++) {
                String l = users[i].getFileOutput();
                writer.write(l);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void saveChat(chatRoom c, String name) {
        try {
            File myObj = new File(basePath + "chatRooms\\" + name + ".txt");
            PrintWriter writer = new PrintWriter(myObj);

            writer.write("port " + c.getPort() + "\n");

            for (int i = 0; i < c.getcMessages(); i++) {
                writer.write(c.getMessages()[i].getContent() + "\n");
            }
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void signUp(user user) {
        users[cUsers] = user;
        cUsers++;
        reWriteUsers();
    }

    public static boolean hasUsername(String username) {
        for (int i = 0; i < cUsers; i++) {
            if (users[i].getUserName().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public static user login(String name, String pass) {
        for (int i = 0; i < cUsers; i++) {
            if (users[i].getUserName().equals(name) && users[i].getPassword().equals(pass)) {
                return users[i];
            }
        }
        return null;
    }
}

class server {
    public static final String TERMINATE = "Exit";
    static String name;
    static volatile boolean finished = false;
    MulticastSocket socket;
    InetAddress group;
    int port;
    static boolean hasWatingMessage = false;
    static String watingMessage = "";

    public server(int port2, String name2 , String send_message,Parent chat_view_root) {

        try {
            InetAddress group = InetAddress.getByName("239.0.0.0");
            name = name2;
            port = port2;
            MulticastSocket socket = new MulticastSocket(port);

            socket.setTimeToLive(0);
            socket.joinGroup(group);
            Thread t = new Thread(new ReadThread(socket, group, port));
            t.start();

                if (hasWatingMessage) {
                    hasWatingMessage = false;
                    byte[] buffer = (watingMessage.getBytes());
                    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
                    socket.send(datagram);
                }

                String m;
                m = send_message;
                if (m.equalsIgnoreCase(server.TERMINATE)) {
                    finished = true;
                    byte[] buffer = (Main.current_user.getAlias() + " went offline").getBytes();
                    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
                    socket.send(datagram);
                    socket.leaveGroup(group);
                    socket.close();
                } else if (m.equalsIgnoreCase("leave")) {
                    Main.current_user.removeChat(Main.current_chat.getName());
                    DataBas.reWriteUsers();
                    finished = true;
                    byte[] buffer = (Main.current_user.getAlias() + " left the group").getBytes();
                    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
                    socket.send(datagram);
                    socket.leaveGroup(group);
                    socket.close();
                }
                m = name + ": " + m;
                byte[] buffer = m.getBytes();
                DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
                socket.send(datagram);
                Main.current_chat.addMessage(new message(m));
                DataBas.saveChat(Main.current_chat, Main.current_chat.getName());
                Main.current_chat.printMessages(chat_view_root);
        } catch (SocketException se) {
            System.out.println("Error creating socket");
            se.printStackTrace();
        } catch (IOException ie) {
            System.out.println("Error reading/writing from/to socket");
            ie.printStackTrace();
        }
    }

    public void sendMessage(String m) {
        byte[] buffer = m.getBytes();
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
        try {
            socket.send(datagram);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class ReadThread implements Runnable {

    private MulticastSocket socket;
    private InetAddress group;
    private int port;
    private static final int MAX_LEN = 1000;

    ReadThread(MulticastSocket socket, InetAddress group, int port) {
        this.socket = socket;
        this.group = group;
        this.port = port;
    }

    @Override
    public void run() {
        while (!server.finished) {
            byte[] buffer = new byte[ReadThread.MAX_LEN];
            DatagramPacket datagram = new
                    DatagramPacket(buffer, buffer.length, group, port);
            String message;
            try {
                socket.receive(datagram);
                message = new
                        String(buffer, 0, datagram.getLength(), "UTF-8");
                if (!message.startsWith(server.name))
                    System.out.println(message);
            } catch (IOException e) {
                System.out.println("Socket closed!");
            }
        }
    }
}