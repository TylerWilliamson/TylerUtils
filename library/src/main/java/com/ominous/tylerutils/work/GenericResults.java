package com.ominous.tylerutils.work;

import androidx.work.Data;

public class GenericResults<T> {
    private Data data;
    private T results;

    public GenericResults(Data data, T results) {
        this.data = data;
        this.results = results;
    }

    public Data getData() {
        return data;
    }

    public T getResults() {
        return results;
    }
}
