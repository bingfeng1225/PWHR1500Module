package cn.haier.bio.medical.hr1500;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.lang.ref.WeakReference;

import cn.qd.peiwen.serialport.PWSerialPortHelper;
import cn.qd.peiwen.serialport.PWSerialPortListener;
import cn.qd.peiwen.serialport.PWSerialPortState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class HR1500SerialPort implements PWSerialPortListener {
    private ByteBuf buffer;
    private HR1500Handler handler;
    private HandlerThread thread;
    private PWSerialPortHelper helper;

    private boolean enabled = false;
    private WeakReference<IHR1500Listener> listener;

    public HR1500SerialPort() {

    }

    public void init(String path) {
        this.createHandler();
        this.createHelper(path);
        this.createBuffer();
    }

    public void enable() {
        if (this.isInitialized() && !this.enabled) {
            this.enabled = true;
            this.helper.open();
        }
    }

    public void disable() {
        if (this.isInitialized() && this.enabled) {
            this.enabled = false;
            this.helper.close();
        }
    }

    public void release() {
        this.listener = null;
        this.destoryHandler();
        this.destoryHelper();
        this.destoryBuffer();
    }

    public void sendData(byte[] data) {
        if (this.isInitialized() && this.enabled) {
            Message msg = Message.obtain();
            msg.what = 0;
            msg.obj = data;
            this.handler.sendMessage(msg);
        }
    }

    public void changeListener(IHR1500Listener listener) {
        this.listener = new WeakReference<>(listener);
    }

    private boolean isInitialized() {
        if (this.handler == null) {
            return false;
        }
        if (this.helper == null) {
            return false;
        }
        return this.buffer != null;
    }

    private void createHelper(String path) {
        if (this.helper == null) {
            this.helper = new PWSerialPortHelper("HR1500SerialPort");
            this.helper.setTimeout(10);
            this.helper.setPath(path);
            this.helper.setBaudrate(9600);
            this.helper.init(this);
        }
    }

    private void destoryHelper() {
        if (null != this.helper) {
            this.helper.release();
            this.helper = null;
        }
    }

    private void createHandler() {
        if (this.thread == null && this.handler == null) {
            this.thread = new HandlerThread("HR1500SerialPort");
            this.thread.start();
            this.handler = new HR1500Handler(this.thread.getLooper());
        }
    }

    private void destoryHandler() {
        if (null != this.thread) {
            this.thread.quitSafely();
            this.thread = null;
            this.handler = null;
        }
    }

    private void createBuffer() {
        if (this.buffer == null) {
            this.buffer = Unpooled.buffer(4);



            byte[] bytes1 = new byte[]{
                    (byte) 0x3C, (byte) 0xC3, (byte) 0x35, (byte) 0x0A, (byte) 0x15, (byte) 0x02,
                    (byte) 0x02, (byte) 0x02, (byte) 0x0E, (byte) 0x0A, (byte) 0x09, (byte) 0x04
            };
            this.buffer.writeBytes(bytes1);
            this.processBytes55();
            this.processBytesBuffer();

            byte[] bytes2 = new byte[]{
                    (byte)0x00, (byte)0x0B, (byte)0x00, (byte)0x0E, (byte)0x00, (byte)0x0F,
                    (byte)0xFF, (byte)0xF7, (byte)0x00, (byte)0xD1, (byte)0x01, (byte)0x31
            };
            this.buffer.writeBytes(bytes2);
            this.processBytes55();
            this.processBytesBuffer();


            byte[] bytes3 = new byte[]{
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02, (byte)0x00, (byte)0x5A,
                    (byte)0x00, (byte)0xF8, (byte)0x42, (byte)0xEE, (byte)0x4A, (byte)0x00
            };
            this.buffer.writeBytes(bytes3);
            this.processBytes55();
            this.processBytesBuffer();


            byte[] bytes4 = new byte[]{
                    (byte)0x01, (byte)0x3C, (byte)0x55, (byte)0x39, (byte)0x11, (byte)0x29,
                    (byte)0x11, (byte)0x2B, (byte)0x01, (byte)0x10, (byte)0x2D, (byte)0x10,
            };

            this.buffer.writeBytes(bytes4);
            this.processBytes55();
            byte[] bytes5 = new byte[]{
                    (byte)0x37, (byte)0x01, (byte)0x09, (byte)0x00, (byte)0x0A, (byte)0x00,
                    (byte)0x00, (byte)0x49, (byte)0x9A
            };
            this.buffer.writeBytes(bytes5);
            this.processBytes55();
            this.processBytesBuffer();
        }
    }

    private void destoryBuffer() {
        if (null != this.buffer) {
            this.buffer.release();
            this.buffer = null;
        }
    }

    private void write(byte[] data) {
        if (!this.isInitialized() || !this.enabled) {
            return;
        }
        this.helper.writeAndFlush(data);
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onHR1500Print("HR1500SerialPort Send:" + HR1500Tools.bytes2HexString(data, true, ", "));
        }
    }

    private boolean ignorePackage() {
        int index = HR1500Tools.indexOf(this.buffer, HR1500Tools.HEADER);
        if (index != -1) {
            byte[] data = new byte[index];
            this.buffer.readBytes(data, 0, data.length);
            this.buffer.discardReadBytes();
            if (null != this.listener && null != this.listener.get()) {
                this.listener.get().onHR1500Print("HR1500SerialPort 指令丢弃:" + HR1500Tools.bytes2HexString(data, true, ", "));
            }
            return this.processBytesBuffer();
        }
        return false;
    }

    private void processBytes55() {
        int index = HR1500Tools.indexOf(this.buffer, HR1500Tools.SPECIAL);
        if(index != -1) {
            byte[] front = new byte[index + 1];
            this.buffer.readBytes(front, 0, front.length); //读取0x55之前的数据
            this.buffer.skipBytes(1);//丢弃0x55

            byte[] behind = new byte[this.buffer.readableBytes()];
            this.buffer.readBytes(behind,0, behind.length);//读取0x55之后的数据

            this.buffer.discardReadBytes(); //丢弃缓冲区内全部数据

            this.buffer.writeBytes(front);
            this.buffer.writeBytes(behind);

            if (null != this.listener && null != this.listener.get()) {
                this.listener.get().onHR1500Print("HR1500SerialPort 删除0x55,位置：" + index);
            }
            this.processBytes55();
        }
    }

    private boolean processBytesBuffer() {
        if (this.buffer.readableBytes() < 4) {
            return true;
        }
        byte[] header = new byte[HR1500Tools.HEADER.length];
        this.buffer.getBytes(0, header);
        if (!HR1500Tools.checkHeader(header)) {
            return this.ignorePackage();
        }

        int frameLength = 0xFF & this.buffer.getByte(2) + 3;
        if (this.buffer.readableBytes() < frameLength) {
            return true;
        }
        this.buffer.markReaderIndex();
        byte[] data = new byte[frameLength];
        this.buffer.readBytes(data, 0, data.length);

        boolean frame = false;
        if (null != this.listener && null != this.listener.get()) {
            frame = this.listener.get().onHR1500CheckFrame(data);
        }

        if (!frame) {
            this.buffer.resetReaderIndex();
            //当前包不合法 丢掉正常的包头以免重复判断
            this.buffer.skipBytes(2);
            this.buffer.discardReadBytes();
            return this.ignorePackage();
        }
        this.buffer.discardReadBytes();
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onHR1500Print("HR1500SerialPort Recv:" + HR1500Tools.bytes2HexString(data, true, ", "));
            this.listener.get().onHR1500PackageReceived(data);
        }
        return true;
    }

    @Override
    public void onConnected(PWSerialPortHelper helper) {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        this.buffer.clear();
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onHR1500Connected();
        }
    }

    @Override
    public void onReadThreadReleased(PWSerialPortHelper helper) {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onHR1500Print("HR1500SerialPort read thread released");
        }
    }

    @Override
    public void onException(PWSerialPortHelper helper, Throwable throwable) {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onHR1500Exception(throwable);
        }
    }

    @Override
    public void onStateChanged(PWSerialPortHelper helper, PWSerialPortState state) {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onHR1500Print("HR1500SerialPort state changed: " + state.name());
        }
    }

    @Override
    public boolean onByteReceived(PWSerialPortHelper helper, byte[] buffer, int length) throws IOException {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return false;
        }
        this.buffer.writeBytes(buffer, 0, length);
        this.processBytes55();
        return this.processBytesBuffer();
    }

    private class HR1500Handler extends Handler {
        public HR1500Handler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    byte[] message = (byte[]) msg.obj;
                    if (null != message && message.length > 0) {
                        HR1500SerialPort.this.write(message);
                    }
                    break;
            }
        }
    }
}
