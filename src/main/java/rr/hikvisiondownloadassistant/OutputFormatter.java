// Copyright (c) 2020 Ryan Richard

package rr.hikvisiondownloadassistant;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import rr.hikvisiondownloadassistant.Model.SearchMatchItem;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static rr.hikvisiondownloadassistant.DateConverter.*;

@RequiredArgsConstructor
public class OutputFormatter {

    private enum MediaType {
        PHOTO,
        VIDEO
    }

    private final Options options;
    private final List<SearchMatchItem> videos;
    private final List<SearchMatchItem> photos;

    // TODO support outputting videos as a VLC playlist file for easy previewing?
    // TODO support json output
    public void printResults() {
        String tableColumnDelimiter = "|";
        System.err.println("Type|Trigger|Start|End|Curl");
        System.err.println("-----------------------------");
        List<OutputRow> rows = convertToOutputRows(MediaType.VIDEO, videos);
        rows.addAll(convertToOutputRows(MediaType.PHOTO, photos));
        rows.sort(Comparator.comparing(OutputRow::getStartTime));
        rows.stream().map(OutputRow::toTextTableRow).forEach(row -> {
            System.out.println(String.join(tableColumnDelimiter, row));
        });
    }

    private List<OutputRow> convertToOutputRows(MediaType mediaType, List<SearchMatchItem> items) {
        return items.stream().map(item -> new OutputRow(
                        item,
                        mediaType,
                        apiStringToDate(item.getTimeSpan().getStartTime()),
                        apiStringToDate(item.getTimeSpan().getEndTime())
                )
        ).collect(Collectors.toList());
    }

    @Data
    private class OutputRow {

        @Getter(value = AccessLevel.PRIVATE)
        private final SearchMatchItem item;

        private final MediaType mediaType;
        private final Date startTime;
        private final Date endTime;

        public List<String> toTextTableRow() {
            return List.of(
                    mediaType.toString(),
                    getEventType(),
                    dateToLocalString(startTime),
                    dateToLocalString(endTime),
                    getCurlCommand()
            );
        }

        private String getEventType() {
            return item.getMetadataMatches().getMetadataDescriptor().replace("recordType.meta.hikvision.com/", "").toUpperCase();
        }

        private String getPlaybackURI() {
            return item.getMediaSegmentDescriptor().getPlaybackURI();
        }

        public String getCurlCommand() {
            return mediaType == MediaType.PHOTO ? formatPhotoCurlCommand() : formatVideoCurlCommand();
        }

        private String formatVideoCurlCommand() {
            return String.join(" ", List.of(
                    "curl",
                    "-f",
                    "-X GET",
                    "-d '<downloadRequest><playbackURI>" + getPlaybackURI().replace("&", "&amp;") + "</playbackURI></downloadRequest>'",
                    "--anyauth --user " + options.getUsername() + ":" + options.getPassword(),
                    "http://" + options.getHost() + "/ISAPI/ContentMgmt/download",
                    "--output " + dateToLocalFilenameString(startTime) + ".mp4"
            ));
        }

        private String formatPhotoCurlCommand() {
            return String.join(" ", List.of(
                    "curl",
                    "-f",
                    "--anyauth --user " + options.getUsername() + ":" + options.getPassword(),
                    "'" + getPlaybackURI() + "'",
                    "--output " + dateToLocalFilenameString(startTime) + "." + item.getMediaSegmentDescriptor().getCodecType()
            ));
        }

    }

}
