package com.example.sks.voiceservice;

import android.os.Binder;
import android.os.RemoteException;

import com.example.sks.voiceinject.IClient;
import com.example.sks.voiceinject.IService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sks on 2016/9/9.
 */
public class MainControlCenter {
    private static MainControlCenter sInstance;

    private final class ClientRecord implements Binder.DeathRecipient {
        IClient clientBinder;

        ClientRecord(IClient client) {
            this.clientBinder = client;
            try {
                client.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void binderDied() {
            synchronized (MainControlCenter.this) {
                handleClientDeathLocked(ClientRecord.this);
            }
        }
    }

    private List<ClientRecord> mClients = new ArrayList<>();

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
        for (int i = 0; i < mClients.size(); ++i) {
            if (mClients.get(i).clientBinder.asBinder() == client.asBinder()) {
                mClients.remove(i);
                return i;
            }
        }
        return -1;
    }

    private int findClientLocked(IClient client) {
        for (int i = 0; i < mClients.size(); ++i) {
            if (mClients.get(i).clientBinder.asBinder() == client.asBinder()) {
                return i;
            }
        }
        return -1;
    }

    private void handleRegisterClientLocked(IClient client) {
        if (findClientLocked(client)  != -1) {
            throw new IllegalArgumentException("call register client twice");
        }
        ClientRecord record = new ClientRecord(client);
        mClients.add(0, record);
    }

    private void handleClientDeathLocked(ClientRecord clientRecord) {
        mClients.remove(clientRecord);
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

}
