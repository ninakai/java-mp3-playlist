import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class MusicPlayerGUI extends JFrame {

    public static final Color FRAME_COLOR = Color.DARK_GRAY;
    public static final Color TEXT_COLOR = Color.WHITE;

    private MusicPlayer musicPlayer;
    private JFileChooser jFileChooser;
    private JLabel songTitle, songArtist;
    private JPanel playbackBtns, songContainer;

    public MusicPlayerGUI(){
        super("Music Player");

        setSize(400, 600);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        setResizable(false);

        setLayout(null);

        getContentPane().setBackground(FRAME_COLOR);

        musicPlayer = new MusicPlayer(this);

        jFileChooser = new JFileChooser();
        jFileChooser.setCurrentDirectory(new File("src/assets"));

        jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));

        addGuiComponents();
    }

    private void addGuiComponents(){
        addToolbar();
        addPlaylistWindow();

        songTitle = new JLabel("Song Title");
        songTitle.setBounds(0, 375, getWidth() - 10, 30);
        songTitle.setFont(new Font("Dialog", Font.BOLD, 24));
        songTitle.setForeground(TEXT_COLOR);
        songTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(songTitle);

        songArtist = new JLabel("Artist");
        songArtist.setBounds(0, 405, getWidth() - 10, 30);
        songArtist.setFont(new Font("Dialog", Font.PLAIN, 24));
        songArtist.setForeground(TEXT_COLOR);
        songArtist.setHorizontalAlignment(SwingConstants.CENTER);
        add(songArtist);

        addPlaybackBtns();

    }
    private void addPlaylistWindow() {
        songContainer = new JPanel();
        songContainer.setLayout(new BoxLayout(songContainer, BoxLayout.Y_AXIS));
        songContainer.setBounds(10, 50, getWidth() - 40, 300);
        add(songContainer);
    }
    private void addSongsToPlaylistWindow() {
        songContainer.removeAll();
        songContainer.updateUI();
        for (int i = 0; i < musicPlayer.getPlaylist().size(); i++){
            Song song = musicPlayer.getPlaylist().get(i);
            String str = song.getSongArtist() + " - " + song.getSongTitle() + " - " + song.getSongLength();
            JLabel fileNameLabel = new JLabel(str);
            fileNameLabel.setFont(new Font("Dialog", Font.BOLD, 12));
            fileNameLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            songContainer.add(fileNameLabel);
        }
            songContainer.revalidate();
    }
    private void addToolbar(){
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 20);

        toolBar.setFloatable(false);

        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        JMenu songMenu = new JMenu("Song");
        menuBar.add(songMenu);

        JMenuItem addSong = new JMenuItem("Add Song");
        addSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if (result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    Song song = new Song(selectedFile.getPath());
                    musicPlayer.stopSong();

                    musicPlayer.loadSong(song);
                    updateSongTitleAndArtist(song);
                    enablePauseButtonDisablePlayButton();

                    musicPlayer.addSongToPlaylist(song);

                    addSongsToPlaylistWindow();

                }
            }
        });
        songMenu.add(addSong);

        JMenuItem removeSong = new JMenuItem("Remove Song From Playlist");
        removeSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Song selectedSong = musicPlayer.getCurrentSong();

                if (selectedSong != null){
                    musicPlayer.deleteSongFromPlaylist(selectedSong);

                    addSongsToPlaylistWindow();
                }
            }
        });
        songMenu.add(removeSong);


        JMenuItem showAllSongs = new JMenuItem("Show All Songs");
        showAllSongs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Song> playlist = new ArrayList<>();
                File directory = new File("src/assets");
                FilenameFilter mp3Filter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".mp3");
                    }
                };
                // Get the list of .mp3 files
                File[] mp3Files = directory.listFiles(mp3Filter);
                if (mp3Files == null) {
                    System.out.println("Error reading the directory or no mp3 files found.");
                    return;
                }
                // Create the output file in the same directory
                File outputFile = new File(directory, "ALL_SONGS.txt");
                try (FileWriter writer = new FileWriter(outputFile)) {
                    for (File mp3File : mp3Files) {
                        writer.write(mp3File.getPath() + System.lineSeparator());
                        Song song = new Song(mp3File.getAbsolutePath());
                        playlist.add(song);
                    }
                    System.out.println("MP3 file paths have been written to " + outputFile.getAbsolutePath());
                } catch (IOException exc) {
                    System.out.println("An error occurred while writing to the file.");
                    exc.printStackTrace();
                }
                musicPlayer.setPlaylist(playlist);
                addSongsToPlaylistWindow();
            }
        });
        songMenu.add(showAllSongs);

        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        createPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new MusicPlaylistDialog(MusicPlayerGUI.this).setVisible(true);
            }
        });
        playlistMenu.add(createPlaylist);

        JMenuItem savePlaylist = new JMenuItem("Save Current Playlist");
        savePlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("Playlist", "txt"));
                jFileChooser.setCurrentDirectory(new File("src/assets"));

                int result = jFileChooser.showSaveDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    if (!selectedFile.getName().endsWith(".txt")) {
                        selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
                    }
                    musicPlayer.saveCurrentPlaylist(selectedFile);
                }
            }
        });
        playlistMenu.add(savePlaylist);

        JMenuItem deletePlaylist = new JMenuItem("Delete Playlist");
        deletePlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deletePlaylistDialog();
            }
        });
        playlistMenu.add(deletePlaylist);

        JMenuItem loadPlaylist = new JMenuItem("Load Playlist");
        loadPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("Playlist", "txt"));
                jFileChooser.setCurrentDirectory(new File("src/assets"));

                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    // stop the music
                    musicPlayer.stopSong();

                    // load playlist
                    musicPlayer.loadPlaylist(selectedFile);

                    addSongsToPlaylistWindow();
                }
            }
        });
        playlistMenu.add(loadPlaylist);


        add(toolBar);
    }

    private void addPlaybackBtns(){
        playbackBtns = new JPanel();
        playbackBtns.setBounds(0, 435, getWidth() - 10, 80);
        playbackBtns.setBackground(null);

        // previous button
        JButton prevButton = new JButton("⏮");
        prevButton.setForeground(TEXT_COLOR);
        prevButton.setFont(new Font("Dialog", Font.PLAIN, 30));
        prevButton.setBorderPainted(false);
        prevButton.setBackground(null);
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // go to the previous song
                musicPlayer.prevSong();
            }
        });
        playbackBtns.add(prevButton);

        // play button
        JButton playButton = new JButton("▶");
        playButton.setForeground(TEXT_COLOR);
        playButton.setFont(new Font("Dialog", Font.PLAIN, 40));
        playButton.setBorderPainted(false);
        playButton.setBackground(null);
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePauseButtonDisablePlayButton();
                musicPlayer.playCurrentSong();
            }
        });
        playbackBtns.add(playButton);

        JButton pauseButton = new JButton("❚❚");
        pauseButton.setForeground(TEXT_COLOR);
        pauseButton.setFont(new Font("Dialog", Font.PLAIN, 40));
        pauseButton.setBorderPainted(false);
        pauseButton.setBackground(null);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePlayButtonDisablePauseButton();
                musicPlayer.pauseSong();
            }
        });
        playbackBtns.add(pauseButton);

        JButton nextButton = new JButton("⏭");
        nextButton.setForeground(TEXT_COLOR);
        nextButton.setFont(new Font("Dialog", Font.PLAIN, 30));
        nextButton.setBorderPainted(false);
        nextButton.setBackground(null);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                musicPlayer.nextSong();
            }
        });
        playbackBtns.add(nextButton);

        add(playbackBtns);
    }

    public void updateSongTitleAndArtist(Song song){
        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());
    }
    public void enablePauseButtonDisablePlayButton(){
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        playButton.setVisible(false);
        playButton.setEnabled(false);

        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);
    }
    public void enablePlayButtonDisablePauseButton(){
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        playButton.setVisible(true);
        playButton.setEnabled(true);

        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);
    }

    private void deletePlaylistDialog() {
        //фильтруем тхт файлы
        File directory = new File("src/assets");
        var playlistsArr = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        });
        //добавляем их в список из которого будем выбирать
        JComboBox<File> playlistsBox = new JComboBox<>();
        for (File playlistFile : playlistsArr) {
            playlistsBox.addItem(playlistFile);
        }
        //интерфейс
        JPanel panel = new JPanel();
        panel.add(new JLabel("Select playlist to delete:"));
        panel.add(playlistsBox);
        //выбираем и нажимаем ОК
        int result = JOptionPane.showConfirmDialog(this, panel, "Delete Playlist", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            File selectedPlaylist = (File) playlistsBox.getSelectedItem();
            if (selectedPlaylist != null) {
                //удаляем файл если он выбран и нажали ОК
                selectedPlaylist.delete();
            }
        }
    }
}








