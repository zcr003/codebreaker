package edu.cnm.deepdive.codebreaker.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import edu.cnm.deepdive.codebreaker.R;
import edu.cnm.deepdive.codebreaker.model.entity.Game;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import edu.cnm.deepdive.codebreaker.service.GameRepository;
import io.reactivex.disposables.CompositeDisposable;

public class PlayViewModel extends AndroidViewModel implements DefaultLifecycleObserver {

  private final GameRepository gameRepository;
  //Live data is not a requirement of view model but will typically be there.
  private final MutableLiveData<Game> game;
  private final MutableLiveData<Throwable> throwable;
  private final CompositeDisposable pending;
  private final SharedPreferences preferences;
  private final String basePool;
  private final String codeLengthPrefKey;
  private final String poolSizePrefKey;
  private final int codeLengthPrefDefault;
  private final int poolSizePrefDefault;


  //The constructor to initialize. Always has the same name as the class. No return type. Application class is a subclass of context.
  public PlayViewModel(@NonNull Application application) {
    super(application);
    gameRepository = new GameRepository();
    game = new MutableLiveData<>();
    throwable = new MutableLiveData<>();
    pending = new CompositeDisposable();
    String[] emojis = application.getResources().getStringArray(R.array.emojis);
    basePool = String.join("", emojis);
    preferences = PreferenceManager.getDefaultSharedPreferences(application);
    Resources resources = application.getResources();
    codeLengthPrefKey = resources.getString(R.string.code_length_pref_key);
    poolSizePrefKey = resources.getString(R.string.pool_size_pref_key);
    codeLengthPrefDefault = resources.getInteger(R.integer.code_length_pref_default);
    poolSizePrefDefault = resources.getInteger(R.integer.pool_size_pref_default);
    startGame();
  }

  //Getters for throwable and game.
  public MutableLiveData<Game> getGame() {
    return game;
  }

  public MutableLiveData<Throwable> getThrowable() {
    return throwable;
  }

  // When we invoke startGame we get a single object.
  public void startGame() {
    throwable.postValue(null);
    int codeLength = preferences.getInt(codeLengthPrefKey, codeLengthPrefDefault);
    int poolSize = preferences.getInt(poolSizePrefKey, poolSizePrefDefault);
    int[] poolCodePoints = basePool
        .codePoints()
        .limit(poolSize)
        .toArray();
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

  @Override
  public void onDestroy(@NonNull LifecycleOwner owner) {
    DefaultLifecycleObserver.super.onDestroy(owner);
    pending.clear();
  }

  private void postThrowable(Throwable throwable) {
    Log.e(getClass().getSimpleName(), throwable.getMessage(), throwable);
    this.throwable.postValue(throwable);

  }
}
