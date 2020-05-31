// Copyright (c) 2020 Ryan Richard

package rr.hikvisiondownloadassistant;

import rr.hikvisiondownloadassistant.Model.SearchMatchItem;

import java.io.IOException;
import java.util.List;
import java.util.logging.LogManager;

import static rr.hikvisiondownloadassistant.DateConverter.dateToLocalHumanString;

class Main {

    public static void main(String[] commandLineArguments) throws IOException, InterruptedException {
        LogManager.getLogManager().reset(); // disable all logging for now, since natty logs a bunch

        Options options = new Options(commandLineArguments);
        IsapiRestClient restClient = new IsapiRestClient(options.getHost(), options.getUsername(), options.getPassword());

        System.err.println("Getting photos and videos from \"" +
                dateToLocalHumanString(options.getFromDate()) +
                "\" to \"" +
                dateToLocalHumanString(options.getToDate()) + "\"\n");

        List<SearchMatchItem> videos = restClient.searchVideos(options.getFromDate(), options.getToDate());
        List<SearchMatchItem> photos = restClient.searchPhotos(options.getFromDate(), options.getToDate());

        if (photos.isEmpty() && videos.isEmpty()) {
            System.err.println("No photos or videos within that time/date range found");
            return;
        }

        new OutputFormatter(options, videos, photos).printResults();
        System.err.println("\nFound " + videos.size() + " videos and " + photos.size() + " photos");
    }

}
