package edu.cnm.deepdive.codebreaker.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import edu.cnm.deepdive.codebreaker.model.entity.Game;
import edu.cnm.deepdive.codebreaker.model.view.GameSummary;
import edu.cnm.deepdive.codebreaker.service.GameRepository;
import io.reactivex.disposables.CompositeDisposable;
import java.util.List;

public class MainViewModel extends AndroidViewModel implements LifecycleObserver {

  private final GameRepository repository;
  //Live data is not a requirement of viewmodel but will typically be there.
  private final MutableLiveData<Game> game;
  //let the ui controller invoke a method that gives us a value for these 3 fields
//  private final LiveData<List<GameSummary>> scores;
//  private final MutableLiveData<Integer> poolSize;
//  private final MutableLiveData<Integer> length;
//  private final MutableLiveData<Boolean> orderByTotalTime;
  private final MutableLiveData<Throwable> throwable;
  private final CompositeDisposable pending;

  //The constructor to initialize. Always has the same name as the class. No return type.
  public MainViewModel(@NonNull Application application) {
    super(application);
    repository = new GameRepository();
    game = new MutableLiveData<>();
    throwable = new MutableLiveData<>();
    pending = new CompositeDisposable();
    startGame();
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
    throwable.postValue(null);
    pending.add(
        repository
            .startGame("ABCDEF", 3)
            .subscribe(
                /* A consumer is a functional interface that takes a
         parameter and doesn't return anything */
                game::postValue,
                this::postThrowable

            )
    );

  }


  public void submitGuess(String text) {
    throwable.postValue(null);
    pending.add(
        repository
            .submitGuess(game.getValue(), text)
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
}
