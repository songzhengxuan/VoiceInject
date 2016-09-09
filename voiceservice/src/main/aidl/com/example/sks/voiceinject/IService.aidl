// IService.aidl
package com.example.sks.voiceinject;
import com.example.sks.voiceinject.IClient;

// Declare any non-default types here with import statements

interface IService {

    void registerClient(IClient client);

    void unregisterClient(IClient client);
}
