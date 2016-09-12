package com.example.sks.voiceservice;

import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;

import com.example.sks.voiceinject.IClient;
import com.example.sks.voiceinject.IService;
import com.example.sks.voiceservice.util.ProcessNameUtil;

/**
 * Created by sks on 2016/9/9.
 */
public class MainControlCenter {
    private static final String TAG = "voicectrl_mainctrl";
    private static MainControlCenter sInstance;

    private final class ClientProcessRecord implements Binder.DeathRecipient {
        IClient binder;
        String packageName;
        String processName;
        int pid;

        ClientProcessRecord(IClient client, String packageName, String processName, int pid) {
            this.binder = client;
            try {
                client.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            this.packageName = packageName;
            this.processName = processName;
            this.pid = pid;
        }

        @Override
        public void binderDied() {
            synchronized (MainControlCenter.this) {
                handleClientDeathLocked(ClientProcessRecord.this);
            }
        }
    }

    private SparseArray<ClientProcessRecord> mClientProcessesSelfLocked = new SparseArray<>();

    private IService.Stub mService = new IService.Stub() {
        @Override
        public void registerClient(IClient client) throws RemoteException {
            synchronized (MainControlCenter.this) {
                handleRegisterClientLocked(client);
            }
        }

        @Override
        public void unregisterClient(IClient client) throws RemoteException {
            synchronized (MainControlCenter.this) {
                handleUnRegisterClientLocked(client);
            }
        }
    };

    private int handleUnRegisterClientLocked(IClient client) {
        for (int i = 0; i < mClientProcessesSelfLocked.size(); ++i) {
            if (mClientProcessesSelfLocked.get(i).binder.asBinder() == client.asBinder()) {
                mClientProcessesSelfLocked.remove(i);
                return i;
            }
        }
        return -1;
    }

    private int findClientLocked(IClient client) {
        for (int i = 0; i < mClientProcessesSelfLocked.size(); ++i) {
            if (mClientProcessesSelfLocked.get(i).binder.asBinder() == client.asBinder()) {
                return i;
            }
        }
        return -1;
    }

    // binder call
    private void handleRegisterClientLocked(IClient client) {
        if (findClientLocked(client)  != -1) {
            throw new IllegalArgumentException("call register client twice");
        }
        int callingPid = Binder.getCallingPid();
        ClientProcessRecord old = getProcessRecordByPid(callingPid);
        if (old != null) {
            removeObsoleteClient(old);
        }

        final String processName = ProcessNameUtil.getProcessName(callingPid);
        final String packageName = ProcessNameUtil.getPackageNameFromProcessName(processName);
        if ((old = getProcessRecordByName(packageName, processName)) != null) {
            removeObsoleteClient(old);
        }

        ClientProcessRecord record = new ClientProcessRecord(client, processName, packageName, callingPid);
        synchronized (mClientProcessesSelfLocked) {
            mClientProcessesSelfLocked.put(callingPid, record);
        }
    }

    private void removeObsoleteClient(ClientProcessRecord record) {
        Log.d(TAG, "removeObsoleteClient called for " + record);
        try {
            record.binder.asBinder().unlinkToDeath(record, 0);
        } catch (Exception e) {
        }
        synchronized (mClientProcessesSelfLocked) {
            mClientProcessesSelfLocked.remove(record.pid);
        }
    }

    private ClientProcessRecord getProcessRecordByName(String packageName, String processName) {
        synchronized (mClientProcessesSelfLocked) {
            for (int i = 0; i < mClientProcessesSelfLocked.size(); ++i) {
                ClientProcessRecord record = mClientProcessesSelfLocked.valueAt(i);
                Log.d(TAG, "getProcessRecordByName item " + record + ",packageName "
                        + packageName + ",processName " + processName);
                if (record.packageName.equals(packageName)
                        && (processName == null || processName.equals(record.processName))) {
                    return record;
                }
            }
        }
        return null;
    }

    private ClientProcessRecord getProcessRecordByPid(int pid) {
        synchronized (mClientProcessesSelfLocked) {
            return mClientProcessesSelfLocked.get(pid);
        }
    }

    private void handleClientDeathLocked(ClientProcessRecord clientRecord) {
        Log.d(TAG, "client died for " + clientRecord);
        mClientProcessesSelfLocked.remove(clientRecord.pid);
    }

    private MainControlCenter() {
    }

    public static synchronized MainControlCenter getInstance() {
        if (sInstance == null) {
            sInstance = new MainControlCenter();
        }
        return sInstance;
    }

    public IService getService() {
        return mService;
    }

    public void executeCommand(String cmd, String processName) {
        final String packageName = ProcessNameUtil.getPackageNameFromProcessName(processName);
        executeCommand(cmd, packageName, processName);
    }

    public void executeCommand(String cmd, String packageName, String processName) {
        ClientProcessRecord client = getProcessRecordByName(packageName, processName);
        if (client == null) {
            Log.e(TAG, "Failed to find client " + processName + " to execute " + cmd);
            return;
        }
        try {
            client.binder.handleCmd(cmd);
        } catch (Exception e) {
            Log.e(TAG, "Client " + processName + " failed to execute command " + cmd);
            e.printStackTrace();
        }
    }

}
