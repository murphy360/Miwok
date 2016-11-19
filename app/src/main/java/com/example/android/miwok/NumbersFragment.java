package com.example.android.miwok;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class NumbersFragment extends Fragment {
    private MediaPlayer mPlayer;
   
    //On completion listener... used to release MediaPlayer after sound stops playing
    MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            releaseMediaPlayer();
        }
    };
    
    //AudioMangager used for requesting AudioFocus while music playing
    private AudioManager audioManager;

    //Audio Focus Change listener handles changes
    AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                            focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                        // Pause playback because your Audio Focus was
                        // temporarily stolen, but will be back soon.
                        // i.e. for a phone call
                        mPlayer.pause();
                        // I want to hear the whole word / Phrase every time...
                        // not restart halfway through
                        mPlayer.seekTo(0);
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        // Stop playback, because you lost the Audio Focus.
                        // i.e. the user started some other playback app
                        // Remember to unregister your controls/buttons here.
                        // And release the kra — Audio Focus!
                        // You’re done.

                        releaseMediaPlayer();
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        // Resume playback, because you hold the Audio Focus
                        // again!
                        // i.e. the phone call ended or the nav directions
                        // are finished
                        // If you implement ducking and lower the volume, be
                        // sure to return it to normal here, as well.
                        mPlayer.start();
                    }
                }
            };

    public NumbersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.word_list, container, false);
        //Instantiate AudioManager instance.
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        // Create a list of words
        ArrayList<Word> words = new ArrayList<>();
        words.add(new Word("one", "lutti", R.raw.number_one, R.drawable.number_one));
        words.add(new Word("two", "otiiko", R.raw.number_two, R.drawable.number_two));
        words.add(new Word("three", "tolookosu",  R.raw.number_three, R.drawable.number_three));
        words.add(new Word("four", "oyyisa",  R.raw.number_four, R.drawable.number_four));
        words.add(new Word("five", "massokka",  R.raw.number_five, R.drawable.number_five));
        words.add(new Word("six", "temmokka",  R.raw.number_six, R.drawable.number_six));
        words.add(new Word("seven", "kenekaku",  R.raw.number_seven, R.drawable.number_seven));
        words.add(new Word("eight", "kawinta",  R.raw.number_eight, R.drawable.number_eight));
        words.add(new Word("nine", "wo’e",  R.raw.number_nine, R.drawable.number_nine));
        words.add(new Word("ten", "na’aacha",  R.raw.number_ten, R.drawable.number_ten));

        // Create an {@link WordAdapter}, whose data source is a list of {@link Word}s. The
        // adapter knows how to create list items for each item in the list.
        WordAdapter adapter = new WordAdapter(getContext(), words, R.color.category_numbers);



        // Find the {@link ListView} object in the view hierarchy of the {@link Activity}.
        // There should be a {@link ListView} with the view ID called list, which is declared in the
        // word_list.xml layout file.
        final ListView listView = (ListView) rootView.findViewById(R.id.list);

        // Make the {@link ListView} use the {@link WordAdapter} we created above, so that the
        // {@link ListView} will display list items for each {@link Word} in the list.
        listView.setAdapter(adapter);

        //Make listView Clickable
        listView.setClickable(true);

        /**
         * onItemClickListener pulls associated word from adapter in order to access imageResource
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                //Word item returned from listView... used to figure out what audio to play
                Word w = (Word) (listView.getItemAtPosition(position));
                
                //Make sure MediaPlayer is ready for setup
                releaseMediaPlayer();
                
                //Setup and tell MediaPlayer what it's going to play
                mPlayer = MediaPlayer.create(getContext(), w.getAudioResourceId());

                //Request AudioFocus for upcoming Play of Audio
                int result = audioManager.requestAudioFocus(afChangeListener,
                        //Use the Music Stream
                        AudioManager.STREAM_MUSIC,
                        //Request Transient Focus
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

                // If I receive Audio Focus, play my audio
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                    //Start the Audio
                    mPlayer.start();
                    //Set OnCompletionListener
                    mPlayer.setOnCompletionListener(mCompletionListener);
                }//TODO - include error handling. What to do if i don't recieve focus?
            }
        });
        return rootView;
    }
    
    @Override
    public void onStop(){
        super.onStop();
        releaseMediaPlayer();
    }

    /**
     * If MediaPlayer is not currently null. it releases and sets to null.
     * Called before and after playing a sound.
     */
    private void releaseMediaPlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            //Abandon Audiofocus
            audioManager.abandonAudioFocus(afChangeListener);
        }
    }
}
