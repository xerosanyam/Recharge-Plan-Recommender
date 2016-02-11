package com.example.sanyam.myapplication.Model;

import java.util.Date;

/**
 * Created by Sanyam Jain & Anisha Lunawat on 5/11/15.
 */

//Database class
public class Data {
    private long txWifiBytes;
    private long rxWifiBytes;
    private long txCellBytes;
    private long rxCellBytes;
    private Date date;

    public Data() {
    }

    public Data(long txWifiBytes, long rxWifiBytes, long txCellBytes, long rxCellBytes, Date date) {
        this.txWifiBytes = txWifiBytes;
        this.rxWifiBytes = rxWifiBytes;
        this.txCellBytes = txCellBytes;
        this.rxCellBytes = rxCellBytes;
        this.date = date;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
