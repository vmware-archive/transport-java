/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples.vm;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.AbstractService;
import org.apache.commons.lang3.RandomUtils;
import samples.vm.model.BaseVmRequest;
import samples.vm.model.BaseVmResponse;
import samples.vm.model.RuntimeInfo;
import samples.vm.model.VirtualDisk;
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

public class VmService extends AbstractService<Request<BaseVmRequest>, Response<BaseVmResponse>> {

   // define the channel the service operates on,.
   public static final String Channel = "vm-service";

   private final Map<VmRef, VirtualMachine> vms = new HashMap<>();

   public VmService() {
      super(VmService.Channel);

      // Init same sample VMs
      createInitialSampleVms();
   }

   @Override
   protected void handleServiceRequest(Request<BaseVmRequest> request, Message busMessage) throws Exception {

      switch(request.getRequest()) {
         case VmOperations.CHANGE_VM_POWER_STATE:
            handleChangePowerState(request);
            break;
         case VmOperations.LIST_VMS:
            handleListVms(request);
            break;
         case VmOperations.DELETE_VM:
            handleDeleteVm(request);
            break;
         case VmOperations.CREATE_VM:
            handleCreateVm(request);
            break;
         default:
            this.handleUnknownRequest(request);
      }
   }

   private void handleListVms(Request request) {
      VmListResponse vmListResponse = new VmListResponse();
      vmListResponse.setVirtualMachines(vms.values().toArray(new VirtualMachine[0]));
      sendBaseVmResponse(request, vmListResponse);
   }

   private void handleDeleteVm(Request request) {
      if (!(request.getPayload() instanceof VmDeleteRequest)) {
         this.sendBaseVmErrorResponse(request, "Request payload should be VmDeleteRequest!");
         return;
      }

      VmRef vmToDelete = ((VmDeleteRequest) request.getPayload()).getVm();
      if (!vms.containsKey(vmToDelete)) {
         this.sendBaseVmErrorResponse(request, "Cannot find VM: " + vmToDelete);
      } else {
         vms.remove(vmToDelete);
         this.sendBaseVmResponse(request, new BaseVmResponse());
      }
   }

   private void handleCreateVm(Request request) {
      if (!(request.getPayload() instanceof VmCreateRequest)) {
         this.sendBaseVmErrorResponse(request, "Request payload should be VmCreateRequest!");
         return;
      }
      VmCreateRequest createRequest = (VmCreateRequest) request.getPayload();

      if (createRequest.getVirtualHardware() == null) {
         this.sendBaseVmErrorResponse(request, "Invalid VmCreateRequest: null virtualHardware!");
         return;
      }
      if (createRequest.getName() == null) {
         this.sendBaseVmErrorResponse(request, "Invalid VmCreateRequest: null name!");
         return;
      }

      VirtualMachine vm = createAndAddVm(createRequest);
      VmCreateResponse vmCreateResponse = new VmCreateResponse();
      vmCreateResponse.setVm(vm);
      sendBaseVmResponse(request, vmCreateResponse);
   }

   private VirtualMachine createAndAddVm(VmCreateRequest createRequest) {
      VmRef vmRef = new VmRef();
      vmRef.setVmId(UUID.randomUUID().toString());
      vmRef.setVcGuid(UUID.randomUUID().toString());

      VirtualMachine vm = new VirtualMachine();
      vm.setVmRef(vmRef);
      vm.setHardware(createRequest.getVirtualHardware());
      vm.setName(createRequest.getName());

      RuntimeInfo runtimeInfo = new RuntimeInfo();
      runtimeInfo.setPowerState(RuntimeInfo.PowerState.poweredOff);
      runtimeInfo.setHost("191.168.12." + RandomUtils.nextInt(0, 255));
      vm.setRuntimeInfo(runtimeInfo);
      vms.put(vmRef, vm);
      return vm;
   }

   private void handleChangePowerState(Request request) {
      if (!(request.getPayload() instanceof VmPowerOperationRequest)) {
         this.sendBaseVmErrorResponse(request, "Request payload should be VmPowerOperationRequest!");
         return;
      }

      VmPowerOperationRequest powerReq = (VmPowerOperationRequest) request.getPayload();

      VmPowerOperationResponse resp = new VmPowerOperationResponse();

      if (powerReq.getVmRefs() != null) {
         for (VmRef vmRef : powerReq.getVmRefs()) {
            resp.getOperationResults().put(
                  vmRef, changeVmPowerState(vmRef, powerReq.getPowerOperation()));
         }
      }
      sendBaseVmResponse(request, resp);
   }

