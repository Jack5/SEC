package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SecureFSInterface extends Remote {
    
    public byte[] FS_init() throws RemoteException;
    public boolean FS_write(int pos, int size, byte[] contents) throws RemoteException;
    public Integer FS_read(byte[] id, int pos, int size, byte[] contents) throws RemoteException;
    
    
}