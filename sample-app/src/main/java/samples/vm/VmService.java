/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples.vm;

import com.vmware.transport.bridge.Request;
import com.vmware.transport.bridge.Response;
import com.vmware.transport.bus.model.Message;
import com.vmware.transport.core.AbstractService;
import com.vmware.transport.core.util.ClassMapper;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
import samples.vm.model.VmPowerOperationResponseItem;
import samples.vm.model.VmRef;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/rest/samples/vm")
@Service("VmService")
public class VmService extends AbstractService<Request<BaseVmRequest>, Response<Object>> {

   // define the channel the service operates on,.
   public static final String Channel = "vm-service";

   private final Map<VmRef, VirtualMachine> vms = new ConcurrentHashMap<>();

   public VmService() {
      super(VmService.Channel);

      // Init same sample VMs
      createInitialSampleVms();
   }

   @Override
   protected void handleServiceRequest(Request<BaseVmRequest> request, Message busMessage) throws Exception {
      InetSocketAddress remoteAddr = request.getSessionAttribute("remoteAddress");
      if (remoteAddr != null ) {
         this.logDebugMessage("request from: " + remoteAddr.getHostName());
      }

      switch(request.getRequest()) {
         case VmOperations.CHANGE_VM_POWER_STATE:
            handleChangePowerState(request);
            break;
         case VmOperations.LIST_VMS:
            handleListVms(request);
            break;
         case VmOperations.GET_VMS_BY_HOST:
            handleGetVmsByHost(request);
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

   @PostMapping(VmOperations.CHANGE_VM_POWER_STATE)
   public VmPowerOperationResponse restChangePowerState(
         @RequestBody VmPowerOperationRequest vmPowerOperationRequest) {
      return getVmPowerOperationResponse(vmPowerOperationRequest);
   }

   @GetMapping(VmOperations.LIST_VMS)
   public VmListResponse restListVms() {
      return getVmListResponse();
   }

   @GetMapping(VmOperations.GET_VMS_BY_HOST)
   public Map<String, List<VirtualMachine>> restGetVmsByHost() {
      return getVmsByHost();
   }

   @PostMapping(VmOperations.CREATE_VM)
   public VmCreateResponse restCreateVm(
         @RequestBody VmCreateRequest vmCreateRequest) {
      return getVmCreateResponse(vmCreateRequest);
   }

   @PostMapping(VmOperations.DELETE_VM)
   public BaseVmResponse restDeleteVm(
         @RequestBody VmDeleteRequest vmDeleteRequest) {
      return getVmDeleteResponse(vmDeleteRequest);
   }

   private void handleListVms(Request request) {
      sendBaseVmResponse(request, getVmListResponse());
   }

   private void handleGetVmsByHost(Request request) {
      sendBaseVmResponse(request, getVmsByHost());
   }

   private VmListResponse getVmListResponse() {
      VmListResponse vmListResponse = new VmListResponse();
      vmListResponse.setVirtualMachines(vms.values().toArray(new VirtualMachine[0]));
      return vmListResponse;
   }

   private Map<String, List<VirtualMachine>>  getVmsByHost() {
      Map<String, List<VirtualMachine>> result = new HashMap<>();
      for (VirtualMachine vm : this.vms.values()) {
           if (!result.containsKey(vm.getRuntimeInfo().getHost())) {
              result.put(vm.getRuntimeInfo().getHost(), new ArrayList<>());
           }
           result.get(vm.getRuntimeInfo().getHost()).add(vm);
      }
      return result;
   }

   private void handleDeleteVm(Request request) {
      VmDeleteRequest deleteReq = castPayload(VmDeleteRequest.class, request);

      if (deleteReq == null) {
         this.sendBaseVmErrorResponse(request, "Request payload should be VmDeleteRequest!");
      } else {
         this.sendBaseVmResponse(request, getVmDeleteResponse(deleteReq));
      }
   }

   private BaseVmResponse getVmDeleteResponse(VmDeleteRequest vmDeleteRequest) {
      VmRef vmToDelete = vmDeleteRequest.getVm();
      if (!vms.containsKey(vmToDelete)) {
         return getErrorResponse("Cannot find VM: " + vmToDelete);
      } else {
         vms.remove(vmToDelete);
         return new BaseVmResponse();
      }
   }

   private void handleCreateVm(Request request) {
      VmCreateRequest createReq = castPayload(VmCreateRequest.class, request);
      if (createReq == null) {
         this.sendBaseVmErrorResponse(request, "Request payload should be VmCreateRequest!");
      } else {
         sendBaseVmResponse(request, getVmCreateResponse(createReq));
      }
   }

   private VmCreateResponse getVmCreateResponse(VmCreateRequest createRequest) {
      VmCreateResponse vmCreateResponse = new VmCreateResponse();
      if (createRequest.getVirtualHardware() == null) {
         vmCreateResponse.setErrorMessage("Invalid VmCreateRequest: null virtualHardware!");
         vmCreateResponse.setError(true);
         return vmCreateResponse;
      }
      if (createRequest.getName() == null) {
         vmCreateResponse.setErrorMessage("Invalid VmCreateRequest: null name!");
         vmCreateResponse.setError(true);
         return vmCreateResponse;
      }

      VirtualMachine vm = createAndAddVm(createRequest);
      vmCreateResponse.setVm(vm);
      return vmCreateResponse;
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
      VmPowerOperationRequest powerOpReq = castPayload(VmPowerOperationRequest.class, request);
      if (powerOpReq != null) {
         sendBaseVmResponse(request, getVmPowerOperationResponse(powerOpReq));
      } else {
         this.sendBaseVmErrorResponse(request, "Request payload should be VmPowerOperationRequest!");
      }
   }

   private VmPowerOperationResponse getVmPowerOperationResponse(VmPowerOperationRequest powerReq) {
      VmPowerOperationResponse resp = new VmPowerOperationResponse();
      if (powerReq.getVmRefs() != null) {
         VmPowerOperationResponseItem[] opResults =
               new VmPowerOperationResponseItem[powerReq.getVmRefs().length];
         for (int i = 0; i < opResults.length; i++) {
            VmRef vmRef = powerReq.getVmRefs()[i];
            opResults[i] = new VmPowerOperationResponseItem();
            opResults[i].setVmRef(vmRef);
            opResults[i].setOperationResult(changeVmPowerState(vmRef, powerReq.getPowerOperation()));
         }
         resp.setOpResults(opResults);
      }
      return resp;
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

   private void sendBaseVmResponse(Request request, Object payload) {
      Response<Object> resp = new Response<>(request.getId(), payload);

      if (payload instanceof BaseVmResponse) {
         BaseVmResponse baseVmResponse = (BaseVmResponse) payload;
         if (baseVmResponse.isError()) {
            resp.setError(baseVmResponse.isError());
            resp.setErrorMessage(baseVmResponse.getErrorMessage());
         }
      }

      if (request.getTargetUser() != null) {
         this.sendResponse(resp, request.getId(), request.getTargetUser());
      } else {
         this.sendResponse(resp, request.getId());
      }
   }

   private void sendBaseVmErrorResponse(Request request, String error) {
      sendBaseVmResponse(request, getErrorResponse(error));
   }

   private BaseVmResponse getErrorResponse(String error) {
      BaseVmResponse errResp = new BaseVmResponse();
      errResp.setErrorMessage(error);
      errResp.setError(true);
      return errResp;
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

   private static <T> T castPayload(Class clazz, Request request) {
      try {
         return ClassMapper.CastPayload(clazz, request);
      } catch (Exception ex) {
         return null;
      }
   }

   public class VmOperations {
      public final static  String CHANGE_VM_POWER_STATE = "changeVmPowerState";
      public final static String CREATE_VM = "createVm";
      public final static String DELETE_VM = "deleteVm";
      public final static String LIST_VMS = "listVms";
      public final static String GET_VMS_BY_HOST = "getVmsByHost";
   }
}
