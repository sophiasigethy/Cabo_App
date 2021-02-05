package msp.group3.caboclient.CaboModel;

import android.content.SharedPreferences;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Objects;

import msp.group3.caboclient.CaboController.DatabaseOperation;
import msp.group3.caboclient.R;
import msp.group3.caboclient.CaboController.TypeDefs;

public class Player {

    private int NO_AVATAR_CHOSEN = 9;
    private int id;
    private String dbID = "";
    private String name = "";
    private String mail = "";
    private String nick = "";



    private int avatarID = NO_AVATAR_CHOSEN;
    private int score=0;
    private int globalScore=0;
    private ArrayList<Player> friendList = new ArrayList<>();
    private ArrayList<Card> myCards = new ArrayList<>();
    private Boolean isOnline = false;



    private String picture="";
    private String smiley="";
    private String status;

    public Player(){

    }

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.status = TypeDefs.waiting;
    }

    public Player(int id, String name, String nick) {
        this.id = id;
        this.name = name;
        this.nick = nick;
        this.status = TypeDefs.waiting;
    }

    public Player(String dbID) {
        this.dbID = dbID;
    }

    public Player(String dbID, String name, String mail, String nick, int avatarID, int globalScore) {
        this.dbID = dbID;
        this.name = name;
        this.mail = mail;
        this.nick = nick;
        this.avatarID = avatarID;
        this.globalScore = globalScore;
    }

    public Player(String dbID, String nick, int avatarID, int globalScore) {
        this.dbID = dbID;
        this.nick = nick;
        this.avatarID = avatarID;
        this.globalScore = globalScore;
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

    public void updateStatus(Player player){
        this.id= player.getId();
        this.name=player.getName();
        this.myCards= player.getMyCards();
        this.status=player.getStatus();
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

    public int getGlobalScore() {
        return globalScore;
    }

    public void setGlobalScore(int globalScore) {
        this.globalScore = globalScore;
    }

    public ArrayList<Player> getFriendList() {
        return friendList;
    }

    public ArrayList<String> getFriendsNicknames() {
        ArrayList<String> nicks = new ArrayList<>();
        for (Player player : friendList) {
            nicks.add(player.getNick());
        }
        return nicks;
    }

    public boolean addNewFriend(Player player, SharedPreferences sharedPref) {
        friendList.add(player);
        return DatabaseOperation.getDao().writePlayerToSharedPref(this, sharedPref);
    }

    public void setFriendList(ArrayList<Player> friendList) {
        this.friendList = friendList;
    }

    public void updateCards(Player other) {
        this.myCards = other.getMyCards();
    }

    public void updateScore(Player other) {
        this.score = other.getScore();
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }

    public int getAvatarID() {
        return avatarID;
    }

    public void setAvatarID(int avatarID) {
        this.avatarID = avatarID;
    }

    public int getAvatarIcon() {
        switch (avatarID)   {
            case 0:
                return R.drawable.avatar0;
            case 1:
                return R.drawable.avatar1;
            case 2:
                return R.drawable.avatar2;
            case 3:
                return R.drawable.avatar3;
            case 4:
                return R.drawable.avatar4;
            case 5:
                return R.drawable.avatar5;
        }
        return R.drawable.avatar9;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(dbID, player.dbID) &&
                Objects.equals(nick, player.nick);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dbID, nick);
    }

    public boolean isEmpty()   {
        return (this.dbID.equals("") && this.nick.equals(""));
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getSmiley() {
        return smiley;
    }

    public void setSmiley(String smiley) {
        this.smiley = smiley;
    }

    public void replacePlayerForNextRound(Player other){
        score=other.getScore();
        myCards.clear();
        myCards.addAll(other.getMyCards());
        status=other.getStatus();
    }
    public int getNO_AVATAR_CHOSEN() {
        return NO_AVATAR_CHOSEN;
    }
    public void setNO_AVATAR_CHOSEN(int NO_AVATAR_CHOSEN) {
        this.NO_AVATAR_CHOSEN = NO_AVATAR_CHOSEN;
    }

    public void won(SharedPreferences sharedPref)   {
        globalScore++;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(String.valueOf(R.string.preference_global_score), globalScore + "");
        editor.apply();
    }

    public Boolean getOnline() {
        return isOnline;
    }
}