   private boolean changeVmPowerState(VmRef vmRef, VmPowerOperationRequest.PowerOperation powerOperation) {
      VirtualMachine vm = vms.get(vmRef);
      if (vm == null) {
         return false;
      }

      switch(powerOperation) {
         case powerOff:
            if (vm.getRuntimeInfo().getPowerState() == RuntimeInfo.PowerState.poweredOff) {
               // We cannot power off powered off VMs
               return false;
            }
            vm.getRuntimeInfo().setPowerState(RuntimeInfo.PowerState.poweredOff);
            return true;

         case reset:
            if (vm.getRuntimeInfo().getPowerState() == RuntimeInfo.PowerState.poweredOff ||
                  vm.getRuntimeInfo().getPowerState() == RuntimeInfo.PowerState.suspended) {
               return false;
            }
            vm.getRuntimeInfo().setPowerState(RuntimeInfo.PowerState.poweredOn);
            return true;

         case powerOn:
            if (vm.getRuntimeInfo().getPowerState() == RuntimeInfo.PowerState.poweredOn) {
               // We cannot power on powered on VMs
               return false;
            }
            vm.getRuntimeInfo().setPowerState(RuntimeInfo.PowerState.poweredOn);
            return true;

         case suspend:
            if (vm.getRuntimeInfo().getPowerState() == RuntimeInfo.PowerState.poweredOff ||
                  vm.getRuntimeInfo().getPowerState() == RuntimeInfo.PowerState.suspended) {
               return false;
            }
            vm.getRuntimeInfo().setPowerState(RuntimeInfo.PowerState.suspended);
            return true;
      }
      return false;
   }

   private void sendBaseVmResponse(Request request, BaseVmResponse baseVmResponse) {
      Response<BaseVmResponse> resp = new Response<>(request.getId(), baseVmResponse);
      if (request.getTargetUser() != null) {
         this.sendResponse(resp, request.getId(), request.getTargetUser());
      } else {
         this.sendResponse(resp, request.getId());
      }
   }

   private void sendBaseVmErrorResponse(Request request, String error) {
      BaseVmResponse errResp = new BaseVmResponse();
      errResp.setErrorMessage(error);
      errResp.setError(true);
      sendBaseVmResponse(request, errResp);
   }

   private void createInitialSampleVms() {
      VmCreateRequest vm1CreateRequest = new VmCreateRequest();
      vm1CreateRequest.setName("sample-vm-1");
      vm1CreateRequest.setVirtualHardware(VirtualHardware.newInstance(2, 2048,
            VirtualDisk.newInstance(1, 300000, VirtualDisk.DiskFormat.native_4k),
            VirtualDisk.newInstance(2, 300000, VirtualDisk.DiskFormat.native_4k),
            VirtualUSB.newInstance(3, true, VirtualUSB.UsbSpeed.full, VirtualUSB.UsbSpeed.high)));
      createAndAddVm(vm1CreateRequest);

      VmCreateRequest vm2CreateRequest = new VmCreateRequest();
      vm2CreateRequest.setName("sample-vm-2");
      vm2CreateRequest.setVirtualHardware(VirtualHardware.newInstance(1, 1024,
            VirtualDisk.newInstance(1, 200000, VirtualDisk.DiskFormat.native_512)));
      createAndAddVm(vm2CreateRequest);

      VmCreateRequest vm3CreateRequest = new VmCreateRequest();
      vm3CreateRequest.setName("sample-vm-3");
      vm3CreateRequest.setVirtualHardware(VirtualHardware.newInstance(4, 8192,
            VirtualDisk.newInstance(1, 2000000, VirtualDisk.DiskFormat.native_512),
            VirtualDisk.newInstance(2, 3000000, VirtualDisk.DiskFormat.native_512)));
      createAndAddVm(vm3CreateRequest);
   }


   public class VmOperations {
      public final static  String CHANGE_VM_POWER_STATE = "changeVmPowerState";
      public final static String CREATE_VM = "createVm";
      public final static String DELETE_VM = "deleteVm";
      public final static String LIST_VMS = "listVms";
   }
}
