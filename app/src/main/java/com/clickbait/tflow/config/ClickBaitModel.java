package com.clickbait.tflow.config;

import org.tensorflow.proto.framework.TensorInfo;

public class ClickBaitModel {
    private String modelPath;
    private String mappingPath;
    private int max1DTensorAxis0;
    private boolean postPadding;
    private String notFound;
    private TensorInfo inputTensorInfo;
    private TensorInfo outputTensorInfo;

    public int getMax1DTensorAxis0() {
        return max1DTensorAxis0;
    }

    public TensorInfo getInputTensorInfo() {
        return inputTensorInfo;
    }

    public void setInputTensorInfo(TensorInfo inputTensorInfo) {
        this.inputTensorInfo = inputTensorInfo;
    }

    public TensorInfo getOutputTensorInfo() {
        return outputTensorInfo;
    }

    public void setOutputTensorInfo(TensorInfo outputTensorInfo) {
        this.outputTensorInfo = outputTensorInfo;
    }

    public String getModelPath() {
        return modelPath;
    }

    public String getNotFound() {
        return notFound;
    }

    public void setNotFound(String notFound) {
        this.notFound = notFound;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getMappingPath() {
        return mappingPath;
    }

    public boolean isPostPadding() {
        return postPadding;
    }

    public void setPostPadding(boolean postPadding) {
        this.postPadding = postPadding;
    }

    public void setMappingPath(String mappingPath) {
        this.mappingPath = mappingPath;
    }

    public void setMax1DTensorAxis0(int max1dTensorAxis0) {
        max1DTensorAxis0 = max1dTensorAxis0;
    }
}