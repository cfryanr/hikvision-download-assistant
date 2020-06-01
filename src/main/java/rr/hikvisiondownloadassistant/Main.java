// Copyright (c) 2020 Ryan Richard

package rr.hikvisiondownloadassistant;

import picocli.CommandLine;
import rr.hikvisiondownloadassistant.Model.SearchMatchItem;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.LogManager;

import static rr.hikvisiondownloadassistant.DateConverter.dateToLocalHumanString;

class Main {

    public static void main(String[] commandLineArguments) {
        LogManager.getLogManager().reset(); // disable all logging for now, since natty logs a bunch

        try {
            run(commandLineArguments);
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] commandLineArguments) throws IOException, InterruptedException {
        Options options = parseCommandLineArguments(commandLineArguments);

        IsapiRestClient restClient = new IsapiRestClient(options.getHost(), options.getUsername(), options.getPassword());
        Date fromDate = options.getFromDate();
        Date toDate = options.getToDate();

        if (!options.isQuiet()) {
            System.err.println("Getting photos and videos from \"" +
                    dateToLocalHumanString(fromDate) +
                    "\" to \"" +
                    dateToLocalHumanString(toDate) + "\"\n");
        }

        List<SearchMatchItem> videos = restClient.searchVideos(fromDate, toDate);
        List<SearchMatchItem> photos = restClient.searchPhotos(fromDate, toDate);

        if (photos.isEmpty() && videos.isEmpty() && !options.isQuiet()) {
            System.err.println("No photos or videos within that time/date range found");
            return;
        }

        new OutputFormatter(options, videos, photos).printResults();

        if (!options.isQuiet()) {
            System.err.println("\nFound " + videos.size() + " videos and " + photos.size() + " photos");
        }

    }

    private static Options parseCommandLineArguments(String[] commandLineArguments) {
        Options options = new Options();
        CommandLine commandLine = new CommandLine(options);
        commandLine.parseArgs(commandLineArguments);

        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            System.exit(0);
        } else if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(System.out);
            System.exit(0);
        }

        return options;
    }

}
