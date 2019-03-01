package samples;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.AbstractService;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.GregorianCalendar;

/*
 * Copyright(c) VMware Inc. 2019. All rights reserved.
 */
@Component
public class CalendarService extends AbstractService<Request<String>, Response<String>> {

    // define the channel the service operates on,.
    public static final String Channel = "calendar-service";
    private Calendar calendar;

    CalendarService() {
        super(CalendarService.Channel);
        this.calendar = GregorianCalendar.getInstance();
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

    private void handleTime(Request request) {

//        String time = calendar.get(GregorianCalendar.HOUR)
//                + ":" + calendar.get(GregorianCalendar.MINUTE)
//                + " " + calendar.get(GregorianCalendar.AM_PM);

        Response<String> response = new Response<>(request.getId(), "TIME");
        this.sendResponse(response, request.getId());
    }

    private void handleDate(Request request) {

//        String date = calendar.get(GregorianCalendar.HOUR)
//                + ":" + calendar.get(GregorianCalendar.MINUTE)
//                + " " + calendar.get(GregorianCalendar.AM_PM);

        Response<String> response = new Response<>(request.getId(), "DATE");
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