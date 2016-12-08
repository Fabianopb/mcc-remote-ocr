package com.temerarious.mccocr13.temerariousocr.helpers;

/**
 * Created by fabiano.brito on 08/12/2016.
 */

public class BenchmarkResults {
    double[] localElapsedTime;
    double[] remoteElapsedTime;
    int numberOfFiles;
    double localTotal = 0;
    double remoteTotal = 0;
    double localAverage = 0;
    double remoteAverage = 0;

    public void setNumberOfFiles(int files) {
        localElapsedTime = new double[files];
        remoteElapsedTime = new double[files];
        numberOfFiles = files;
    }

    public void setLocalElapsedTime(int position, double time) {
        localElapsedTime[position] = time;
    }

    public void setRemoteElapsedTime(int position, double time) {
        remoteElapsedTime[position] = time;
    }

    public double getLocalElapsedTime(int position) {
        return localElapsedTime[position];
    }

    public double getRemoteElapsedTime(int position) {
        return remoteElapsedTime[position];
    }

    public double getLocalTotal() {
        for (int i = 0; i < localElapsedTime.length; i++) {
            localTotal += localElapsedTime[i];
        }
        return localTotal;
    }

    public double getRemoteTotal() {
        for (int i = 0; i < remoteElapsedTime.length; i++) {
            remoteTotal += remoteElapsedTime[i];
        }
        return remoteTotal;
    }

    public double getLocalAverage() {
        localAverage = localTotal /= numberOfFiles;
        return localAverage;
    }

    public double getRemoteAverage() {
        remoteAverage = remoteTotal /= numberOfFiles;
        return remoteAverage;
    }

    public double getLocalDeviation() {
        double deviation = 0;
        for (int i = 0; i < localElapsedTime.length; i++) {
            deviation += Math.pow((localElapsedTime[i] - localAverage), 2);
        }
        deviation /= localAverage;
        return Math.sqrt(deviation);
    }

}
