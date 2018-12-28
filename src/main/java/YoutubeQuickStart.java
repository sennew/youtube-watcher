import com.github.axet.vget.VGet;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

public class YoutubeQuickStart {
    private static final String[] YOUTUBE_PLAYLISTS = {
            "PLViXnSWCsCRdjiZSVZaV0A-48XL7HZRx4"
    };

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;
    /** Youtube data API */
    private YouTube youtubeDataApi;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (final Throwable t){
            t.printStackTrace();
            System.exit(1);
        }
    }

    public static YouTube getYoutubeService() throws Exception {
        return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, null)
                .setApplicationName("YoutubePlaylist")
                .build();
    }

    private static final String YOUTUBE_PLAYLIST_PART = "snippet";
    private static final String YOUTUBE_PLAYLIST_FIELDS = "items(id,snippet(title,resourceId/videoId))";

    public static void main(final String[] args) {
        final String YOUTUBE_CHANNEL_TO_WATCH = "PLViXnSWCsCRdjiZSVZaV0A-48XL7HZRx4";
        final String DOWNLOAD_DIR = "C:\\";

        PlaylistItemListResponse playlistItemListResponse = null;

        try {
            final YouTube youTubeApi = getYoutubeService();

            /**
             * TODO swen je kan miximaal 50 nummers per call uitgelezen worden je zal dus met paging moeten werken om meerdere op te halen.
             */
            playlistItemListResponse = youTubeApi.playlistItems()
                    .list(YOUTUBE_PLAYLIST_PART)
                    .setPlaylistId(YOUTUBE_CHANNEL_TO_WATCH)
                    .setFields(YOUTUBE_PLAYLIST_FIELDS)
                    .setKey(ApiKey.YOUTUBE_API_KEY)
                    .setMaxResults(50L)
                    .execute();

        } catch (final Exception e) {
            e.printStackTrace();
        }

        if (playlistItemListResponse != null){
            final Map<String, String> playListMap = playlistItemListResponse.getItems().stream()
                                    .map(PlaylistItem::getSnippet)
                                    .collect(Collectors.toMap(
                                            PlaylistItemSnippet::getTitle,
                                            entry -> String.format("https://www.youtube.com/watch?v=%s",entry.getResourceId().getVideoId())
                                    ));

            playListMap.entrySet().stream()
                    .map(entry -> String.format("Title: %s -- Url: %s", entry.getKey(), entry.getValue()))
                    .forEach(System.out::println);

            try {
                final VGet v  = new VGet(new URL(playListMap.entrySet().iterator().next().getValue()), new File(DOWNLOAD_DIR));
                v.download();
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
