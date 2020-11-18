/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples.store;

import com.vmware.transport.bridge.Request;
import com.vmware.transport.bridge.Response;
import com.vmware.transport.bus.model.Message;
import com.vmware.transport.bus.store.BusStoreApi;
import com.vmware.transport.bus.store.model.BusStore;
import com.vmware.transport.core.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/samples/motd")
@Service("MessageOfTheDayService")
public class MessageOfTheDayService extends AbstractService<Request<MessageItem>, Response<MessageItem>> {

    private static final String MESSAGE_OF_THE_DAY = "messageOfTheDay";

    // define the channel the service operates on,.
    public static final String Channel = "motd-service";

    @Autowired
    protected BusStoreApi storeManager;

    private BusStore<String, MessageItem> store;

    public MessageOfTheDayService() {
        super(MessageOfTheDayService.Channel);
    }

    @Override
    public void initialize() {

        super.initialize();

        store = storeManager.createStore("messageOfTheDayStore");
        // Set the value type so that store updates coming from the UI
        // can be deserialized correctly.
        store.setValueType(MessageItem.class);

        MessageItem item = new MessageItem();
        item.message = "Default message of the day";
        item.from = "Admin";

        store.getBusStoreInitializer().add(MESSAGE_OF_THE_DAY, item).done();
    }

    @Override
    protected void handleServiceRequest(Request<MessageItem> request, Message busMessage) throws Exception {
        try {
            switch (request.getRequest()) {
                case "getMessage":
                    Response<MessageItem> resp = new Response<>(request.getId(), store.get(MESSAGE_OF_THE_DAY));
                    sendResponse(resp, request.getId());
                    break;
                case "setMessage":
                    MessageItem newMsg = mapper.convertValue(request.getPayload(), MessageItem.class);
                    store.put(MESSAGE_OF_THE_DAY, newMsg, "setMessageOfTheDay");
                    resp = new Response<>(request.getId(), newMsg);
                    sendResponse(resp, request.getId());
                    break;
            }
        } catch (Exception ex) {
            logErrorMessage("Failed to process request", ex.getMessage());
        }
    }

    @PostMapping("setMessage")
    public MessageItem setMessageOfTheDay(
          @RequestBody MessageItem msg) {
        store.put(MESSAGE_OF_THE_DAY, msg, "setMessageOfTheDay");
        return msg;
    }

    @GetMapping("getMessage")
    public MessageItem getMessage() {
        return store.get(MESSAGE_OF_THE_DAY);
    }
}
