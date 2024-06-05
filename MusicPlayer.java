import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.*;
import java.util.ArrayList;

public class MusicPlayer extends PlaybackListener {
    private MusicPlayerGUI musicPlayerGUI;;
    private Song currentSong;
    public Song getCurrentSong() { return currentSong; }
    private ArrayList<Song> playlist;
    private int currentPlaylistIndex;
    private File currentPlaylist;
    private AdvancedPlayer advancedPlayer;
    private boolean isPaused;
    private int currentFrame;

    public File getCurrentPlaylist() {
        return currentPlaylist;
    }

    public ArrayList<Song> getPlaylist() { return playlist; }

    public void setPlaylist(ArrayList<Song> playlist) { this.playlist = playlist; }

    public MusicPlayer(MusicPlayerGUI musicPlayerGUI) { this.musicPlayerGUI = musicPlayerGUI; }

    public void loadSong(Song song){
        currentSong = song;
        if(currentSong != null){
            currentFrame = 0;

            playCurrentSong();
        }
    }
    public void loadPlaylist(File playlistFile){
        playlist = new ArrayList<>();

        try{
            FileReader fileReader = new FileReader(playlistFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String songPath;

            while((songPath = bufferedReader.readLine()) != null){
                Song song = new Song(songPath);

                playlist.add(song);
            }
            currentPlaylist = playlistFile;
        }catch(Exception e){
            e.printStackTrace();
        }

        if(playlist.size() > 0){
            currentSong = playlist.get(0);

            currentFrame = 0;

            musicPlayerGUI.enablePauseButtonDisablePlayButton();
            musicPlayerGUI.updateSongTitleAndArtist(currentSong);

            playCurrentSong();
        }
    }
    public void pauseSong(){
        if (advancedPlayer != null){
            isPaused = true;

            stopSong();
        }
    }
    public void stopSong(){
        if (advancedPlayer != null){
            advancedPlayer.stop();
            advancedPlayer.close();
            advancedPlayer = null;
        }
    }

    public void nextSong(){
        if(playlist == null) return;

        stopSong();

        if(currentPlaylistIndex + 1 > playlist.size() - 1) { currentPlaylistIndex = -1; };

        currentPlaylistIndex++;
        currentSong = playlist.get(currentPlaylistIndex);
        currentFrame = 0;

        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);

        playCurrentSong();
    }
    public void prevSong(){
        if(playlist == null) return;

        stopSong();

        if(currentPlaylistIndex == 0) { currentPlaylistIndex = playlist.size(); };

        currentPlaylistIndex--;
        currentSong = playlist.get(currentPlaylistIndex);
        currentFrame = 0;

        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);

        playCurrentSong();
    }

    public void saveCurrentPlaylist(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            for (Song song : playlist) {
                writer.write(song.getFilePath() + System.lineSeparator());
            }
            System.out.println("Playlist saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("An error occurred while saving the playlist.");
            e.printStackTrace();
        }
    }
    public void playCurrentSong() {
        if (currentSong == null) return;

        try {
            FileInputStream fileInputStream = new FileInputStream(currentSong.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            advancedPlayer = new AdvancedPlayer(bufferedInputStream);
            advancedPlayer.setPlayBackListener(this);

            startMusicThread();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void startMusicThread () {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isPaused){
                        isPaused = false;
                        advancedPlayer.play(currentFrame, Integer.MAX_VALUE);
                    }else{
                        advancedPlayer.play();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        System.out.println("Playback Started");
    }
    @Override
    public void playbackFinished(PlaybackEvent evt) {
        System.out.println("Playback Finished");
        if(isPaused){
            currentFrame += (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMilliseconds());
        }
    }

    public void addSongToPlaylist(Song song) {
        if (playlist == null) {
            playlist = new ArrayList<>();
        }
        playlist.add(song);

    }
    public void deleteSongFromPlaylist(Song song) {
        playlist.remove(song);
        System.out.println("Song removed from playlist: " + song.getSongTitle());
    }


}
