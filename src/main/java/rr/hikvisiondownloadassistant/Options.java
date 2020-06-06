// Copyright (c) 2020 Ryan Richard

package rr.hikvisiondownloadassistant;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import lombok.Getter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Date;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Command(
        name = "java -jar hikvision-download-assistant.jar",
        version = "1.1.0",
        mixinStandardHelpOptions = true,
        usageHelpAutoWidth = true
)
public class Options {

    @Parameters(
            paramLabel = "HOST",
            description = "Connect to this host or IP address to perform search."
    )
    private String host;

    @Parameters(
            paramLabel = "USERNAME",
            description = "Use this username when connecting to perform search."
    )
    private String username;

    @Parameters(
            paramLabel = "PASSWORD",
            description = "Use this password when connecting to perform search."
    )
    private String password;

    @Option(
            names = {"-f", "--from-time"},
            defaultValue = "24 hours ago",
            description = "Search starting from this time, entered using English natural language. Defaults to '${DEFAULT-VALUE}'."
    )
    @Getter(value = PRIVATE)
    private String fromTime;

    @Option(
            names = {"-t", "--to-time"},
            defaultValue = "now",
            description = "Search up to this time, entered using English natural language. Defaults to '${DEFAULT-VALUE}'."
    )
    @Getter(value = PRIVATE)
    private String toTime;

    @Option(
            names = {"-p", "--output-password"},
            description = "Output a different password in the printed curl commands, e.g. '$PASSWORD'."
    )
    private String outputPassword;

    @Option(
            names = {"-u", "--output-username"},
            description = "Output a different username in the printed curl commands, e.g. '$USERNAME'."
    )
    private String outputUsername;

    @Option(
            names = {"-d", "--table-delimiter"},
            defaultValue = "|",
            description = "The column delimiter for table output. Defaults to '${DEFAULT-VALUE}'."
    )
    private String tableDelimiter;

    @Option(
            names = {"-q", "--quiet"},
            description = "Suppress header and footer."
    )
    private boolean quiet;

    enum OutputFormat {
        json,
        table
    }

    @Option(
            names = {"-o", "--output"},
            defaultValue = "table",
            description = "Output format. Can be 'table' or 'json'. Defaults to '${DEFAULT-VALUE}'."
    )
    private OutputFormat outputFormat;

    public Date getFromDate() {
        return getDateFromNaturalLanguage(fromTime);
    }

    public Date getToDate() {
        return getDateFromNaturalLanguage(toTime);
    }

    private Date getDateFromNaturalLanguage(String naturalLanguageTimeDescription) {
        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(naturalLanguageTimeDescription);
        if (groups.size() != 1) {
            throw new RuntimeException("Please describe one date/time for a time option");
        }
        DateGroup dateGroup = groups.get(0);
        if (dateGroup.isRecurring()) {
            throw new RuntimeException("Please do not use recurring date/times");
        }
        if (dateGroup.getDates().size() != 1) {
            throw new RuntimeException("Please describe one date/time for a time option");
        }
        return dateGroup.getDates().get(0);
    }

}
