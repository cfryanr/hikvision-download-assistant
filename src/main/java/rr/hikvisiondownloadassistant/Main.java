// Copyright (c) 2020 Ryan Richard

package rr.hikvisiondownloadassistant;

import picocli.CommandLine;
import rr.hikvisiondownloadassistant.Model.SearchMatchItem;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.LogManager;

import static rr.hikvisiondownloadassistant.DateConverter.dateToApiString;
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

        DateConverter.setLegacyTimezoneMode(options.isLegacyTimeProcess());

        IsapiRestClient restClient = new IsapiRestClient(options.getHost(), options.getUsername(), options.getPassword());
        Date fromDate = options.getFromDate();
        Date toDate = options.getToDate();

        if (!options.isQuiet()) {
            System.err.println("Getting photos and videos from \"" +
                    dateToLocalHumanString(fromDate) +
                    "\" to \"" +
                    dateToLocalHumanString(toDate) + "\"\n");
        }

        String fromDateStr = dateToApiString(fromDate);
        String toDateStr = dateToApiString(toDate);

        List<SearchMatchItem> results = restClient.searchMedia(fromDateStr, toDateStr, options.getChannelId());

        if (results.isEmpty() && !options.isQuiet()) {
            System.err.println("No files within that time/date range found");
            return;
        }

        new OutputFormatter(options, results).printResults();

        if (!options.isQuiet()) {
            System.err.println("\nFound " + results.size() + " files");
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
