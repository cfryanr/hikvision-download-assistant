// Copyright (c) 2020 Ryan Richard

package rr.hikvisiondownloadassistant;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class Model {

    public static final int VIDEOS_TRACK_ID = 101;
    public static final int PHOTOS_TRACK_ID = 103;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CMSearchDescription {
        @Builder.Default
        private String searchID = "search"; // supposed to be a guid in format ISO 9834-8, e.g. XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX numbers and capital letters

        private int maxResults;

        private int searchResultPosition;

        @JacksonXmlElementWrapper(localName = "trackIDList")
        private List<Integer> trackID; // 101 is the main stream (videos), 102 is substream, 103 is the third stream (photos)

//        @JacksonXmlElementWrapper(localName = "contentTypeList")
//        private List<String> contentType; // video, audio, metadata (for photos), text, mixed, other

//        private Metadata metadataList; // "//metadata.psia.org/VideoMotion" or when searching photos "//recordType.meta.std-cgi.com/MOTION"

        @JacksonXmlElementWrapper(localName = "timeSpanList")
        private List<TimeSpan> timeSpan;
    }

    @Getter
    @NoArgsConstructor
    public static class CMSearchResult {
        private String version; // e.g. 2.0
        private String searchID;
        private boolean responseStatus;
        private String responseStatusStrg; // e.g. OK, e.g. MORE (when paginating), e.g. NO MATCHES (for empty result)
        private long numOfMatches;
        private List<SearchMatchItem> matchList;
    }

    @Getter
    @NoArgsConstructor
    public static class SearchMatchItem {
        private String sourceID; // e.g. {0000000000-0000-0000-0000-000000000000}
        private int trackID; // e.g. 101
        private TimeSpan timeSpan;
        private MediaSegmentDescriptor mediaSegmentDescriptor;
        private Metadata metadataMatches;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSpan {
        private String startTime; // e.g. 2020-05-29T04:57:49Z
        private String endTime; // e.g. 2020-05-29T04:58:05Z
    }

    @Getter
    @NoArgsConstructor
    public static class MediaSegmentDescriptor {
        private String contentType; // e.g. video or picture

        private String codecType; // e.g. H.264-BP or jpeg

        // e.g. rtsp://192.168.1.64/Streaming/tracks/101/?starttime=20200529T045749Z&amp;endtime=20200529T045805Z&amp;name=ch01_00000000000000613&amp;size=2901372
        // e.g. http://192.168.1.64/ISAPI/Streaming/tracks/103/?starttime=20200531T012016Z&amp;endtime=20200531T012016Z&amp;name=ch01_00000000001026401&amp;size=600489
        private String playbackURI;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        private String metadataDescriptor; // e.g. recordType.meta.hikvision.com/AllEvent
    }

}
