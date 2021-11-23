package edu.cnm.deepdive.codebreaker.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import edu.cnm.deepdive.codebreaker.R;
import edu.cnm.deepdive.codebreaker.model.entity.Game;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import edu.cnm.deepdive.codebreaker.service.GameRepository;
import edu.cnm.deepdive.codebreaker.service.SettingsRepository;
import io.reactivex.disposables.CompositeDisposable;

public class PlayViewModel extends AndroidViewModel implements LifecycleObserver {

  private final GameRepository gameRepository;
  private final SettingsRepository settingsRepository;
  //Live data is not a requirement of viewmodel but will typically be there.
  private final MutableLiveData<Game> game;
  private final MutableLiveData<Throwable> throwable;
  private final CompositeDisposable pending;

  private int codeLength;
  private int poolSize;
  private String basePool;


  //The constructor to initialize. Always has the same name as the class. No return type.
  public PlayViewModel(@NonNull Application application) {
    super(application);
    gameRepository = new GameRepository();
    settingsRepository = new SettingsRepository(application);
    game = new MutableLiveData<>();
    throwable = new MutableLiveData<>();
    pending = new CompositeDisposable();
    String[] emojis = application.getResources().getStringArray(R.array.emojis);
    basePool = String.join("", emojis);
    subscribeToSettings();
  }

  //Getters for throwable and game.
  public MutableLiveData<Game> getGame() {
    return game;
  }


  public MutableLiveData<Throwable> getThrowable() {
    return throwable;
  }

  //The Method. When we invoke startGame we get a single object.
  public void startGame() {
    if (codeLength > 0 && poolSize > 0) {
      int[] poolCodePoints = basePool
          .codePoints()
          .limit(poolSize)
          .toArray();
      throwable.postValue(null);
      Game game = new Game();
      game.setPool(new String(poolCodePoints, 0, poolCodePoints.length));
      game.setLength(codeLength);
      pending.add(
          gameRepository
              .save(game)
              .subscribe(
                  /* A consumer is a functional interface that takes a
           parameter and doesn't return anything */
                  this.game::postValue,
                  this::postThrowable

              )
      );
    }

  }


  public void submitGuess(String text) {
    throwable.postValue(null);
    Guess guess = new Guess();
    guess.setText(text);
    //noinspection ConstantConditions
    pending.add(
        gameRepository
            .save(game.getValue(), guess)
            .subscribe(
                game::postValue,
                this::postThrowable
            )
    );


  }

  @OnLifecycleEvent(Event.ON_STOP)
  private void clearPending() {
    pending.clear();
  }

  //Another Method.
  private void postThrowable(Throwable throwable) {
    Log.e(getClass().getSimpleName(), throwable.getMessage(), throwable);
    this.throwable.postValue(throwable);

  }

  private void subscribeToSettings() {
    pending.add(
        settingsRepository
            .getCodeLengthPreference()
            .subscribe(
                (codeLength) -> {
                  this.codeLength = codeLength;
                  startGame();
                },
                this::postThrowable
            )
    );
    pending.add(
        settingsRepository
            .getPoolSizePreference()
            .subscribe(
                (poolSize) -> {
                  this.poolSize = poolSize;
                  startGame();
                },
                this::postThrowable
            )
    );

  }

}
