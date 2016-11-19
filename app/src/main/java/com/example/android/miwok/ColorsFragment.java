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
public class ColorsFragment extends Fragment {
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

    public ColorsFragment() {
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
        words.add(new Word("red", "weṭeṭṭi", R.raw.color_red, R.drawable.color_red));
        words.add(new Word("mustard yellow", "chiwiiṭә", R.raw.color_mustard_yellow, R.drawable.color_mustard_yellow));
        words.add(new Word("dusty yellow", "ṭopiisә", R.raw.color_dusty_yellow, R.drawable.color_dusty_yellow));
        words.add(new Word("green", "chokokki", R.raw.color_green, R.drawable.color_green));
        words.add(new Word("brown", "ṭakaakki", R.raw.color_brown, R.drawable.color_brown));
        words.add(new Word("gray", "ṭopoppi", R.raw.color_gray, R.drawable.color_gray));
        words.add(new Word("black", "kululli", R.raw.color_black, R.drawable.color_black));
        words.add(new Word("white", "kelelli", R.raw.color_white, R.drawable.color_white));

        // Create an {@link WordAdapter}, whose data source is a list of {@link Word}s. The
        // adapter knows how to create list items for each item in the list.
        WordAdapter adapter = new WordAdapter(getContext(), words, R.color.category_colors);

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
