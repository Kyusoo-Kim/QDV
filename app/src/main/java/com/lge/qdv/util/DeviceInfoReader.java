package com.lge.qdv.util;

/**
 * Created by kyusoo.kim on 2015-04-01.
 */
public class DeviceInfoReader {

    private static DeviceInfoReader mDeviceInfoReader = null;

    public static DeviceInfoReader getDeviceInfoReader(){
        if(mDeviceInfoReader == null) {
            mDeviceInfoReader = new DeviceInfoReader();
        }
        return mDeviceInfoReader;
    }

    public boolean matchText(byte[] buffer, int index, String text) {
        int N = text.length();
        if ((index+N) >= buffer.length) {
            return false;
        }
        for (int i=0; i<N; i++) {
            if (buffer[index+i] != text.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public int extractMemValue(byte[] buffer, int index) {
        while (index < buffer.length && buffer[index] != '\n') {
            if (buffer[index] >= '0' && buffer[index] <= '9') {
                int start = index;
                index++;
                while (index < buffer.length && buffer[index] >= '0'
                        && buffer[index] <= '9') {
                    index++;
                }
                String str = new String(buffer, start, index-start);
                return ((int)Integer.parseInt(str));
            }
            index++;
        }
        return 0;
    }

}
