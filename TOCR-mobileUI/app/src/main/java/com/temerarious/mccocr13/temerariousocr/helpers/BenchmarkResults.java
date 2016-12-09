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

    public double getRemoteDeviation() {
        double deviation = 0;
        for (int i = 0; i < remoteElapsedTime.length; i++) {
            deviation += Math.pow((remoteElapsedTime[i] - remoteAverage), 2);
        }
        deviation /= remoteAverage;
        return Math.sqrt(deviation);
    }

    public int getLocalMaxIndex() {
        int index = 0;
        for (int i = 1; i < numberOfFiles; i++) {
            if (localElapsedTime[i] > localElapsedTime[i - 1]) {
                index = i;
            }
        }
        return index + 1;
    }

    public int getLocalMinIndex() {
        int index = 0;
        for (int i = 1; i < numberOfFiles; i++) {
            if (localElapsedTime[i] < localElapsedTime[i - 1]) {
                index = i + 1;
            }
        }
        return index;
    }

    public int getRemoteMaxIndex() {
        int index = 0;
        for (int i = 1; i < numberOfFiles; i++) {
            if (remoteElapsedTime[i] > remoteElapsedTime[i - 1]) {
                index = i;
            }
        }
        return index + 1;
    }

    public int getRemoteMinIndex() {
        int index = 0;
        for (int i = 1; i < numberOfFiles; i++) {
            if (remoteElapsedTime[i] < remoteElapsedTime[i - 1]) {
                index = i + 1;
            }
        }
        return index;
    }

}
