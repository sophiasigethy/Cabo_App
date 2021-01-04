package msp.group3.caboclient;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class Player {

    private int id;
    private String dbID = "";
    private String name = "";
    private String mail = "";
    private String nick = "";
    private int score;
    private ArrayList<Player> friendList = new ArrayList<>();
    private ArrayList<Card> myCards = new ArrayList<>();


    private String status;

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.status=TypeDefs.waiting;
    }

    public Player(String dbID) {
        this.dbID = dbID;
    }

    public Player(String dbID, String name, String mail, String nick) {
        this.dbID = dbID;
        this.name = name;
        this.mail = mail;
        this.nick = nick;
    }

    public Player(String dbID, String nick) {
        this.dbID = dbID;
        this.nick = nick;
    }

    public Player(GoogleSignInAccount account) {
        dbID = account.getId();
        name = account.getDisplayName();
        mail = account.getEmail();
    }

    public Player(FirebaseUser user) {
        dbID = user.getUid();
        name = user.getDisplayName();
        mail = user.getEmail();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Card> getMyCards() {
        return myCards;
    }

    public void setMyCards(ArrayList<Card> myCards) {
        this.myCards = myCards;
    }

    public void replacePlayer(Player player){
        this.id= player.getId();
        this.name=player.getName();
        this.myCards= player.getMyCards();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDbID() {
        return dbID;
    }

    public void setDbID(String dbID) {
        this.dbID = dbID;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public ArrayList<Player> getFriendList() {
        return friendList;
    }

    public ArrayList<String> getFriendsNicknames()  {
        ArrayList<String> nicks = new ArrayList<>();
        for (Player player : friendList)    {
            nicks.add(player.getNick());
        }
        return nicks;
    }

    public void setFriendList(ArrayList<Player> friendList) {
        this.friendList = friendList;
    }
}

