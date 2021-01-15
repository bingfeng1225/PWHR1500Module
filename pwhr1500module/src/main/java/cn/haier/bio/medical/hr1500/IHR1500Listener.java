package cn.haier.bio.medical.hr1500;

public interface IHR1500Listener {
    void onHR1500Connected();

    void onHR1500Print(String message);

    void onHR1500Exception(Throwable throwable);

    void onHR1500PackageReceived(byte[] data);
}
