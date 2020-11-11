package samples.pong;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.Transaction;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.AbstractService;
import com.vmware.bifrost.core.util.ClassMapper;
import org.springframework.stereotype.Component;
import samples.calendar.CalendarService;

import java.util.UUID;

@Component
public class PongService extends AbstractService<Request<String>, Response<String>> {
    // define the channel the service operates on,.
    public static final String Channel = "services-PongService";

    PongService() {
        super(PongService.Channel);
    }

    protected void handleServiceRequest(Request request, Message busMessage) {

        // which command shall we run?
        switch(request.getRequest()) {
            case PongRequestType.Basic:
                this.handleBasicPongRequest(request);
                break;

            case PongRequestType.Full:
                this.handleFullPongRequest(request);
                break;
        }
    }

    /**
     * Handle requests for basic pong needs
     * @param request The request being sent over the bus.
     */
    private void handleBasicPongRequest(Request request) {

        // prepare and send a basic response.
        Response<String> response = new Response<>(request.getId(), "Fabric Pong (Basic): Pong: " + request.getPayload());
        this.sendResponse(response, request.getId());
    }

    /**
     * Handle requests for full pong needs.
     * @param request the request being sent over the bus.
     */
    private void handleFullPongRequest(Request request) {

        UUID transactionId = request.getId();

        // call our calendar service and get a date and time, in two separate synchronous calls via transaction, pass
        // in our request ID as the transaction ID, so it can be tracked correctly across service calls.
        Transaction transaction = bus.createTransaction(Transaction.TransactionType.SYNC, "calendar-transaction", transactionId);

        // Queue up requests for time and date via Calendar Service.
        transaction.sendRequest(CalendarService.Channel, new Request<String>(transactionId, "date")); // request date
        transaction.sendRequest(CalendarService.Channel, new Request<String>(transactionId, "time")); // request time

        // register complete handler for transaction.
        transaction.onComplete(
                (Message[] responses) -> {

                    // concatenate service responses into a string.
                    StringBuilder dateAndTime = new StringBuilder();
                    for(Message msg: responses) {
                        String calendarResponse = ClassMapper.CastPayload(String.class, (Response)msg.getPayload());
                        dateAndTime.append(calendarResponse + " "); // add each response together.
                    }

                    // prepare a response, with our date and time requests stuck together as a more elaborate response.
                    Response<String> response = new Response<>(request.getId(),
                            "Fabric Pong (Full): Calendar: " + dateAndTime.toString() + " / Pong: " + request.getPayload());
                    this.sendResponse(response, request.getId());
                }
        );

        // commit transaction.
        transaction.commit();
    }
}

abstract class PongRequestType {
    static final String Basic = "basic";
    static final String Full = "full";
}
