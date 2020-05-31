// Copyright (c) 2020 Ryan Richard

package rr.hikvisiondownloadassistant;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
public class Options {

    private String host;
    private String username;
    private String password;
    private Date fromDate;
    private Date toDate;

    public Options(String[] commandLineArguments) {
        // TODO proper argument parsing
        // TODO verbose mode for logging request/response cycles
        host = commandLineArguments[0];
        username = commandLineArguments[1];
        password = commandLineArguments[2];
        fromDate = getDateFromNaturalLanguage(commandLineArguments[3]);
        toDate = getDateFromNaturalLanguage(commandLineArguments[4]); // TODO default to now
    }

    private Date getDateFromNaturalLanguage(String naturalLanguageTimeDescription) {
        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(naturalLanguageTimeDescription);
        if (groups.size() != 1) {
            throw new RuntimeException("please describe one date/time for a time option");
        }
        DateGroup dateGroup = groups.get(0);
        if (dateGroup.isRecurring()) {
            throw new RuntimeException("please do not use recurring date/times");
        }
        if (dateGroup.getDates().size() != 1) {
            throw new RuntimeException("please describe one date/time for a time option");
        }
        return dateGroup.getDates().get(0);
    }

}
