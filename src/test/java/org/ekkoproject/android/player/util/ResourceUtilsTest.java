package org.ekkoproject.android.player.util;

import static org.junit.Assert.assertEquals;

import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ResourceUtilsTest {
    @Test
    public void testYouTubeExtractVideoId() {
        final String id = "dQw4w9WgXcQ";
        final String[] urls = new String[] {"https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                "http://www.youtube.com/watch?v=dQw4w9WgXcQ&feature=related", "http://youtu.be/dQw4w9WgXcQ",
                "http://www.youtube.com/embed/dQw4w9WgXcQ", "http://www.youtube.com/v/dQw4w9WgXcQ",
                "https://youtube.googleapis.com/v/dQw4w9WgXcQ", "http://m.youtube.com/watch?v=dQw4w9WgXcQ",
                "http://www.youtube.com/ytscreeningroom?v=dQw4w9WgXcQ",};
        for (final String url : urls) {
            assertEquals(url, id, ResourceUtils.youtubeExtractVideoId(Uri.parse(url)));
        }
    }
}
