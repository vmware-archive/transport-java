/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples.vm;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.EventBus;
import com.vmware.bifrost.bus.EventBusImpl;
import com.vmware.bifrost.core.util.ServiceMethodLookupUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import samples.vm.model.BaseVmRequest;
import samples.vm.model.BaseVmResponse;
import samples.vm.model.RuntimeInfo;
import samples.vm.model.VirtualHardware;
import samples.vm.model.VirtualMachine;
import samples.vm.model.VirtualUSB;
import samples.vm.model.VmCreateRequest;
import samples.vm.model.VmCreateResponse;
import samples.vm.model.VmDeleteRequest;
import samples.vm.model.VmListResponse;
import samples.vm.model.VmPowerOperationRequest;
import samples.vm.model.VmPowerOperationResponse;
import samples.vm.model.VmRef;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        EventBusImpl.class,
        VmService.class,
        ServiceMethodLookupUtil.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class VmServiceTest {

   @Autowired
   private EventBus bus;

   private int errorCount;
   private Object lastError;
   private int responseCount;
   private Response lastResponse;

   @Autowired
   private VmService vmService;

   @Before
   public void before() {
      this.responseCount = 0;
      this.errorCount = 0;
      this.lastError = null;
      this.lastResponse = null;

      bus.listenStream(VmService.Channel,
            message -> {
               responseCount++;
               lastResponse = (Response) message.getPayload();
            }, error -> {
               errorCount++;
               lastError = error.getPayload();
            });
   }

   @Test
   public void testListVms() throws Exception {
      Map<String, VirtualMachine> vmsByName = getVms();
      Assert.assertEquals(responseCount, 1);
      Assert.assertEquals(errorCount, 0);

      assertLastBaseVmResponseIsNotError();
      Assert.assertTrue(vmsByName.containsKey("sample-vm-1"));
      Assert.assertEquals(vmsByName.get("sample-vm-1").getHardware().getDevices().length, 3);
      Assert.assertEquals(vmsByName.get("sample-vm-1").getRuntimeInfo().getPowerState(),
            RuntimeInfo.PowerState.poweredOff);
      Assert.assertTrue(vmsByName.containsKey("sample-vm-2"));
      Assert.assertTrue(vmsByName.containsKey("sample-vm-3"));
   }

   @Test
   public void testRestListVms() {
      VmListResponse response = vmService.restListVms();
      Map<String, VirtualMachine> vmsByName = new HashMap<>();
      for (VirtualMachine vm : response.getVirtualMachines()) {
         vmsByName.put(vm.getName(), vm);
      }

      Assert.assertTrue(vmsByName.containsKey("sample-vm-1"));
      Assert.assertEquals(vmsByName.get("sample-vm-1").getHardware().getDevices().length, 3);
      Assert.assertEquals(vmsByName.get("sample-vm-1").getRuntimeInfo().getPowerState(),
            RuntimeInfo.PowerState.poweredOff);
      Assert.assertTrue(vmsByName.containsKey("sample-vm-2"));
      Assert.assertTrue(vmsByName.containsKey("sample-vm-3"));
   }

   @Test
   public void testDeleteVm() throws Exception {
      Request<BaseVmRequest> req = new Request<>();
      req.setId(UUID.randomUUID());

      VmDeleteRequest deleteRequest = new VmDeleteRequest();
      deleteRequest.setVm(VmRef.newInstance("vm1", "vc1"));

      req.setPayload(deleteRequest);
      req.setRequest(VmService.VmOperations.DELETE_VM);
      vmService.handleServiceRequest(req, null);

      Assert.assertEquals(responseCount, 1);
      Assert.assertEquals(errorCount, 0);

      Assert.assertEquals(lastResponse.getId(), req.getId());

      assertLastBaseVmResponseIsError("Cannot find VM: vm1:vc1");

      Map<String, VirtualMachine> vmsByName = getVms();
      Assert.assertEquals(responseCount, 2);
      Assert.assertEquals(errorCount, 0);

      // Delete VM 1
      Request<BaseVmRequest> delVm1Req = new Request<>();
      delVm1Req.setId(UUID.randomUUID());

      deleteRequest = new VmDeleteRequest();
      deleteRequest.setVm(vmsByName.get("sample-vm-1").getVmRef());

      req.setPayload(deleteRequest);
      req.setRequest(VmService.VmOperations.DELETE_VM);
      vmService.handleServiceRequest(req, null);

      Assert.assertEquals(responseCount, 3);
      Assert.assertEquals(errorCount, 0);

      assertLastBaseVmResponseIsNotError();

      vmsByName = getVms();
      Assert.assertEquals(responseCount, 4);
      Assert.assertEquals(errorCount, 0);

      Assert.assertFalse(vmsByName.containsKey("sample-vm-1"));
      Assert.assertEquals(vmsByName.size(), 2);
   }

   @Test
   public void testRestDeleteVm() throws Exception {
      Map<String, VirtualMachine> vmsByName = getVms();
      VmDeleteRequest vmDeleteRequest = new VmDeleteRequest();
      vmDeleteRequest.setVm(vmsByName.get("sample-vm-1").getVmRef());
      BaseVmResponse response = vmService.restDeleteVm(vmDeleteRequest);
      Assert.assertFalse(response.isError());
      vmsByName = getVms();
      Assert.assertFalse(vmsByName.containsKey("sample-vm-1"));
      Assert.assertEquals(vmsByName.size(), 2);
   }

   @Test
   public void testCreateVm() throws Exception {
      Request<BaseVmRequest> req = new Request<>();
      req.setId(UUID.randomUUID());

      VmCreateRequest createVmRequest = new VmCreateRequest();
      createVmRequest.setName("test-vm");
      createVmRequest.setVirtualHardware(VirtualHardware.newInstance(
            1, 64, VirtualUSB.newInstance(1, true, VirtualUSB.UsbSpeed.full)));
      req.setPayload(createVmRequest);
      req.setRequest(VmService.VmOperations.CREATE_VM);
      vmService.handleServiceRequest(req, null);

      Assert.assertEquals(responseCount, 1);
      Assert.assertEquals(errorCount, 0);

      assertLastBaseVmResponseIsNotError();

      VmCreateResponse createVmResponse = (VmCreateResponse) lastResponse.getPayload();

      Assert.assertEquals(createVmResponse.getVm().getName(), "test-vm");
      Assert.assertEquals(createVmResponse.getVm().getRuntimeInfo().getPowerState(),
            RuntimeInfo.PowerState.poweredOff);
      Assert.assertEquals(createVmResponse.getVm().getHardware().getNumCPU(), 1);
      Assert.assertEquals(createVmResponse.getVm().getHardware().getMemoryMB(), 64);

      VmPowerOperationRequest powerOnReq = new VmPowerOperationRequest();
      powerOnReq.setVmRefs(new VmRef[] {createVmResponse.getVm().getVmRef()});
      powerOnReq.setPowerOperation(VmPowerOperationRequest.PowerOperation.powerOn);
      req = new Request<>();
      req.setId(UUID.randomUUID());
      req.setPayload(powerOnReq);
      req.setRequest(VmService.VmOperations.CHANGE_VM_POWER_STATE);
      vmService.handleServiceRequest(req, null);
      Assert.assertEquals(responseCount, 2);
      Assert.assertEquals(errorCount, 0);

      assertLastBaseVmResponseIsNotError();

      VmPowerOperationResponse powerOperationResponse =
            (VmPowerOperationResponse) lastResponse.getPayload();
      Assert.assertTrue(powerOperationResponse.getOpResults()[0].isOperationResult());
      Assert.assertEquals(powerOperationResponse.getOpResults()[0].getVmRef(), createVmResponse.getVm().getVmRef());

      Map<String, VirtualMachine> vmsByName = getVms();

      Assert.assertEquals(vmsByName.get("test-vm").getRuntimeInfo().getPowerState(),
            RuntimeInfo.PowerState.poweredOn);
   }

   @Test
   public void testRestCreateVm() {
      VmCreateRequest createVmRequest = new VmCreateRequest();
      createVmRequest.setName("test-vm");
      createVmRequest.setVirtualHardware(VirtualHardware.newInstance(
            1, 64, VirtualUSB.newInstance(1, true, VirtualUSB.UsbSpeed.full)));

      VmCreateResponse resp = vmService.restCreateVm(createVmRequest);
      Assert.assertFalse(resp.isError());
      Assert.assertNotNull(resp.getVm());
      Assert.assertEquals(resp.getVm().getRuntimeInfo().getPowerState(),
            RuntimeInfo.PowerState.poweredOff);
   }

   private void assertLastBaseVmResponseIsNotError() {
      BaseVmResponse vmListResponse = (BaseVmResponse) lastResponse.getPayload();
      Assert.assertFalse(vmListResponse.isError());
      Assert.assertNull(vmListResponse.getErrorMessage());
   }

   private void assertLastBaseVmResponseIsError(String expectedErrorMessage) {
      BaseVmResponse baseVmResponse = (BaseVmResponse) lastResponse.getPayload();
      Assert.assertTrue(baseVmResponse.isError());
      Assert.assertEquals(baseVmResponse.getErrorMessage(), expectedErrorMessage);
   }

   private Map<String, VirtualMachine> getVms() throws Exception {
      Request<BaseVmRequest> req = new Request<>();
      req.setId(UUID.randomUUID());
      req.setPayload(new BaseVmRequest());
      req.setRequest(VmService.VmOperations.LIST_VMS);
      vmService.handleServiceRequest(req, null);
      VmListResponse vmListResponse = (VmListResponse) lastResponse.getPayload();
      Map<String, VirtualMachine> vmsByName = new HashMap<>();
      for (VirtualMachine vm : vmListResponse.getVirtualMachines()) {
         vmsByName.put(vm.getName(), vm);
      }
      return vmsByName;
   }
}
