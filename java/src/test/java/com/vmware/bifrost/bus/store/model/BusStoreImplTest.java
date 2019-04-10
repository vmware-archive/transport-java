/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus.store.model;

import com.vmware.bifrost.bus.EventBus;
import com.vmware.bifrost.bus.EventBusImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BusStoreImplTest {

   private EventBus eventBus;
   private int whenReadyCalls;
   private int allStoreEvents;
   private int storeAddEvents;
   private int storeUpdateEvents;
   private int storeRemoveEvents;
   private Exception lastException;

   private TestStoreItem item1;
   private TestStoreItem item2;
   private TestStoreItem item3;
   private TestStoreItem item4;
   private TestStoreItem lastItem;
   private TestStoreItem lastRemovedItem;
   private BusStore<TestStoreItem> store;

   @Before
   public void before() throws Exception {
      this.eventBus = new EventBusImpl();
      store = new BusStoreImpl<>(eventBus, "testStore");
      this.whenReadyCalls = 0;
      this.allStoreEvents = 0;
      this.storeUpdateEvents = 0;
      this.storeRemoveEvents = 0;
      this.lastItem = null;
      this.lastException = null;
      this.item1 = new TestStoreItem("Item1", 0);
      this.item2 = new TestStoreItem("Item2", 0);
      this.item3 = new TestStoreItem("Item3", 0);
      this.item4 = new TestStoreItem("Item4", 0);
   }

   @Test
   public void testPopulate() {
      Assert.assertFalse(store.isInitialized());

      store.whenReady(storeItemsMap -> {
         whenReadyCalls++;
         verifyTestItemMap(storeItemsMap, item1, item2, item3);
         Assert.assertEquals(store.get(item1.uuid), item1);
         Assert.assertNull(store.get(null));
      });

      Map<UUID, TestStoreItem> map = new HashMap<>();
      map.put(item1.uuid, item1);
      map.put(item2.uuid, item2);
      map.put(item3.uuid, item3);
      store.populate(map);
      Assert.assertTrue(store.isInitialized());

      // Verify that populate cannot be invoked more than once.
      Assert.assertFalse(store.populate(map));
      Assert.assertEquals(whenReadyCalls, 1);

      // Verify that populate method won't be invoked if
      // there are existing items in the store.
      store.reset();
      store.getBusStoreInitializer().add(item1.uuid, item1);
      Assert.assertFalse(store.populate(map));

      // Verify that populate method won't be invoked
      // for empty store which was marked as initialized.
      store.reset();
      store.initialize();
      Assert.assertFalse(store.populate(map));
   }

   @Test
   public void testBusStoreInitializer() {
      store.whenReady(storeItemsMap -> {
         whenReadyCalls++;
         verifyTestItemMap(storeItemsMap, item1, item2);
      });

      store.getBusStoreInitializer()
            .add(item1.uuid, item1)
            .add(item2.uuid, item2)
            .add(null, item3) // null keys are not support so this shouldn't be added
            .done();

      Assert.assertTrue(store.isInitialized());
      Assert.assertEquals(whenReadyCalls, 1);

      // Verify that getBusStoreInitializer will return null if the store is initialized.
      Assert.assertNull(store.getBusStoreInitializer());
   }

   @Test
   public void testAllValuesMap() {
      Assert.assertTrue(store.allValuesAsMap().isEmpty());

      store.getBusStoreInitializer()
            .add(item1.uuid, item1)
            .add(item2.uuid, item2)
            .done();

      Map<UUID, TestStoreItem> allValues = store.allValuesAsMap();

      verifyTestItemMap(allValues, item1, item2);

      // Verify that allValuesAsMap returns a copy of the internal store map
      // and changes to the copy don't affect the internal map.
      allValues.put(item4.uuid, item4);
      verifyTestItemMap(store.allValuesAsMap(), item1, item2);

      // Also verify that changes to the internal store map don't affect
      // already returned allValuesAsMap results.
      store.put(item3.uuid, item3, TestStoreItemState.ITEM_ADDED);
      verifyTestItemMap(allValues, item1, item2, item4);
   }

   @Test
   public void testAllValues() {
      Assert.assertTrue(store.allValues().isEmpty());

      store.getBusStoreInitializer()
            .add(item1.uuid, item1)
            .add(item2.uuid, item2)
            .done();

      List<TestStoreItem> allItemsList = store.allValues();
      Assert.assertTrue(allItemsList.contains(item1));
      Assert.assertTrue(allItemsList.contains(item2));
      Assert.assertEquals(allItemsList.size(),  2);

      store.put(item3.uuid, item3, TestStoreItemState.ITEM_ADDED);

      Assert.assertEquals(allItemsList.size(),  2);
   }

   @Test
   public void testOnChange() {
      store.getBusStoreInitializer()
            .add(item1.uuid, item1)
            .add(item2.uuid, item2)
            .done();

      Assert.assertNull(store.onChange(null));

      StoreStream<TestStoreItem> allChangesStream = store.onChange(item1.uuid);

      // Calling unsubscribe before subscribe shouldn't have any effect
      // and shouldn't throw any errors.
      allChangesStream.unsubscribe();

      allChangesStream.subscribe(testStoreItem -> {
         allStoreEvents++;
         lastItem = testStoreItem;
      });

      store.onChange(item1.uuid, TestStoreItemState.ITEM_REMOVED).subscribe(testStoreItem -> {
         storeRemoveEvents++;
         lastRemovedItem = testStoreItem;
      });

      TestStoreItem modifiedItem1 = new TestStoreItem("modifiedItem", 10);
      store.put(item1.uuid, modifiedItem1, TestStoreItemState.ITEM_UPDATED);

      Assert.assertEquals(allStoreEvents, 1);
      Assert.assertEquals(lastItem, modifiedItem1);

      store.remove(item2.uuid, TestStoreItemState.ITEM_REMOVED);

      Assert.assertEquals(allStoreEvents, 1);
      Assert.assertEquals(storeRemoveEvents, 0);

      allChangesStream.unsubscribe();
      // A second unsubscribe shouldn't have any effect.
      allChangesStream.unsubscribe();
      store.remove(item1.uuid, TestStoreItemState.ITEM_REMOVED);

      Assert.assertEquals(allStoreEvents, 1);
      Assert.assertEquals(storeRemoveEvents, 1);
      Assert.assertEquals(lastRemovedItem, modifiedItem1);
   }

   @Test
   public void testOnAllChanges() {
      store.initialize();

      // All changes handler
      store.onAllChanges().subscribe(testStoreItem -> {
         allStoreEvents++;
         this.lastItem = testStoreItem;
      });

      StoreStream<TestStoreItem> addItemsStream = store.onAllChanges(TestStoreItemState.ITEM_ADDED);
      addItemsStream.subscribe(testStoreItem -> {
         storeAddEvents++;
      });
      store.onAllChanges(TestStoreItemState.ITEM_UPDATED, TestStoreItemState.ITEM_REMOVED)
            .subscribe(testStoreItem -> {
               storeUpdateEvents++;
            });

      store.put(item1.uuid, item1, TestStoreItemState.ITEM_ADDED);
      store.put(null, item1, TestStoreItemState.ITEM_ADDED);
      Assert.assertEquals(allStoreEvents, 1);
      Assert.assertEquals(storeAddEvents, 1);
      Assert.assertEquals(storeUpdateEvents, 0);
      Assert.assertEquals(lastItem, item1);

      store.remove(item1.uuid, TestStoreItemState.ITEM_REMOVED);
      Assert.assertNull(store.get(item1.uuid));

      Assert.assertEquals(allStoreEvents, 2);
      Assert.assertEquals(storeAddEvents, 1);
      Assert.assertEquals(storeUpdateEvents, 1);
      Assert.assertEquals(lastItem, item1);


      store.put(item1.uuid, item1, TestStoreItemState.ITEM_ADDED);
      Assert.assertEquals(storeAddEvents, 2);

      addItemsStream.unsubscribe();
      store.put(item1.uuid, item1, TestStoreItemState.ITEM_ADDED);

      Assert.assertEquals(allStoreEvents, 4);
      Assert.assertEquals(storeAddEvents, 2);
      Assert.assertEquals(storeUpdateEvents, 1);

      try {
         store.onAllChanges().subscribe(null);
      } catch (Exception ex) {
         lastException = ex;
      }
      Assert.assertNotNull(lastException);
      Assert.assertEquals(lastException.getMessage(), "Invalid store stream handler.");
   }

   @Test
   public void testRemove() {
      store.getBusStoreInitializer()
            .add(item1.uuid, item1)
            .add(item2.uuid, item2)
            .done();

      store.onAllChanges(TestStoreItemState.ITEM_REMOVED).subscribe(testStoreItem -> {
         storeRemoveEvents++;
      });
      store.onAllChanges().subscribe(testStoreItem -> {
         allStoreEvents++;
      });

      Assert.assertFalse(store.remove(item3.uuid, TestStoreItemState.ITEM_REMOVED));
      Assert.assertFalse(store.remove(null, TestStoreItemState.ITEM_REMOVED));
      Assert.assertEquals(storeRemoveEvents, 0);
      Assert.assertEquals(allStoreEvents, 0);
   }

   @Test
   public void testReset() {
      store.whenReady(uuidTestStoreItemMap -> {
         whenReadyCalls++;
      });

      store.initialize();

      Assert.assertEquals(whenReadyCalls, 1);
      store.reset();

      Assert.assertFalse(store.isInitialized());
      Assert.assertTrue(store.allValues().isEmpty());

      store.whenReady(uuidTestStoreItemMap -> {
         whenReadyCalls++;
      });
      store.getBusStoreInitializer()
            .add(item1.uuid, item1)
            .done();
      Assert.assertEquals(whenReadyCalls, 2);
      verifyTestItemMap(store.allValuesAsMap(), item1);
   }

   @Test
   public void testWhenReady() {
      store.whenReady(uuidTestStoreItemMap -> {
         whenReadyCalls++;
         Assert.assertFalse(uuidTestStoreItemMap.isEmpty());
      });

      store.getBusStoreInitializer()
            .add(item1.uuid, item1)
            .done();

      Assert.assertEquals(whenReadyCalls, 1);

      // calling initialize on already initialized store shouldn't
      // have any effect.
      store.initialize();
      Assert.assertEquals(whenReadyCalls, 1);

      // Verify that calling whenReady on already initialized
      // store will invoke the whenReady handler.
      store.whenReady(uuidTestStoreItemMap -> {
         whenReadyCalls++;
         Assert.assertFalse(uuidTestStoreItemMap.isEmpty());
      });
      Assert.assertEquals(whenReadyCalls, 2);

      // Verify that whenReady handler exceptions are handled
      // correctly.
      try {
         store.whenReady(uuidTestStoreItemMap -> {
            throw new Exception("Test Exception");
         });
      } catch (Exception ex) {
         lastException = ex;
      }
      Assert.assertNull(lastException);
   }

   @Test
   public void testMutate() {
      store.getBusStoreInitializer()
            .add(item1.uuid, item1)
            .add(item2.uuid, item2)
            .done();

      final MutateStream<TestStoreItemUpdateRequest> decreaseMutationStream =
            store.onMutationRequest("decreaseCount");
      decreaseMutationStream.subscribe(
            (MutationRequestWrapper<TestStoreItemUpdateRequest> requestWrapper) -> {
               TestStoreItemUpdateRequest request = requestWrapper.getRequest();
               request.item.count -= request.incCountBy;
               requestWrapper.success(request.item);
            });

      try {
         decreaseMutationStream.subscribe(null);
      } catch (Exception ex) {
         lastException = ex;
      }
      Assert.assertNotNull(lastException);
      Assert.assertEquals(lastException.getMessage(), "Invalid mutate stream handler.");

      try {
         decreaseMutationStream.subscribe(updateRequest -> {});
      } catch (Exception ex) {
         lastException = ex;
      }
      Assert.assertNotNull(lastException);
      Assert.assertEquals(lastException.getMessage(), "Stream already subscribed!");

      final MutateStream<TestStoreItemUpdateRequest> mutateStream = store.onMutationRequest();

      mutateStream.subscribe(requestWrapper -> {
         TestStoreItemUpdateRequest request = requestWrapper.getRequest();
         request.item.count += request.incCountBy;
         requestWrapper.success(request.item);
      });

      store.mutate(new TestStoreItemUpdateRequest(item1, 20), "increaseCount",
            (Object result) -> {
               Assert.assertEquals(result, item1);
               storeUpdateEvents++;
            },
            (Object error) -> {
               // this shouldn't be called
               Assert.assertNotNull(error);
            });
      store.mutate(new TestStoreItemUpdateRequest(item1, 1), "increaseCount", null, null);

      Assert.assertEquals(item1.count, 21);
      Assert.assertEquals(storeUpdateEvents, 1);

      mutateStream.unsubscribe();

      store.mutate(new TestStoreItemUpdateRequest(item1, 5), "decreaseCount",
            (Object result) -> {
               Assert.assertEquals(result, item1);
               storeUpdateEvents++;
            },
            (Object error) -> {
               // this shouldn't be called
               Assert.assertNotNull(error);
            });

      Assert.assertEquals(item1.count, 16);
      Assert.assertEquals(storeUpdateEvents, 2);

      store.onMutationRequest("changeName").subscribe(
            requestWrapper -> requestWrapper.error(new Exception("TestException")));

      store.mutate(new TestStoreItemUpdateRequest(item1, 5), "changeName",
            (Object result) -> {
               storeUpdateEvents++;
            },
            (Object error) -> {
               lastException = (Exception) error;
            });

      Assert.assertEquals(storeUpdateEvents, 2);
      Assert.assertNotNull(lastException);
      Assert.assertEquals(lastException.getMessage(), "TestException");

      lastException = null;

      store.mutate(new TestStoreItemUpdateRequest(item1, 5), "changeName", null, null);
      Assert.assertNull(lastException);
   }

   private void verifyTestItemMap(Map<UUID, TestStoreItem> map, TestStoreItem... items) {
      Assert.assertEquals(map.size(), items.length);
      for (TestStoreItem item : items) {
         Assert.assertEquals(map.get(item.uuid), item);
      }
   }
}
