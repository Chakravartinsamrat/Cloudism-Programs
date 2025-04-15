import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import java.util.*;
import org.cloudbus.cloudsim.provisioners.*;

public class CloudSimExample1 {
    public static void main(String[] args) {
        try {
            // Initialize CloudSim
            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;
            CloudSim.init(numUsers, calendar, traceFlag);

            // Create Datacenter
            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            // Create Broker
            DatacenterBroker broker = new DatacenterBroker("Broker_0");
            int brokerId = broker.getId();

            // ========== EXAMPLE 1 ==========
            // Create one VM and one cloudlet

            // Create VMs
            List<Vm> vmList = new ArrayList<>();

            int vmId = 0;
            int mips = 1000;
            long size = 10000; // image size (MB)
            int ram = 512; // VM memory (MB)
            long bw = 1000;
            int pesNumber = 1; // number of CPUs
            String vmm = "Xen";

            Vm vm = new Vm(vmId, brokerId, mips, pesNumber, ram, bw, size, vmm,
                    new CloudletSchedulerTimeShared());

            vmList.add(vm);

            broker.submitVmList(vmList);

            // Create Cloudlets
            List<Cloudlet> cloudletList = new ArrayList<>();

            int cloudletId = 0;
            long length = 400000;
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel utilizationModel = new UtilizationModelFull();

            Cloudlet cloudlet = new Cloudlet(cloudletId, length, pesNumber, fileSize, outputSize,
                    utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            cloudlet.setVmId(vmId);

            cloudletList.add(cloudlet);

            broker.submitCloudletList(cloudletList);

            // =============================
            // EXAMPLE 2: Add multiple VMs and Cloudlets
            /*
            // Uncomment this block for Example 2
            for (int i = 1; i < 3; i++) {
                Vm extraVm = new Vm(i, brokerId, 1200, 1, 1024, 1000, 10000, "Xen",
                        new CloudletSchedulerTimeShared());
                vmList.add(extraVm);

                Cloudlet extraCloudlet = new Cloudlet(i, 500000, 1, 300, 300,
                        utilizationModel, utilizationModel, utilizationModel);
                extraCloudlet.setUserId(brokerId);
                extraCloudlet.setVmId(i);
                cloudletList.add(extraCloudlet);
            }
            */

            // =============================
            // EXAMPLE 3: Use Space-Shared Scheduling
            /*
            // Replace VM creation line above with:
            new Vm(vmId, brokerId, mips, pesNumber, ram, bw, size, vmm,
                   new CloudletSchedulerSpaceShared());
            */

            // =============================
            // EXAMPLE 4: Custom VM Allocation Policy or Datacenter Configuration
            /*
            // Replace createDatacenter method with a custom one:
            // Datacenter datacenter0 = createCustomDatacenter("Datacenter_0");
            */

            // Start simulation
            CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            printCloudletList(newList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Standard Datacenter with 1 Host
    private static Datacenter createDatacenter(String name) throws Exception {
        List<Host> hostList = new ArrayList<>();

        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(1000))); // 1 CPU

        int hostId = 0;
        int ram = 2048; // host memory (MB)
        long storage = 1000000; // host storage
        int bw = 10000;

        hostList.add(new Host(hostId, new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw), storage, peList,
                new VmSchedulerTimeShared(peList)));

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.1;
        double costPerBw = 0.1;

        LinkedList<Storage> storageList = new LinkedList<>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        return new Datacenter(name, characteristics,
                new VmAllocationPolicySimple(hostList), storageList, 0);
    }

    // Output
    private static void printCloudletList(List<Cloudlet> list) {
        String indent = "    ";
        System.out.println("\n========== OUTPUT ==========\n");
        System.out.println("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent +
                "Start Time" + indent + "Finish Time");

        for (Cloudlet cloudlet : list) {
            System.out.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                System.out.println("SUCCESS" + indent +
                        cloudlet.getResourceId() + indent + indent +
                        cloudlet.getVmId() + indent + indent +
                        cloudlet.getActualCPUTime() + indent +
                        cloudlet.getExecStartTime() + indent + indent +
                        cloudlet.getFinishTime());
            }
        }
    }
}
