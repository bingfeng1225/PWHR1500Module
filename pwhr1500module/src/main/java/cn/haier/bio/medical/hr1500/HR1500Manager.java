package cn.haier.bio.medical.hr1500;

/***
 * 超低温变频、T系列、双系统主控板通讯
 *
 */
public class HR1500Manager {
    private HR1500SerialPort serialPort;
    private static HR1500Manager manager;

    public static HR1500Manager getInstance() {
        if (manager == null) {
            synchronized (HR1500Manager.class) {
                if (manager == null)
                    manager = new HR1500Manager();
            }
        }
        return manager;
    }

    private HR1500Manager() {

    }

    public void init(String path) {
        if (this.serialPort == null) {
            this.serialPort = new HR1500SerialPort();
            this.serialPort.init(path);
        }
    }

    public void enable() {
        if (null != this.serialPort) {
            this.serialPort.enable();
        }
    }

    public void disable() {
        if (null != this.serialPort) {
            this.serialPort.disable();
        }
    }

    public void release() {
        if (null != this.serialPort) {
            this.serialPort.release();
            this.serialPort = null;
        }
    }

    public void sendData(byte[] data) {
        if (null != this.serialPort) {
            this.serialPort.sendData(data);
        }
    }

    public void changeListener(IHR1500Listener listener) {
        if (null != this.serialPort) {
            this.serialPort.changeListener(listener);
        }
    }
}

