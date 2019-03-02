package samples;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.AbstractService;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/*
 * Copyright(c) VMware Inc. 2019. All rights reserved.
 */
@Component
public class CalendarService extends AbstractService<Request<String>, Response<String>> {

    // define the channel the service operates on,.
    public static final String Channel = "calendar-service";

    CalendarService() {
        super(CalendarService.Channel);
    }
    protected void handleServiceRequest(Request request, Message busMessage) {
        // which command shall we run?
        switch(request.getCommand()) {
            case SampleCommand.Date:
                handleDate(request);
                break;

            case SampleCommand.Time:
                handleTime(request);
                break;

            default:
                handleUnknown(request);
        }
    }

    private String formatCalendar(String format) {
        Calendar calendar = GregorianCalendar.getInstance();
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        return fmt.format(calendar.getTime());
    }

    private void handleTime(Request request) {

        Response<String> response = new Response<>(request.getId(), formatCalendar("hh:mm:ss a"));
        this.sendResponse(response, request.getId());
    }

    private void handleDate(Request request) {

        Response<String> response = new Response<>(request.getId(), formatCalendar("yyyy-MM-dd"));
        this.sendResponse(response, request.getId());
    }

    private void handleUnknown(Request request) {

        Response<String> response = new Response<>(request.getId(), "Unknown Command");
        this.sendResponse(response, request.getId());
    }
}

abstract class SampleCommand {
    static final String Time = "time";
    static final String Date = "date";
}