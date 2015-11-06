package com.example.sanyam.myapplication;

/**
 * Created by sanyam & anisha on 5/11/15.
 */
public class DataUsage {
    private long txWifiBytes;
    private long rxWifiBytes;
    private long txCellBytes;
    private long rxCellBytes;
    private String date;
    private String time;

    public DataUsage() {
    }

    public DataUsage(long txWifiBytes, long rxWifiBytes, long txCellBytes, long rxCellBytes, String date, String time) {
        this.txWifiBytes = txWifiBytes;
        this.rxWifiBytes = rxWifiBytes;
        this.txCellBytes = txCellBytes;
        this.rxCellBytes = rxCellBytes;
        this.date = date;
        this.time = time;
    }

    public long getTxWifiBytes() {
        return txWifiBytes;
    }

    public void setTxWifiBytes(long txWifiBytes) {
        this.txWifiBytes = txWifiBytes;
    }

    public long getRxWifiBytes() {
        return rxWifiBytes;
    }

    public void setRxWifiBytes(long rxWifiBytes) {
        this.rxWifiBytes = rxWifiBytes;
    }

    public long getTxCellBytes() {
        return txCellBytes;
    }

    public void setTxCellBytes(long txCellBytes) {
        this.txCellBytes = txCellBytes;
    }

    public long getRxCellBytes() {
        return rxCellBytes;
    }

    public void setRxCellBytes(long rxCellBytes) {
        this.rxCellBytes = rxCellBytes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
