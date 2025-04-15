package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

import java.text.DecimalFormat;
import java.util.*;

public class CloudSimExample2 {

    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmlist;

    public static void main(String[] args) {
        Log.printLine("Starting CloudSimExample3 with priority scheduling...");

        try {
            int numUser = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;
            CloudSim.init(numUser, calendar, traceFlag);

            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            PriorityDatacenterBroker broker = new PriorityDatacenterBroker("PriorityBroker");
            int brokerId = broker.getId();

            vmlist = new ArrayList<>();
            int mips = 250;
            long size = 10000;
            int ram = 2048;
            long bw = 1000;
            int pesNumber = 1;
            String vmm = "Xen";

            Vm vm1 = new Vm(0, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            Vm vm2 = new Vm(1, brokerId, mips * 2, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vmlist.add(vm1);
            vmlist.add(vm2);
            broker.submitVmList(vmlist);

            cloudletList = new ArrayList<>();
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel utilizationModel = new UtilizationModelFull();

            // High-priority cloudlet (shorter length)
            Cloudlet cloudlet1 = new Cloudlet(0, 20000, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet1.setUserId(brokerId);

            // Low-priority cloudlet (longer length)
            Cloudlet cloudlet2 = new Cloudlet(1, 60000, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet2.setUserId(brokerId);

            cloudletList.add(cloudlet1);
            cloudletList.add(cloudlet2);
            broker.submitCloudletList(cloudletList);

            CloudSim.startSimulation();
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();

            printCloudletList(newList);
            Log.printLine("CloudSimExample3 finished!");

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();

        int mips = 1000;
        List<Pe> peList1 = new ArrayList<>();
        peList1.add(new Pe(0, new PeProvisionerSimple(mips)));

        Host host1 = new Host(0, new RamProvisionerSimple(2048), new BwProvisionerSimple(10000),
                1000000, peList1, new VmSchedulerTimeShared(peList1));

        List<Pe> peList2 = new ArrayList<>();
        peList2.add(new Pe(0, new PeProvisionerSimple(mips)));

        Host host2 = new Host(1, new RamProvisionerSimple(2048), new BwProvisionerSimple(10000),
                1000000, peList2, new VmSchedulerTimeShared(peList2));

        hostList.add(host1);
        hostList.add(host2);

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double timeZone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;

        LinkedList<Storage> storageList = new LinkedList<>();
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, timeZone, cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static void printCloudletList(List<Cloudlet> list) {
        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent +
                "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (Cloudlet cloudlet : list) {
            Log.print(indent + cloudlet.getCloudletId() + indent);
            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS" + indent);
                Log.print(cloudlet.getResourceId() + indent + cloudlet.getVmId() + indent +
                        dft.format(cloudlet.getActualCPUTime()) + indent +
                        dft.format(cloudlet.getExecStartTime()) + indent + dft.format(cloudlet.getFinishTime()));
                Log.printLine();
            }
        }
    }
}

class PriorityDatacenterBroker extends DatacenterBroker {

    public PriorityDatacenterBroker(String name) throws Exception {
        super(name);
    }

    @Override
    protected void submitCloudlets() {
        getCloudletList().sort(Comparator.comparingLong(Cloudlet::getCloudletLength)); // Lower length = higher priority
        super.submitCloudlets();
    }
}