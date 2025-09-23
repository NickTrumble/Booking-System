import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;

public class Main {
    //TODO
    /*
    - save bookings once actually booked to data base and colour them a different colour
    - update seats based on bookings in the database
    - add admin roles maybe
    - password encryption of some sorty
    - UI stuff
     */



    public static Establishment room;
    public static HashSet<String> selectedSeats;
    public static void main(String[] args) {
        Database.init();

        //Beginning form
        JFrame introFrame = new JFrame("Booking System");
        introFrame.setSize(400, 100);
        introFrame.setLayout(null);
        introFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        introFrame.setLocationRelativeTo(null);

        //buttons
        JButton LoginButton = new JButton("Login");
        LoginButton.setBounds(125, 25, 150, 25);
        introFrame.add(LoginButton);



        //second form
        JFrame loginFrame = new JFrame("Login to booking system");
        loginFrame.setSize(400, 125);
        loginFrame.setLayout(null);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);

        JButton backButton = new JButton("Cancel");
        backButton.setBounds(290, 5, 75, 20);
        loginFrame.add(backButton);

        JLabel titleLabel = new JLabel("Login:");
        titleLabel.setBounds(80, 2, 100, 25);
        loginFrame.add(titleLabel);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(10, 30, 70, 20);
        loginFrame.add(userLabel);

        JTextField userField = new JTextField();
        userField.setBounds(80, 30, 180, 20);
        loginFrame.add(userField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 60, 70, 20);
        loginFrame.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(80, 60, 180, 20);
        loginFrame.add(passwordField);


        JButton confirmLoginButton = new JButton("Login");
        confirmLoginButton.setBounds(280, 60, 100, 20);
        loginFrame.add(confirmLoginButton);

        //Main booking area
        JFrame MainFrame = new JFrame("Booking System:");
        MainFrame.setSize(1000, 800);
        MainFrame.setLayout(null);
        MainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MainFrame.setLocationRelativeTo(null);

        JButton bookButton = new JButton("Book Seat(s)");
        bookButton.setBounds(752, 601, 230, 158);
        MainFrame.add(bookButton);

        //event listeners
        LoginButton.addActionListener(e -> goToForm(introFrame, loginFrame));
        backButton.addActionListener(e -> goToForm(loginFrame, introFrame));
        confirmLoginButton.addActionListener(e -> {
            boolean result = Database.authenticate(
                    userField.getText(), new String(passwordField.getPassword())
            );

            if (result){
                goToForm(loginFrame, MainFrame);
                selectedSeats = new HashSet<String>();

                MainFrame.setTitle("Booking System: " + room.name + ", " + room.location);

                JPanel chairPanel = room.getSeatPanel();

                chairPanel.setBounds(250, 360, 500, 400);
                MainFrame.add(chairPanel);
            }

        });
        bookButton.addActionListener(e -> {
            JFrame bookedSeatsFrame = new JFrame("Booking seats!");
            bookedSeatsFrame.setSize(270, 100);
            bookedSeatsFrame.setLayout(null);
            bookedSeatsFrame.setLocationRelativeTo(null);

            selectedSeats = room.parseSelectedSeats();

            StringBuilder string = new StringBuilder("Booking seats:");
            for (String seat : selectedSeats){
                string.append(" ").append(seat).append(",");
            }
            JTextArea seatListings = new JTextArea(string.toString());
            seatListings.setBounds(0, 0, 260, 70);
            seatListings.setEditable(false);
            seatListings.setLineWrap(true);
            seatListings.setWrapStyleWord(true);
            bookedSeatsFrame.add(seatListings);
            bookedSeatsFrame.setVisible(true);
        });

        introFrame.setVisible(true);
    }

    public static void goToForm(JFrame original, JFrame newFrame){
        original.setVisible(false);
        newFrame.setVisible(true);
    }
}

//after authenticating, register all data in the establishment class and the worker class can come back with a hierarchy level?



class Establishment{
    public String name;
    public String location;
    public int capacity;
    public int rows;
    public JButton[][] seats;


    public Establishment(String name, String location, int capacity, int rows){
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.rows = rows;

        seats = new JButton[10][rows];
    }

    public JPanel getSeatPanel(){
        JPanel seatPanel = new JPanel();
        seatPanel.setLayout(new GridLayout(this.rows, 10, 2, 2));

        for (int i = 0; i < 10; i++){
            for (int j = 0; j < this.rows; j++){

                JButton chair = new JButton(i + "" + j);

                chair.addActionListener(e ->{
                    if (chair.getBackground() == Color.pink){
                        chair.setBackground(null);
                    }
                    else {
                        chair.setBackground(Color.pink);
                    }
                });

                seats[i][j] = chair;
                seatPanel.add(chair);
            }
        }
        return seatPanel;
    }

    public HashSet<String> parseSelectedSeats(){
        HashSet<String> selectedSeats = new HashSet<>();

        for (int i = 0; i < 10; i++){
            for (int j = 0; j < this.rows; j++){
                if (seats[i][j].getBackground() == Color.pink)
                    selectedSeats.add(i + "" + j);
            }
        }

        return selectedSeats;
    }

}


class Database{
    private static final String fileURL = "jdbc:sqlite:bookings.db";

    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(fileURL);
    }

    public static void init(){
        String[] sql = {"CREATE TABLE IF NOT EXISTS Users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, " +
                "password TEXT, " +
                "FOREIGN KEY (establishmentID) REFERENCES Establishments(establishmentID))",

                "CREATE TABLE IF NOT EXISTS Seats (" +
                "establishmentID INTEGER, " +
                "seatrow INTEGER, " +
                "seatcol INTEGER, " +
                "reserved INTEGER, " +
                "PRIMARY KEY(establishmentID, seatrow, seatcol), " +
                "FOREIGN KEY (establishmentID) REFERENCES Establishments(establishmentID))",

                "CREATE TABLE IF NOT EXISTS Establishments (" +
                "establishmentID INTEGER PRIMARY KEY, " +
                "name TEXT, " +
                "location TEXT, " +
                "capacity INTEGER)"};

        try(Connection connection = getConnection();
        Statement statement = connection.createStatement()){
            for (String query : sql){
                statement.execute(query);
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static boolean authenticate(String username, String password){
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";

        try(Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() && registerData(resultSet);
        }
        catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean registerData(ResultSet resultSet){
        try {
            //String username = resultSet.getString("username");
            int establishmentID = resultSet.getInt("establishmentID");

            String sql = "SELECT * FROM Establishments WHERE establishmentID = ?";
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, establishmentID);

                ResultSet establishmentSet = preparedStatement.executeQuery();
                Main.room = new Establishment(establishmentSet.getString(2),
                        establishmentSet.getString(3), establishmentSet.getInt(3), 10);

                return true;

            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}