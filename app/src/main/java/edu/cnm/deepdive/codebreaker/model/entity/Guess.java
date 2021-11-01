package edu.cnm.deepdive.codebreaker.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.Date;


@Entity(
    tableName = "guess",
    indices = {
        @Index(value = "service_key", unique = true)
    },
    foreignKeys = {
        @ForeignKey(
            entity = Game.class,
            parentColumns = "game_id",
            childColumns = "game_id",
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class Guess {

  @PrimaryKey
  @ColumnInfo(name = "guess_id")
  private long id;

  @ColumnInfo(name = "game_id", index = true)
  private long gameId;

  @NonNull
  @Expose
  @SerializedName("id")
  @ColumnInfo(name = "service_key")
  private String serviceKey;

  @NonNull
  @Expose
  @ColumnInfo(index = true)
  private Date created;

  @Expose
  private String text;

  @Expose
  @ColumnInfo(name = "exact_matches")
  private int exactMatches;

  @Expose
  @ColumnInfo(name = "near_matches")
  private int nearMatches;

  @Ignore
  @Expose
  private boolean solution;

  //Getters and setters to give access outside of this class
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getGameId() {
    return gameId;
  }

  public void setGameId(long gameId) {
    this.gameId = gameId;
  }

  @NonNull
  public String getServiceKey() {
    return serviceKey;
  }

  public void setServiceKey(@NonNull String serviceKey) {
    this.serviceKey = serviceKey;
  }

  @NonNull
  public Date getCreated() {
    return created;
  }

  public void setCreated(@NonNull Date created) {
    this.created = created;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public int getExactMatches() {
    return exactMatches;
  }

  public void setExactMatches(int exactMatches) {
    this.exactMatches = exactMatches;
  }

  public int getNearMatches() {
    return nearMatches;
  }

  public void setNearMatches(int nearMatches) {
    this.nearMatches = nearMatches;
  }

  public boolean isSolution() {
    return solution;
  }

  public void setSolution(boolean solution) {
    this.solution = solution;
  }
}
