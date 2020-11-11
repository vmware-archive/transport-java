/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.transport.bus.store;

import com.vmware.transport.bridge.Request;
import com.vmware.transport.bus.EventBus;
import com.vmware.transport.bus.EventBusImpl;
import com.vmware.transport.bus.model.MessageObject;
import com.vmware.transport.bus.model.MessageType;
import com.vmware.transport.bus.model.MonitorObject;
import com.vmware.transport.bus.model.MonitorType;
import com.vmware.transport.bus.store.model.BusStore;
import com.vmware.transport.bus.store.model.BusStoreError;
import com.vmware.transport.bus.store.model.CloseStoreRequest;
import com.vmware.transport.bus.store.model.OpenStoreRequest;
import com.vmware.transport.bus.store.model.StoreContent;
import com.vmware.transport.bus.store.model.StoreContentResponse;
import com.vmware.transport.bus.store.model.TestStoreItem;
import com.vmware.transport.bus.store.model.UpdateStoreRequest;
import com.vmware.transport.bus.store.model.UpdateStoreResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
      EventBusImpl.class,
      GalacticStoreService.class,
      StoreManager.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GalacticStoreServiceTest {

    @Autowired
    private GalacticStoreService galacticStoreService;

    @Autowired
    private StoreManager storeManager;

    @Autowired
    private EventBus eventBus;

    @Before
    public void before() {
        galacticStoreService.initialize();

        BusStore<String, TestStoreItem> store = storeManager.createStore("store1");
        store.setValueType(TestStoreItem.class);

        store.getBusStoreInitializer()
              .add("item1", new TestStoreItem("item1", 1))
              .add("item2", new TestStoreItem("item2", 1))
              .done();

        BusStore<String, String> store2 = storeManager.createStore("store2");
        store2.getBusStoreInitializer()
              .add("item1", "gold")
              .add("item2", "silver")
              .done();
    }

    @Test
    public void testOpenStore() {
        String syncChannel1 = addNewGalacticStoreSyncChannel();
        List<Object> channel1Responses = new ArrayList<>();
        List<BusStoreError> channe1Errors = new ArrayList<>();
        listenSyncChannel(syncChannel1, channel1Responses, channe1Errors);

        String syncChannel2 = addNewGalacticStoreSyncChannel();
        List<Object> channel2Responses = new ArrayList<>();
        List<BusStoreError> channel2Errors = new ArrayList<>();
        listenSyncChannel(syncChannel2, channel2Responses, channel2Errors);

        sendOpenStoreRequest(syncChannel1, "store1");
        Assert.assertEquals(channel1Responses.size(), 1);
        Assert.assertEquals(channe1Errors.size(), 0);

        validateStoreContent(channel1Responses.get(0), "store1");
        Assert.assertEquals(channel2Responses.size(), 0);
        Assert.assertEquals(channel2Errors.size(), 0);

        sendOpenStoreRequest(syncChannel2, "store1");
        Assert.assertEquals(channel1Responses.size(), 1);

        Assert.assertEquals(channel2Responses.size(), 1);
        validateStoreContent(channel2Responses.get(0), "store1");

        BusStore<String, String> uninitlizedStore = storeManager.createStore("store3");
        sendOpenStoreRequest(syncChannel1, "store3");

        // Verify store content is not sent for non initialized stores
        Assert.assertEquals(channel1Responses.size(), 1);

        // Initialize the store and verify that the content is send to sync channels
        uninitlizedStore.initialize();
        Assert.assertEquals(channel1Responses.size(), 2);
        validateStoreContent(channel1Responses.get(1), "store3");
    }

    @Test
    public void testOpenStoreWithInvalidArgs() {
        String syncChannel1 = addNewGalacticStoreSyncChannel();
        List<Object> channel1Responses = new ArrayList<>();
        List<BusStoreError> channe1Errors = new ArrayList<>();
        listenSyncChannel(syncChannel1, channel1Responses, channe1Errors);

        String syncChannel2 = addNewGalacticStoreSyncChannel();
        List<Object> channel2Responses = new ArrayList<>();
        List<BusStoreError> channel2Errors = new ArrayList<>();
        listenSyncChannel(syncChannel2, channel2Responses, channel2Errors);

        Request<Object> request = new Request<>(
              GalacticStoreService.GalacticStoreCommands.OpenStore,
              null);
        eventBus.sendRequestMessage(syncChannel1, request);

        Assert.assertEquals(channe1Errors.size(), 1);

        Assert.assertEquals(channe1Errors.get(0).storeName, null);
        Assert.assertEquals(channe1Errors.get(0).errorMsg, "Invalid OpenStoreRequest: null request");

        request = new Request<>(
              GalacticStoreService.GalacticStoreCommands.OpenStore,
              "invalid-arg");
        eventBus.sendRequestMessage(syncChannel1, request);
        Assert.assertEquals(channe1Errors.size(), 2);
        Assert.assertEquals(channe1Errors.get(1).storeName, null);
        Assert.assertTrue(channe1Errors.get(1).errorMsg.startsWith("Invalid OpenStoreRequest:"));

        sendOpenStoreRequest(syncChannel1, "invalid-store");
        Assert.assertEquals(channe1Errors.size(), 3);
        Assert.assertEquals(channe1Errors.get(2).storeName, "invalid-store");
        Assert.assertEquals(channe1Errors.get(2).errorMsg, "Cannot open non-existing store: invalid-store");

        sendOpenStoreRequest(syncChannel1, null);
        Assert.assertEquals(channe1Errors.size(), 4);
        Assert.assertEquals(channe1Errors.get(3).storeName, null);
        Assert.assertEquals(channe1Errors.get(3).errorMsg, "Invalid OpenStoreRequest: null storeId");

        request = new Request<>(
              (String )null,
              null);
        eventBus.sendRequestMessage(syncChannel1, request);

        Assert.assertEquals(channel1Responses.size(), 0);
        Assert.assertEquals(channel2Responses.size(), 0);
        Assert.assertTrue(channel2Errors.isEmpty());
    }

    @Test
    public void testStoreBackendUpdate() {
        String syncChannel1 = addNewGalacticStoreSyncChannel();
        List<Object> channel1Responses = new ArrayList<>();
        List<BusStoreError> channe1Errors = new ArrayList<>();
        listenSyncChannel(syncChannel1, channel1Responses, channe1Errors);

        String syncChannel2 = addNewGalacticStoreSyncChannel();
        List<Object> channel2Responses = new ArrayList<>();
        List<BusStoreError> channel2Errors = new ArrayList<>();
        listenSyncChannel(syncChannel2, channel2Responses, channel2Errors);


        sendOpenStoreRequest(syncChannel1, "store2");
        sendOpenStoreRequest(syncChannel2, "store2");

        storeManager.getStore("store2").put("item3", "bronze", "update");

        Assert.assertEquals(channel1Responses.size(), 2);
        validateStoreUpdateResponse(channel1Responses.get(1), "store2", "item3", "bronze", 2);
        Assert.assertEquals(channel2Responses.size(), 2);
        validateStoreUpdateResponse(channel2Responses.get(1), "store2", "item3", "bronze", 2);

        sendCloseStoreRequest(syncChannel1, "store2");
        storeManager.getStore("store2").put("item4", "copper", "update");
        Assert.assertEquals(channel1Responses.size(), 2);
        Assert.assertEquals(channel2Responses.size(), 3);
        validateStoreUpdateResponse(channel2Responses.get(2), "store2", "item4", "copper", 3);

        sendCloseStoreRequest(syncChannel2, "store2");
        storeManager.getStore("store2").put("item1", "copper", "update");
        Assert.assertEquals(channel1Responses.size(), 2);
        Assert.assertEquals(channel2Responses.size(), 3);
    }

    @Test
    public void testStoreUpdateRequest() {
        String syncChannel1 = addNewGalacticStoreSyncChannel();
        List<Object> channel1Responses = new ArrayList<>();
        List<BusStoreError> channe1Errors = new ArrayList<>();
        listenSyncChannel(syncChannel1, channel1Responses, channe1Errors);

        String syncChannel2 = addNewGalacticStoreSyncChannel();
        List<Object> channel2Responses = new ArrayList<>();
        List<BusStoreError> channel2Errors = new ArrayList<>();
        listenSyncChannel(syncChannel2, channel2Responses, channel2Errors);

        sendOpenStoreRequest(syncChannel1, "store1");
        sendOpenStoreRequest(syncChannel2, "store1");

        TestStoreItem newItem = new TestStoreItem("item5", 1);
        sendUpdateStoreRequest(syncChannel1, "store1", "item5", newItem);

        Assert.assertEquals(channel1Responses.size(), 2);
        validateStoreUpdateResponse(channel1Responses.get(1), "store1", "item5", newItem, 2);
        Assert.assertEquals(channel2Responses.size(), 2);
        validateStoreUpdateResponse(channel2Responses.get(1), "store1", "item5", newItem, 2);

        Assert.assertEquals(storeManager.getStore("store1").get("item5"), newItem);

        TestStoreItem item7 = new TestStoreItem("item7", 2);

        Map<String, Object> item7AsMap = new HashMap<>();
        item7AsMap.put("name", item7.name);
        item7AsMap.put("count", item7.count);
        item7AsMap.put("uuid", item7.uuid);

        sendUpdateStoreRequest(syncChannel1, "store1", "item7", item7AsMap);

        TestStoreItem serializedItem = (TestStoreItem) storeManager.getStore("store1").get("item7");
        Assert.assertEquals(serializedItem.name, item7.name);
        Assert.assertEquals(serializedItem.uuid, item7.uuid);

        Assert.assertEquals(channel1Responses.size(), 3);
        validateStoreUpdateResponse(channel1Responses.get(2), "store1", "item7", serializedItem, 3);

        sendOpenStoreRequest(syncChannel1, "store2");
        sendOpenStoreRequest(syncChannel2, "store2");

        Assert.assertEquals(channel1Responses.size(), 4);
        validateStoreContent(channel1Responses.get(3), "store2");

        sendUpdateStoreRequest(syncChannel1, "store2", "item1", null);
        Assert.assertEquals(channel1Responses.size(), 5);
        validateStoreUpdateResponse(channel1Responses.get(4), "store2", "item1", null, 2);

        Assert.assertEquals(channel2Responses.size(), 5);
        validateStoreUpdateResponse(channel2Responses.get(4), "store2", "item1", null, 2);

        BusStore<UUID, String> store3 = storeManager.createStore("store3");
        store3.setKeyType(UUID.class);
        store3.initialize();

        sendOpenStoreRequest(syncChannel1, "store3");

        Assert.assertEquals(channel1Responses.size(), 6);

        UUID itemId = UUID.randomUUID();
        sendUpdateStoreRequest(syncChannel1, "store3", itemId.toString(), "itemValue");
        Assert.assertEquals(channel1Responses.size(), 7);
        validateStoreUpdateResponse(channel1Responses.get(6), "store3", itemId, "itemValue", 2);
    }

    @Test
    public void testUpdateStoreWithInvalidArgs() {
        String syncChannel1 = addNewGalacticStoreSyncChannel();
        List<Object> channel1Responses = new ArrayList<>();
        List<BusStoreError> channe1Errors = new ArrayList<>();
        listenSyncChannel(syncChannel1, channel1Responses, channe1Errors);

        String syncChannel2 = addNewGalacticStoreSyncChannel();
        List<Object> channel2Responses = new ArrayList<>();
        List<BusStoreError> channel2Errors = new ArrayList<>();
        listenSyncChannel(syncChannel2, channel2Responses, channel2Errors);

        Request<Object> request = new Request<>(
              GalacticStoreService.GalacticStoreCommands.UpdateStore,
              null);
        eventBus.sendRequestMessage(syncChannel1, request);


        Assert.assertEquals(channe1Errors.size(), 1);

        Assert.assertEquals(channe1Errors.get(0).storeName, null);
        Assert.assertEquals(channe1Errors.get(0).errorMsg, "Invalid UpdateStoreRequest: null request");

        request = new Request<>(
              GalacticStoreService.GalacticStoreCommands.UpdateStore,
              "invalid-arg");
        eventBus.sendRequestMessage(syncChannel1, request);
        Assert.assertEquals(channe1Errors.size(), 2);
        Assert.assertEquals(channe1Errors.get(1).storeName, null);
        Assert.assertTrue(channe1Errors.get(1).errorMsg.startsWith("Invalid UpdateStoreRequest:"));

        sendUpdateStoreRequest(syncChannel1, "invalid-store", "item1", "newValue");
        Assert.assertEquals(channe1Errors.size(), 3);
        Assert.assertEquals(channe1Errors.get(2).storeName, "invalid-store");
        Assert.assertEquals(channe1Errors.get(2).errorMsg, "Cannot update non-existing store: invalid-store");

        sendUpdateStoreRequest(syncChannel1, "store1", "item1", "newValue");
        Assert.assertEquals(channe1Errors.size(), 4);
        Assert.assertEquals(channe1Errors.get(3).storeName, "store1");
        Assert.assertEquals(channe1Errors.get(3).itemId, "item1");
        Assert.assertTrue(channe1Errors.get(3).errorMsg.startsWith("Invalid UpdateStoreRequest:"));

        sendUpdateStoreRequest(syncChannel1, null, "item1", "newValue");
        Assert.assertEquals(channe1Errors.size(), 5);
        Assert.assertEquals(channe1Errors.get(4).storeName, null);
        Assert.assertEquals(channe1Errors.get(4).errorMsg, "Invalid UpdateStoreRequest: null storeId");

        Assert.assertEquals(channel1Responses.size(), 0);
        Assert.assertEquals(channel2Responses.size(), 0);
        Assert.assertTrue(channel2Errors.isEmpty());
    }

    @Test
    public void testCloseStoreWithInvalidArgs() {
        String syncChannel1 = addNewGalacticStoreSyncChannel();
        List<Object> channel1Responses = new ArrayList<>();
        List<BusStoreError> channe1Errors = new ArrayList<>();
        listenSyncChannel(syncChannel1, channel1Responses, channe1Errors);

        String syncChannel2 = addNewGalacticStoreSyncChannel();
        List<Object> channel2Responses = new ArrayList<>();
        List<BusStoreError> channel2Errors = new ArrayList<>();
        listenSyncChannel(syncChannel2, channel2Responses, channel2Errors);

        Request<Object> request = new Request<>(
              GalacticStoreService.GalacticStoreCommands.CloseStore,
              null);
        eventBus.sendRequestMessage(syncChannel1, request);


        Assert.assertEquals(channe1Errors.size(), 1);

        Assert.assertEquals(channe1Errors.get(0).storeName, null);
        Assert.assertEquals(channe1Errors.get(0).errorMsg, "Invalid CloseStoreRequest: null request");

        request = new Request<>(
              GalacticStoreService.GalacticStoreCommands.CloseStore,
              "invalid-arg");
        eventBus.sendRequestMessage(syncChannel1, request);
        Assert.assertEquals(channe1Errors.size(), 2);
        Assert.assertEquals(channe1Errors.get(1).storeName, null);
        Assert.assertTrue(channe1Errors.get(1).errorMsg.startsWith("Invalid CloseStoreRequest:"));

        sendCloseStoreRequest(syncChannel1, "invalid-store");
        Assert.assertEquals(channe1Errors.size(), 2);

        sendCloseStoreRequest(syncChannel1, null);
        Assert.assertEquals(channe1Errors.size(), 3);
        Assert.assertEquals(channe1Errors.get(2).storeName, null);
        Assert.assertEquals(channe1Errors.get(2).errorMsg, "Invalid CloseStoreRequest: null storeId");

        Assert.assertEquals(channel1Responses.size(), 0);
        Assert.assertEquals(channel2Responses.size(), 0);
        Assert.assertTrue(channel2Errors.isEmpty());
    }

    @Test
    public void testCloseSyncChannel() {
        String syncChannel1 = addNewGalacticStoreSyncChannel();
        List<Object> channel1Responses = new ArrayList<>();
        List<BusStoreError> channe1Errors = new ArrayList<>();
        listenSyncChannel(syncChannel1, channel1Responses, channe1Errors);

        String syncChannel2 = addNewGalacticStoreSyncChannel();
        List<Object> channel2Responses = new ArrayList<>();
        List<BusStoreError> channel2Errors = new ArrayList<>();
        listenSyncChannel(syncChannel2, channel2Responses, channel2Errors);

        sendOpenStoreRequest(syncChannel1, "store1");
        sendOpenStoreRequest(syncChannel1, "store2");

        sendOpenStoreRequest(syncChannel2, "store1");
        sendOpenStoreRequest(syncChannel2, "store2");

        Assert.assertEquals(channel1Responses.size(), 2);
        Assert.assertEquals(channel2Responses.size(), 2);

        closeGalacticStoreSyncChannel(syncChannel2);

        sendUpdateStoreRequest(syncChannel1, "store2", "item2", "test");

        Assert.assertEquals(channel1Responses.size(), 3);
        Assert.assertEquals(channel2Responses.size(), 2);

        sendUpdateStoreRequest(syncChannel2, "store2", "item3", "test");
        Assert.assertEquals(channel1Responses.size(), 3);
        Assert.assertEquals(channel2Responses.size(), 2);

        closeGalacticStoreSyncChannel(syncChannel1);

        storeManager.getStore("store2").put("item1", "test", "update");
        Assert.assertEquals(channel1Responses.size(), 3);
        Assert.assertEquals(channel2Responses.size(), 2);
    }

    private void validateStoreContent(Object response, String storeName) {
        Assert.assertTrue(response instanceof StoreContentResponse);
        StoreContentResponse storeContentResp = (StoreContentResponse) response;

        Assert.assertEquals(storeContentResp.storeId, storeName);
        Assert.assertEquals(storeContentResp.responseType, "storeContentResponse");

        StoreContent content = storeManager.getStore(storeName).getStoreContent();
        Assert.assertEquals(storeContentResp.storeVersion, content.storeVersion);
        Assert.assertEquals(storeContentResp.items.size(), content.items.size());
        for (Object key : storeContentResp.items.keySet()) {
            Assert.assertEquals(storeContentResp.items.get(key), content.items.get(key));
        }
    }

    private void validateStoreUpdateResponse(
          Object response, String store, Object objectId, Object newValue, long version) {

        Assert.assertTrue(response instanceof UpdateStoreResponse);
        UpdateStoreResponse updateResponse = (UpdateStoreResponse) response;

        Assert.assertEquals(updateResponse.storeId, store);
        Assert.assertEquals(updateResponse.itemId, objectId);

        Assert.assertEquals(updateResponse.newItemValue, newValue);
        Assert.assertEquals(updateResponse.storeVersion, version);
    }

    private void sendOpenStoreRequest(String channel, String storeName) {
        OpenStoreRequest openStoreReq = new OpenStoreRequest();
        openStoreReq.storeId = storeName;

        Request<OpenStoreRequest> request = new Request<>(
              GalacticStoreService.GalacticStoreCommands.OpenStore,
              openStoreReq);

        eventBus.sendRequestMessage(channel, request);
    }

    private void sendUpdateStoreRequest(
          String channel, String storeName, Object itemId, Object newValue) {

        UpdateStoreRequest updateStoreRequest = new UpdateStoreRequest();
        updateStoreRequest.storeId = storeName;
        updateStoreRequest.itemId = itemId;
        updateStoreRequest.newItemValue = newValue;

        Request<UpdateStoreRequest> request = new Request<>(
              GalacticStoreService.GalacticStoreCommands.UpdateStore,
              updateStoreRequest);

        eventBus.sendRequestMessage(channel, request);
    }

    private void sendCloseStoreRequest(String channel, String storeName) {
        CloseStoreRequest storeReq = new CloseStoreRequest();
        storeReq.storeId = storeName;

        Request<CloseStoreRequest> request = new Request<>(
              GalacticStoreService.GalacticStoreCommands.CloseStore,
              storeReq);

        eventBus.sendRequestMessage(channel, request);
    }

    private void listenSyncChannel(String channel, List<Object> responses, List<BusStoreError> errors) {
        eventBus.listenStream(channel,
              m -> {
                  responses.add(m.getPayload());
              }, e -> {
                  errors.add((BusStoreError) e.getPayload());
              });
    }

    private String addNewGalacticStoreSyncChannel() {
        String channelName = "fabric-store-sync." + UUID.randomUUID();
        MonitorObject mo = new MonitorObject(MonitorType.MonitorNewBridgeSubscription, channelName, "test");
        MessageObject<MonitorObject> message = new MessageObject<>(MessageType.MessageTypeRequest, mo);
        eventBus.getApi().getMonitor().onNext(message);
        return channelName;
    }

    private void closeGalacticStoreSyncChannel(String channel) {
        MonitorObject mo = new MonitorObject(MonitorType.MonitorCloseChannel, channel, "test");
        MessageObject<MonitorObject> message = new MessageObject<>(MessageType.MessageTypeRequest, mo);
        eventBus.getApi().getMonitor().onNext(message);
    }
}
